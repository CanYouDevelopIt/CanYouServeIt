package appli;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class MasterServer {
	private static Logger logger = Logger.getLogger(MasterServer.class.getCanonicalName());

	private static final boolean ENABLE_LOGGING = true;
	public static final String SETTINGS_FILE_NAME = "server.properties";

	private ServerDescription[] mServersList = null;
	private int mListeningTcpPort = 8899;
	private boolean mUseLoadBalancingAlgorithm = true;
	private long mCheckAliveIntervalMs = 5000;

	public ServerDescription[] getServersList() {
		return mServersList;
	}

	public long getCheckAliveIntervalMs() {
		return mCheckAliveIntervalMs;
	}

	public boolean isLoadBalancingEnabled() {
		return mUseLoadBalancingAlgorithm;
	}

	public void readSettings() throws Exception {
		// Read properties file in a Property object
		Properties props = new Properties();
		props.load(new FileInputStream(SETTINGS_FILE_NAME));

		// Read and parse the server list
		String serversProperty = props.getProperty("Servers");
		if (serversProperty == null)
			throw new Exception("The server list can not be empty.");
		try {
			ArrayList servers = new ArrayList();
			StringTokenizer stServers = new StringTokenizer(serversProperty, ",");
			while (stServers.hasMoreTokens()) {
				String serverAndPort = stServers.nextToken().trim();
				StringTokenizer stServerPort = new StringTokenizer(serverAndPort, ": ");
				String host = stServerPort.nextToken();
				int port = Integer.parseInt(stServerPort.nextToken());
				ServerDescription sd = new ServerDescription(host, port);
				log(sd.toString());
				servers.add(sd);
			}
			mServersList = (ServerDescription[]) servers.toArray(new ServerDescription[] {});
		} catch (Exception e) {
			throw new Exception("Invalid server list format : " + serversProperty);
		}
		if (mServersList.length == 0)
			throw new Exception("The server list can not be empty.");

		// Read server's listening port number
		try {
			mListeningTcpPort = Integer.parseInt(props.getProperty("ListeningPort"));
			log("Listenning port " + mListeningTcpPort + " from properties file.");
		} catch (Exception e) {
			log("Server listening port not specified. Using default port : " + mListeningTcpPort);
		}

		// Read load balancing property
		try {
			String loadBalancing = props.getProperty("LoadBalancing").toLowerCase();
			mUseLoadBalancingAlgorithm = (loadBalancing.equals("yes") || loadBalancing.equals("true")
					|| loadBalancing.equals("1") || loadBalancing.equals("enable") || loadBalancing.equals("enabled"));
			log("Load balancing : " + mUseLoadBalancingAlgorithm);
		} catch (Exception e) {
			log("LoadBalancing property is not specified. Using default value : " + mUseLoadBalancingAlgorithm);
		}

		// Read the check alive interval
		try {
			mCheckAliveIntervalMs = Integer.parseInt(props.getProperty("CheckAliveInterval"));
			log("Check alive interval is " + mCheckAliveIntervalMs + " ms");
		} catch (Exception e) {
			e.printStackTrace();
			log("Check alive interval is not specified. Using default value : " + mCheckAliveIntervalMs + " ms.");
		}

	}

	private void startCheckAliveThread() {
		CheckAliveThread checkAliveThread = new CheckAliveThread(this);
		checkAliveThread.setDaemon(true);
		checkAliveThread.start();
	}

	public void startMasterServer() throws Exception {
		// Bind server on given TCP port
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(mListeningTcpPort);
		} catch (IOException ioe) {
			throw new IOException("Unable to bind to port " + mListeningTcpPort);
		}

		log("Master Server started on TCP port " + mListeningTcpPort + ".");
		log("All TCP connections to " + InetAddress.getLocalHost().getHostAddress() + ":" + mListeningTcpPort
				+ " will be forwarded to the following servers:");
		for (int i = 0; i < mServersList.length; i++) {
			log("  " + mServersList[i].host + ":" + mServersList[i].port);
		}
		log("Load balancing algorithm is " + (mUseLoadBalancingAlgorithm ? "ENABLED." : "DISABLED."));

		// Accept client connections and process them until stopped
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				String clientHostPort = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
				log("Accepted client from " + clientHostPort);
				ServerClientThread masterThread = new ServerClientThread(this, clientSocket);
				masterThread.start();
			} catch (Exception e) {
				throw new Exception("Unexpected error.\n" + e.toString());
			}
		}
	}

	public void log(String aMessage) {
		if (ENABLE_LOGGING)
			System.out.println(aMessage);
	}

	public static void main(String[] aArgs) {
		MasterServer srv = new MasterServer();
		try {
			srv.readSettings();
			srv.startCheckAliveThread();
			srv.startMasterServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

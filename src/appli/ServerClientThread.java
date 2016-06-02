package appli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerClientThread extends Thread {
	private MasterServer masterServer = null;
	private ServerDescription mServer = null;
	private Socket mClientSocket = null;
	private Socket mServerSocket = null;
	private boolean mBothConnectionsAreAlive = false;
	private String mClientHostPort;
	private String mServerHostPort;

	public ServerClientThread(MasterServer aMasterServer, Socket aClientSocket) {
		masterServer = aMasterServer;
		mClientSocket = aClientSocket;
	}

	/**
	 * Obtains a destination server socket to some of the servers in the list.
	 * Starts two threads for forwarding : "client in <--> dest server out" and
	 * "dest server in <--> client out", waits until one of these threads stop
	 * due to read/write failure or connection closure. Closes opened
	 * connections.
	 */
	public void run() {
		try {
			mClientHostPort = mClientSocket.getInetAddress().getHostAddress() + ":" + mClientSocket.getPort();

			// Create a new socket connection to one of the servers from the
			// list
			mServerSocket = createServerSocket();
			if (mServerSocket == null) { // If all the servers are down
				System.out.println(
						"Can not establish connection for client " + mClientHostPort + ". All the servers are down.");
				try {
					mClientSocket.close();
				} catch (IOException e) {
				}
				return;
			}

			// Obtain input and output streams of server and client
			InputStream clientIn = mClientSocket.getInputStream();
			OutputStream clientOut = mClientSocket.getOutputStream();
			InputStream serverIn = mServerSocket.getInputStream();
			OutputStream serverOut = mServerSocket.getOutputStream();

			mServerHostPort = mServer.host + ":" + mServer.port;
			masterServer.log("TCP Forwarding  " + mClientHostPort + " <--> " + mServerHostPort + "  started.");

			// Start forwarding of socket data between server and client
			ForwardThread clientForward = new ForwardThread(this, clientIn, serverOut);
			ForwardThread serverForward = new ForwardThread(this, serverIn, clientOut);
			mBothConnectionsAreAlive = true;
			clientForward.start();
			serverForward.start();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public synchronized void connectionBroken() {
		if (mBothConnectionsAreAlive) {
			// One of the connections is broken. Close the other connection
			// and stop forwarding
			// Closing these socket connections will close their input/output
			// streams
			// and that way will stop the threads that read from these streams
			try {
				mServerSocket.close();
			} catch (IOException e) {
			}
			try {
				mClientSocket.close();
			} catch (IOException e) {
			}

			mBothConnectionsAreAlive = false;
			mServer.clientsConectedCount--;

			masterServer.log("TCP Forwarding  " + mClientHostPort + " <--> " + mServerHostPort + "  stopped.");
		}
	}

	private Socket createServerSocket() throws IOException {
		while (true) {
			mServer = getServerWithMinimalLoad();
			if (mServer == null) // All the servers are down
				return null;
			try {
				Socket socket = new Socket(mServer.host, mServer.port);
				mServer.clientsConectedCount++;
				return socket;
			} catch (IOException ioe) {
				mServer.isAlive = false;
			}
		}
	}

	private ServerDescription getServerWithMinimalLoad() {
		ServerDescription minLoadServer = null;
		ServerDescription[] servers = masterServer.getServersList();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].isAlive) {
				if ((minLoadServer == null) || (servers[i].clientsConectedCount < minLoadServer.clientsConectedCount))
					minLoadServer = servers[i];
				// If load balancing is disabled, return first alive server
				if (!masterServer.isLoadBalancingEnabled())
					break;
			}
		}
		return minLoadServer;
	}

}

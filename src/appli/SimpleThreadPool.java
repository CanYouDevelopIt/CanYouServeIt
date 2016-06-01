package appli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Logger;

public class SimpleThreadPool implements Runnable {
	private static Logger logger = Logger.getLogger(SimpleThreadPool.class.getCanonicalName());

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private String clientName;
	private Boolean shutdown = false;

	public SimpleThreadPool(ServerSocket server, Socket client, String name) {
		this.serverSocket = server;
		this.clientSocket = client;
		this.clientName = name;
	}

	@Override
	public void run() {
		logger.info("Doing stuff for " + clientName);
		if (!shutdown) {
			try {
				doYourJob();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Am shutting you down " + clientName + " !!!!");
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void doYourJob() throws Exception {
		InputStreamReader isr;
		isr = new InputStreamReader(clientSocket.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String line = reader.readLine();
		Date today = new Date();
		String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today + "\n";
		httpResponse += "Hello, " + clientName + "\n";
		while (line != null && !line.isEmpty()) {
			httpResponse += line;
			line = reader.readLine();
		}
		clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
	}

	public void shutdown() {
		shutdown = true;
	}

}

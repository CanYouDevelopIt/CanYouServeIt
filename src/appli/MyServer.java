package appli;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ServerSocketFactory;

public abstract class MyServer extends Thread {

	protected ServerSocket serverSocket;
	protected Boolean running;
	protected int port;
	protected int backlog;
	protected RequestQueue requestQueue;

	public MyServer(int port, int backlog, String requestHandlerClassName, int maxQueueLength, int minThreads,
			int maxThreads) {
		// Save our socket parameters
		this.port = port;
		this.backlog = backlog;
		// Create our request queue
		this.requestQueue = new RequestQueue(requestHandlerClassName, maxQueueLength, minThreads, maxThreads);
	}

	public void startServer() {
		try {
			// Create our Server Socket
			ServerSocketFactory ssf = ServerSocketFactory.getDefault();
			serverSocket = ssf.createServerSocket(this.port, this.backlog);
			// Start our thread
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		try {
			this.running = false;
			this.serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// Start the server
		System.out.println("Server Started, listening on port: " + this.port);
		this.running = true;
		while (running) {
			try {
				// Accept the next connection
				Socket s = serverSocket.accept();

				// Log some debugging information
				InetAddress addr = s.getInetAddress();
				System.out.println(
						"Received a new connection from (" + addr.getHostAddress() + "): " + addr.getHostName());

				// Add the socket to the new RequestQueue
				this.requestQueue.add(s);
			} catch (SocketException se) {
				// We are closing the ServerSocket in order to shutdown the
				// server, so if
				// we are not currently running then ignore the exception.
				if (this.running) {
					se.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down...");

		// Shutdown our request queue
		this.requestQueue.shutdown();
	}

}

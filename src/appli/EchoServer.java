package appli;

import java.util.logging.Logger;

public class EchoServer extends MyServer {
	private static Logger logger = Logger.getLogger(EchoServer.class.getCanonicalName());

	public EchoServer() {
		super(2345, 50, "echo.EchoRequestHandler", 2000, 5, 10);
	}

	public static void main(String args[]) throws Exception {
		EchoServer es = new EchoServer();
		es.startServer();
	}
	// public static void main(String args[]) throws Exception {
	// // Creating Server
	// ServerSocket server = new ServerSocket(port);
	// System.out.println("Listening for connection on port " + port + " ....");
	// int i = 0;
	// List<SimpleThreadPool> listStp = new ArrayList<SimpleThreadPool>();
	// while (true) {
	// if (i == 3) {// accept only 3 clients
	// break;
	// }
	// try (Socket clientSocket = server.accept()) {
	// String name = "Client " + (i++);
	// System.out.println("The " + name + " just connected");
	// SimpleThreadPool clientStp = new SimpleThreadPool(server, clientSocket,
	// name);
	// System.out.println("Adding " + name + " to list of threads");
	// listStp.add(clientStp);
	// clientStp.run();
	// }
	// }
	// System.out.println("There are " + listStp.size() + "client(s)");
	// System.out.println("Shutting every one out");
	// for (SimpleThreadPool stp : listStp) {
	// stp.shutdown();
	// }
	// System.out.println("Shutting Server down in 1 min");
	// Thread.currentThread();
	// Thread.sleep(1000);
	// server.close();
	// System.out.println("Server has shut down.");
	//
	// }
}

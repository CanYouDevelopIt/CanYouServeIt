package echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import appli.RequestHandler;

/**
 * An EchoRequestHandler handles incoming requests sent to an Echo Server
 */
public class EchoRequestHandler implements RequestHandler {
	/**
	 * Echo Server request handling logic
	 */
	@Override
	public void handleRequest(Socket socket) {
		try {
			// Obtain input and output streams to the client
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream out = new PrintStream(socket.getOutputStream());

			// Send our header
			out.println("JavaSRC EchoServer");
			out.flush();

			// Read lines from the client and echo them back; close the
			// connection as soon
			// as the Echo receives a blank line
			String line = in.readLine();
			while (line != null && line.length() > 0) {
				// Echo the line back to the client
				out.println(line);
				out.flush();

				// Read the next line
				line = in.readLine();
			}

			// Be cordial and say goodbye
			out.println("Goodbye");
			out.flush();

			// Close our streams and socket
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
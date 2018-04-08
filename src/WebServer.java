import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

	public static void main(String[] args) throws Throwable {
		// Server-side initializations
		int port = 6789;
		ServerSocket listenSocket = new ServerSocket(port);

		// Process HTTP service requests in an infinite loop
		while (true)
		{
			// Wait for client request
			Socket connSocket = listenSocket.accept();

			// Read client data
			InputStreamReader clientStream = new InputStreamReader(connSocket.getInputStream());
			BufferedReader clientBuffer = new BufferedReader(clientStream);

			// Respond client
			DataOutputStream serverStream = new DataOutputStream(connSocket.getOutputStream());
		}
	}
	
}

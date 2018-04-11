import java.net.ServerSocket;
import java.net.Socket;

public class WebServer
{

	public static void main(String[] args) throws Exception
	{
		// Server-side initializations
		int port = 6789;
		ServerSocket listenSocket = new ServerSocket(port);

		// Process HTTP service requests in an infinite loop
		while (true)
		{
			// Wait for client request
			Socket socket = listenSocket.accept();

			// Construct an object to process the HTTP request message
			HttpRequest request = new HttpRequest(socket);

			// Create a new thread to process the request
			Thread thread = new Thread(request);

			// Start the thread
			thread.start();
		}
	}

}

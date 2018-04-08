import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

public class HttpRequest implements Runnable
{
	static String CRLF = "\r\n";
	static String StatusCodeOK = " 200 OK";
	static String StatusCodeNotFound = " 404 Not Found";
	private Socket socket;

	public HttpRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}

	@Override
	public void run()
	{
		try 
		{
			processRequest();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	private void processRequest() throws Exception
	{
		// Get a reference to the socket's input and output streams
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		// Set up input stream filters
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		// Get the request line of the HTTP request message
		String requestLine = br.readLine();
		
		// Display the request line
		System.out.println(requestLine);
		
		// Get and display the header lines
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0)
		{
			System.out.println(headerLine);
		}
		
		// Additional line to command window
		System.out.println();

		// Extract the filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();

		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;

		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try
		{
			fis = new FileInputStream(fileName);
		}
		catch (FileNotFoundException e)
		{
			fileExists = false;
		}

		// Extract the HTTP version from the request line
		String httpVersion = tokens.nextToken();

		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists)
		{
			statusLine = httpVersion + StatusCodeOK + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
		}
		else
		{
			statusLine = httpVersion + StatusCodeNotFound + CRLF;
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" + 
						 "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
						 "<BODY>Not Found</BODY></HTML>";
		}

		// Send the status line.
		os.writeBytes(statusLine);

		// Send the content type line.
		os.writeBytes(contentTypeLine);

		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);

		// Send the entity body.
		if (fileExists)
		{
			sendBytes(fis, os);
			fis.close();
		}
		else
		{
			os.writeBytes(entityBody);
		}
		
		// Close streams and socket
		os.close();
		br.close();
		socket.close();
	}

	private void sendBytes(FileInputStream fis, DataOutputStream os) throws Exception
	{
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1)
		{
		   os.write(buffer, 0, bytes);
		}		
	}

	private String contentType(String fileName)
	{
		String contentType;

		if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
		{
			contentType = "text/html";
		}
		else if (fileName.endsWith(".gif"))
		{
			contentType = "image/gif";
		}
		else if (fileName.endsWith(".jpg"))
		{
			contentType = "image/jpeg";
		}
		else
		{
			contentType = "application/octet-stream";
		}

		return contentType;
	}
	
}

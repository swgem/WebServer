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
		
		// Get the header lines
		String headerLines = requestLine + CRLF;
		String line;
		while ((line = br.readLine()).length() != 0)
		{
			headerLines += line + CRLF;
		}

		// Print the header lines
		printHeader(headerLines);

		// Extract the filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String filename = "./res" + tokens.nextToken(); // search in res folder

		// Extract the HTTP version from the request line
		String httpVersion = tokens.nextToken();

		// Send the response message
		respondClient(httpVersion, filename, os);		
		
		// Close streams and socket
		os.close();
		br.close();
		socket.close();
	}

	private void printHeader(String headerLines)
	{
		// Additional line to command window
		System.out.println(headerLines);

		// Additional line to command window
		System.out.println();
	}

	private void respondClient(String httpVersion, String filename, DataOutputStream os) throws Exception 
	{
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		// Retrieve file from directory if it exists
		FileInputStream fis = retrieveFile(filename);
		
		// Construct the response message
		if (fis != null)
		{
			statusLine = httpVersion + StatusCodeOK + CRLF;
			contentTypeLine = "Content-type: " + contentType(filename) + CRLF;
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
		if (fis != null)
		{
			sendBytes(fis, os);
			fis.close();
		}
		else
		{
			os.writeBytes(entityBody);
		}
	}

	private FileInputStream retrieveFile(String filename)
	{
		// Open the requested file.
		FileInputStream fis;
		try
		{
			fis = new FileInputStream(filename);
		}
		catch (FileNotFoundException e)
		{
			fis = null;
		}

		return fis;
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

	private String contentType(String filename)
	{
		String contentType;

		if (filename.endsWith(".htm") || filename.endsWith(".html"))
		{
			contentType = "text/html";
		}
		else if (filename.endsWith(".txt"))
		{
			contentType = "text/plain";
		}
		else if (filename.endsWith(".gif"))
		{
			contentType = "image/gif";
		}
		else if (filename.endsWith(".jpg"))
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

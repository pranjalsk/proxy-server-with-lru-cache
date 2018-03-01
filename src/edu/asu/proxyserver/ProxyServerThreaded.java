package edu.asu.proxyserver;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class ProxyServerThreaded {

	private int localPort;
	private String serverName;
	private int port;
	private long cacheSizeKB;
	private int delayInMs;

	public ProxyServerThreaded(int localPort, String serverName, int port, long cacheSizeKB, int delayInMs) {
		this.localPort = localPort;
		this.serverName = serverName;
		this.port = port;
		this.cacheSizeKB = cacheSizeKB;
		this.delayInMs = delayInMs;
	}

	public void startServer() {

		ServerSocket server = null;
		Socket sock = null;

		try {
			server = new ServerSocket(localPort);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		while (server.isBound() && !server.isClosed()) {
			System.out.println("Ready...ServerStarted...");
			try {
				sock = server.accept();
				createClientThread(sock, serverName, port, cacheSizeKB, delayInMs);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void createClientThread(Socket sock, String serverName, int port, long cacheSizeKB, int delayInMs) {
		Thread thread = new Thread(new ClientHandler(sock, serverName, port, cacheSizeKB, delayInMs));
		System.out.println("New thread created: " + thread.getName());
		thread.start();
	}
}

class ClientHandler implements Runnable {

	Socket clientSocket;
	String serverName;
	int port;
	long cacheSizeKB;
	int delayInMs;
	CacheLRU cache;

	InputStream in = null;
	OutputStream outputStream = null;

	public ClientHandler(Socket clientSocket, String serverName, int port, long cacheSizeKB, int delayInMs) {

		this.clientSocket = clientSocket;
		this.serverName = serverName;
		this.port = port;
		this.cacheSizeKB = cacheSizeKB;
		this.delayInMs = delayInMs;
		this.cache = new CacheLRU(cacheSizeKB);

		try {
			in = clientSocket.getInputStream();
			outputStream = clientSocket.getOutputStream();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void createResponse(InputStream inStream) {

		BufferedReader bufferedReader = null;
		String line = null;
		String filename = null;
		String remoteData = "";
		String requestURL = "";
		int responseCode = 0;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inStream));
			line = bufferedReader.readLine();
			System.out.println("Received request: " + line);
			if (line != null && !line.trim().equals("")) {
				StringTokenizer st = new StringTokenizer(line);
				if (st.nextToken().equals("GET") && st.hasMoreTokens()) {
					filename = st.nextToken();
					if (filename.startsWith("/")) {
						filename = filename.substring(1);
					}
				} else {
					String invalidRequest = "<html>Invalid request: Only accepts GET requests</html>";
					outputStream.write(invalidRequest.getBytes());
				}
			}

			// ---------Request received------Cache Ops-------------
			if (filename.equals(null) || filename.equals("")) {
				remoteData = "<html>Illegal request: File name not received</html>";
				outputStream.write(remoteData.getBytes());
            }
			requestURL = serverName + "/" + filename;

			System.out.println("Requested page is: " + requestURL);

			
			synchronized (cache) {
				if (cache.containsKey(requestURL)) {
					remoteData = cache.get(requestURL);
					System.out.println("Cache hit........" + requestURL);
				} else {
					remoteData = ProxyClient.connectRemote(requestURL);
					responseCode = ProxyClient.getResponseCode();					
//					System.out.println("Remote data received at server: "+ remoteData);
					if (remoteData.length() > cacheSizeKB) {
						System.out.println("File size larger than cache....");
					} else {
						String cache_miss = cache.put(requestURL, remoteData);
						System.out.println("Cache missed.........." + cache_miss);
					}
				}
			}		
//				System.out.println("Remotedata is: "+ remoteData);
				if (!remoteData.equals(null)) {
					outputStream.write(remoteData.getBytes());
				} else {
					String emptyResponse = "<html>File not found</html>";
					outputStream.write(emptyResponse.getBytes());
				}
				
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void run() {

		System.out.println("Starting thread");
		try {
			Thread.sleep(delayInMs);
			createResponse(in);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
				outputStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		System.out.println("Ending thread");
	}
}

package edu.asu.proxyserver;

public class Driver {

	public static void main(String[] args) {
		
		if (args.length != 5) {
			System.out.println("USAGE: Input should be <local port> <server> <port> <cacheInKB> <delayInMilliseconds>");
			System.exit(0);
		}
		else{
			//sample input: 8000 //docs.oracle.com 80 1024 5000
			
			int localPort = Integer.parseInt(args[0]);
			String serverName = "http://" + args[1];
			int port = Integer.parseInt(args[2]);
			long cacheSizeKB = Long.parseLong(args[3]) * 1000;
			int delayInMs = Integer.parseInt(args[4]);
			ProxyServerThreaded server = new ProxyServerThreaded(localPort,serverName,port,cacheSizeKB,delayInMs);
			
			System.out.println("Arguments are:"+ localPort + " "+ serverName+" "+port+" "+cacheSizeKB+" "+" "+delayInMs);
			
			server.startServer();
			
		}
		
		
	}

}

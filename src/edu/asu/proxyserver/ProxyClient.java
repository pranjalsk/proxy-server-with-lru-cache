package edu.asu.proxyserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProxyClient {

	public static int responseCode = 0;
	
	public static String connectRemote(String url){
		InputStream in = null;
		
		try {
			URL obj = new URL(url);
			
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		    con.setRequestMethod("GET");
		    con.setRequestProperty("User-Agent", "Mozilla/5.0");
		    
		    responseCode = con.getResponseCode();
		    
		    System.out.println("\nSending 'GET' request to URL : " + url);
		    System.out.println("Response Code : " + responseCode);
		    try {
			    in = con.getInputStream();
			} catch (Exception e) {
				if (responseCode >= 300 && responseCode <400){
					return "<html>"+responseCode+" Redirection Error</html>";
				}else if (responseCode >= 400 && responseCode <500){
					switch (responseCode) {
					case 400:
						return "<html>"+responseCode+" Bad Request</html>";
					case 401:
						return "<html>"+responseCode+" Unauthorized</html>";
					case 403:
						return "<html>"+responseCode+" Forbidden</html>";
					case 404:
						return "<html>"+responseCode+" Requested Page Not Found</html>";
					default:
						return "<html>"+responseCode+" Conflict Error</html>";
					}					
				}
				else if (responseCode >= 500 && responseCode <600){
					if (responseCode == 500) {
						return "<html>"+responseCode+" Internal Server Error</html>";
					}
					return "<html>"+responseCode+" Server Error</html>";
				}else{
				return "<html>Unknown Error</html>";
				}
			}
		    //Convert stream to string
		    StringBuilder sb=new StringBuilder();
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String read;
		    while((read=br.readLine()) != null) {
		        sb.append(read);   
		    }
		    br.close();
		    in.close();
		    
		    return sb.toString();
			   
		}catch (Exception e) {
            e.printStackTrace();
        }     
	    
		return null;
	}
	
	public static int getResponseCode(){
		return responseCode;
	}
	
	
}

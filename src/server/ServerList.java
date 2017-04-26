package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ServerList {
	private JSONArray serverList = new JSONArray();
	
	@SuppressWarnings("unchecked")
	public synchronized void update(JSONArray newList, String hostname, int hostport) 
			throws ClassCastException, UnknownHostException, NumberFormatException, serverException {
		for(Object newEle : newList) {
			JSONObject newServer = (JSONObject) newEle;
			
			//Check validation
			String newHostname = (String) newServer.get("hostname");
			InetAddress.getByName(newHostname);
			int newPort = Integer.parseInt(newServer.get("port").toString());
			
			if(newPort < 0 || newPort > 65535) {
				throw new serverException("invalid server record");
			}
			
			boolean add = true;
			if(newHostname.equals(hostname) && newPort == hostport){
			    add = false;
			}
			
			for(Object oldEle : serverList) {
				JSONObject oldServer = (JSONObject) oldEle;
				String oldHostname = (String) oldServer.get("hostname");
	            int oldPort = Integer.parseInt(oldServer.get("port").toString());
	            
				if(newHostname.equals(oldHostname) && newPort == oldPort){
	                add = false;
	            }
			}
			if(add) serverList.add(newServer);
		}		
		//TODO testing
		System.out.println("Server list: " + serverList.toJSONString());
	}
	
	public JSONObject select() {
		if(serverList.size() > 0) {
			int random = ThreadLocalRandom.current().nextInt(0, serverList.size());
			return (JSONObject) serverList.get(random);
		}
		return null;
	}
	
	public JSONArray getServerList() {
		if(serverList.size() > 0) return serverList;
		return null;
	}
	
	public int getLength() {
		return serverList.size();
	}
	
	public void remove(JSONObject server) {
		serverList.remove(server);
	}
}

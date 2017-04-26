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
			JSONObject host = new JSONObject();
			host.put("hostname", hostname);
			host.put("port", hostport);
		for(Object newEle : newList) {
			JSONObject newServer = (JSONObject) newEle;
			
			//Check validation
			InetAddress.getByName((String) newServer.get("hostname"));
			int port = Integer.parseInt(newServer.get("port").toString());
			if(port < 0 || port > 65535) {
				throw new serverException("invaild server record");
			}
			
			boolean add = true;
			if(host.equals(newEle)) add = false;
			
			for(Object oldEle : serverList) {
				JSONObject oldServer = (JSONObject) oldEle;
				if(oldServer.equals(newServer)) add = false;
			}
			if(add) serverList.add(newServer);
		}		
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

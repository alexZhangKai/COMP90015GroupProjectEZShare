package EZShare;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Subscription {
	private List<Resource> resourceTemplate = new ArrayList<Resource>();
	private DataOutputStream sendToClient;
	private String id;
	private List<Socket> relaySocList = new ArrayList<Socket>();
	private List<ListenRelay> listenRelayList = new ArrayList<ListenRelay>();
	private int resultCount = 0;
	
	public Subscription(Resource resourceTemplate, DataOutputStream sendToClient, String id) {
		this.resourceTemplate.add(resourceTemplate);
		this.sendToClient = sendToClient;
		this.id = id;
	}
	
	public Subscription(Resource resourceTemplate, DataOutputStream sendToClient, String id, List<Socket> relaySocList) {
		this.resourceTemplate.add(resourceTemplate);
		this.sendToClient = sendToClient;
		this.id = id;
		this.relaySocList = relaySocList;
	}
	
	void matchNewResource(Resource newRes) throws IOException {
		//Check if match any template
        for(Resource template : resourceTemplate) {
        	if(ResourceList.queryingForSubscription(template, newRes)){
        		break;
        	}
        }
        
        JSONObject resource_temp = Connection.Resource2JSONObject(newRes); 
        sendToClient.writeUTF(resource_temp.toJSONString());
        //TODO handle debug here
        
	}
	
	void matchResource() throws IOException {
		//store local results here
        List<Resource> res_results = new ArrayList<Resource>();
        for(Resource template : resourceTemplate) {
        	res_results = ResourceList.queryForQuerying(template);
        }
        
        resultCount += res_results.size();
        
        //send each resource to client
        for (Resource res: res_results){
            JSONObject resource_temp = Connection.Resource2JSONObject(res); 
            sendToClient.writeUTF(resource_temp.toJSONString());
            //TODO handle debug here
        }
	}
	
	public String getId() {
		return this.id;
	}

	public void getNewTemplate(Resource newResTemplate) {
		this.resourceTemplate.add(newResTemplate);
		newTemplate(newResTemplate);
	}
	
	//Listen on each server in a new thread 
	public void ListenSubscribeRelay() {
		for(Socket relay : relaySocList) {
			ListenRelay listenRelay = new ListenRelay(sendToClient, relay, resourceTemplate, this.id);
			this.listenRelayList.add(listenRelay);
			listenRelay.start();
		}
	}
	
	@SuppressWarnings("unchecked")
	public int unsubscribe() {
		for(ListenRelay listenRelay : listenRelayList) {
			listenRelay.setUnsubscribeFlag();
		}
		
		//calculate total result size
		int totalResultSize = this.resultCount;
		JSONParser parser = new JSONParser();
		for(Socket relay : relaySocList) {
			try {
				DataOutputStream sendToRelay = new DataOutputStream(relay.getOutputStream());
				DataInputStream receiveFromRelay = new DataInputStream(relay.getInputStream());
				
				JSONObject command = new JSONObject();
				command.put("command", "UNSUBSCRIBE");
				command.put("id", this.id);
				
				sendToRelay.writeUTF(command.toJSONString());
				
				if(receiveFromRelay.available() > 0) {
					JSONObject relayReply = (JSONObject) parser.parse(receiveFromRelay.readUTF());
					int relaySize = (int) relayReply.get("resultSize");
					totalResultSize += relaySize;
				}
				
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
		
		return totalResultSize;
	}
	
	public void newTemplate(Resource newResTemplate) {
		for(ListenRelay listenRelay: listenRelayList) {
			listenRelay.setNewTemplateFlag(newResTemplate);
		}
	}
	
	public void newRelay(JSONArray newRelayList) {
		try{
			for (Object newServerObject : newRelayList) {
				JSONObject newServerJSON = (JSONObject) newServerObject;
				String newHostname = (String) newServerJSON.get("hostname");
				int newPort = Integer.parseInt(newServerJSON.get("port").toString());
				
				Socket newRelay = new Socket(newHostname, newPort);
				this.relaySocList.add(newRelay);

				ListenRelay listenRelay = new ListenRelay(sendToClient, newRelay, resourceTemplate, this.id);
				this.listenRelayList.add(listenRelay);
				listenRelay.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ListenRelay extends Thread{
	private Socket server;
	private DataOutputStream sendToClient;
	private volatile boolean newTemplateFlag = false;
	private volatile boolean unsubscribeFlag = false;
	private List<Resource> template;
	private Resource newTemplate;
	private String id;
	
	public ListenRelay(DataOutputStream sendToClient, Socket server, List<Resource> template, String id) {
		this.sendToClient = sendToClient;
		this.server = server;
		this.template = template;
		this.id = id;
	}

	public void setNewTemplateFlag(Resource newTemplate) {
		this.newTemplate = newTemplate;
		this.newTemplateFlag = true;
	}
	
	public void setUnsubscribeFlag() {
		this.unsubscribeFlag = true;
	}
 	
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		DataInputStream serverReply;
		DataOutputStream sendToServer;
		
		try {
			serverReply = new DataInputStream(server.getInputStream());
			sendToServer = new DataOutputStream(server.getOutputStream());
			JSONObject command;
			JSONObject resourceTemplate;
			
			for(Resource res : template) {
				//send the first subscribe command here
				command = new JSONObject();
				resourceTemplate = Connection.Resource2JSONObject(res);

				command.put("command", "SUBSCRIBE");
				command.put("relay", false);
				command.put("id",this.id);
				command.put("resourceTemplate", resourceTemplate);

				sendToServer.writeUTF(command.toJSONString());
			}
			
			while(true) {
				//TODO change to...
				if(serverReply.available() > 0) {
					String result = serverReply.readUTF();
					if(!result.contains("response") && !result.contains("success")) {
						this.sendToClient.writeUTF(result);
					}
				}
				
				if(this.newTemplateFlag) {
					//TODO send new subscribe command here
					command = new JSONObject();
			    	resourceTemplate = Connection.Resource2JSONObject(this.newTemplate);
			    	
			    	command.put("command", "SUBSCRIBE");
			    	command.put("relay", false);
			    	command.put("id",this.id);
			    	command.put("resourceTemplate", resourceTemplate);
					
			    	sendToServer.writeUTF(command.toJSONString());
				}
				
				if(this.unsubscribeFlag) {
					break;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

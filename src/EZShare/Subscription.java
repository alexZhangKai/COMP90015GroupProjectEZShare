package EZShare;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Subscription {
	private List<Resource> resourceTemplate = new ArrayList<Resource>();
	private DataOutputStream sendToClient;
	private String id;
	private List<Socket> relaySocList = null;
	private List<ListenRelay> listenRelayList = new ArrayList<ListenRelay>();
	private int resultCount = 0;
	private Boolean secure = false;
	private Boolean relay = false;
	private Boolean debug = false;

    // Relay specified by client
    public Subscription(Resource resourceTemplate, DataOutputStream sendToClient, 
	        String id, List<Socket> relaySocList, 
	        Boolean secure, Boolean relay, Boolean debug) {
		this.resourceTemplate.add(resourceTemplate);
		this.sendToClient = sendToClient;
		this.id = id;
		this.secure = secure;
		this.relaySocList = relaySocList;
		this.relay = relay;
		this.debug = debug;
	}

	// No relay specified by client
	public Subscription(Resource resourceTemplate, DataOutputStream sendToClient, 
	        String id, Boolean relay, Boolean debug) {
	    this.resourceTemplate.add(resourceTemplate);
        this.sendToClient = sendToClient;
        this.id = id;
        this.relay = relay;
        this.debug = debug;
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
        if (debug) {
            System.out.println(new Timestamp(System.currentTimeMillis())
                    + " - [DEBUG] - RECEIVED: " + resource_temp.toJSONString());
        }        
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
            if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())
                        + " - [DEBUG] - RECEIVED: " + resource_temp.toJSONString());
            }
        }
	}

	public void getNewTemplate(Resource newResTemplate) {
		this.resourceTemplate.add(newResTemplate);
		newTemplate(newResTemplate);
	}
	
	//Listen on each server in a new thread 
	public void ListenSubscribeRelay() {
		for(Socket relay : relaySocList) {
			ListenRelay listenRelay = new ListenRelay(
			        sendToClient, relay, resourceTemplate, id, debug);
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
				if (debug) {
		            System.out.println(new Timestamp(System.currentTimeMillis())
		                    + " - [DEBUG] - RECEIVED: " + command.toJSONString());
		        }
				
				if(receiveFromRelay.available() > 0) {
					JSONObject relayReply = (JSONObject) parser.parse(receiveFromRelay.readUTF());
					if (debug) {
		                System.out.println(new Timestamp(System.currentTimeMillis())
		                        + " - [DEBUG] - RECEIVED: " + relayReply);
		            }
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
				
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket newRelay = null;
				if (secure) {
				    newRelay = sslsocketfactory.createSocket(newHostname, newPort);
                } else {
                    newRelay = new Socket(newHostname, newPort); 
                }
				
				relaySocList.add(newRelay);

				ListenRelay listenRelay = new ListenRelay(
				        sendToClient, newRelay, resourceTemplate, id, debug);
				listenRelayList.add(listenRelay);
				listenRelay.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Boolean getSecure() {
        return secure;
    }

    public Boolean getRelay() {
        return relay;
    }
}

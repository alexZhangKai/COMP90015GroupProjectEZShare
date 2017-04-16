//lisa branch
package server;

import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Connection implements Runnable {

	private int id;
	private Socket client;
	private ResourceList resourceList;
	
	public Connection(int id, Socket client, ResourceList resourceList) {
		this.id = id;
		this.client = client;
		this.resourceList = resourceList;
	}
	
	@Override
	public void run() {
		System.out.println("Connection: " + id);
		try {
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());			
			JSONParser parser = new JSONParser();
			
			JSONObject command = (JSONObject) parser.parse(input.readUTF());
			JSONObject reply = new JSONObject();
			if(command.containsKey("command")) {
				switch((String) command.get("command")) {
				case "PUBLISH":
					System.out.println("enter publish");
					System.out.println(command.toJSONString());
					System.out.println("--------------------------");
					reply = publish(command);
					break;
				default:
					reply.put("error", "unknown_command");
				}

			}
			//put reply in output stream
			output.writeUTF(reply.toJSONString());
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}		
	}

	private JSONObject publish(JSONObject command) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject resource = (JSONObject) parser.parse((String) command.get("resource"));
		
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		String URI = (String) resource.get("uri");
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		String EZserver = resource.containsKey("ezserver") ? (String) resource.get("ezserver") : "";
		
		resource.put("description", Description);
		resource.put("uri", URI);
		resource.put("channel", Channel);
		resource.put("owner", Owner);
		resource.put("ezserver", EZserver);
		
		boolean result = resourceList.addResource(resource);
		
		//make a reply
		JSONObject reply = new JSONObject();
		if(result) {
			reply.put("response", "success");
		} else {
			reply.put("reponse", "fail");
		}
		
		return reply;
	}
}

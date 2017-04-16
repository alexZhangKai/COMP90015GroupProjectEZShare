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
		JSONObject resourceJSON = (JSONObject) parser.parse((String) command.get("resource"));
		
		Resource resource = JSONObj2Resource(resourceJSON);
		
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
	
	private Resource JSONObj2Resource(JSONObject resource) {
		
		String Name = resource.containsKey("name") ? (String) resource.get("name") : "";
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		//String[] Tags is different deal with it later
		String[] Tags = new String[0];
		String URI = resource.containsKey("uri") ? (String) resource.get("uri") : "";
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		String EZserver = resource.containsKey("ezserver") ? (String) resource.get("ezserver") : "";
		
		return new Resource(Name, Description, Tags, URI, Channel, Owner, EZserver);
	}
}

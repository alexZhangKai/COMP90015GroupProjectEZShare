package server;

import java.io.*;
import java.net.*;
import java.util.Arrays;

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
			
			//TODO Create blank methods for everyone to work on
			if(command.containsKey("command")) {
				switch((String) command.get("command")) {
				case "PUBLISH":
					publish(command,output);
					break;
				case "REMOVE":
					remove(command, output);
					break;
				case "FETCH":
					fetch(command, output);
					break;
				default:
					JSONObject reply = new JSONObject();
					reply.put("error", "unknown_command");
					output.writeUTF(reply.toJSONString());
				}

			}
			
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}		
	}

	private void publish(JSONObject command, DataOutputStream output) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject resourceJSON = (JSONObject) parser.parse((String) command.get("resource"));
		
		Resource resource = JSONObj2Resource(resourceJSON);
		
		boolean result = resourceList.addResource(resource);
		
		//create a reply
		JSONObject reply = new JSONObject();
		if(result) {
			reply.put("response", "success");
		} else {
			reply.put("reponse", "fail");
		}
		
		//put reply in output stream
		System.out.println("resourceListSize: " + resourceList.getSize());
		output.writeUTF(reply.toJSONString());
	}
	
	private void remove(JSONObject command, DataOutputStream output) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject resourceJSON = (JSONObject) parser.parse((String) command.get("resource"));
		
		Resource resource = JSONObj2Resource(resourceJSON);
		boolean result = resourceList.removeResource(resource);
		
		//create a reply
		JSONObject reply = new JSONObject();
		if(result) {
			reply.put("response", "success");
		} else {
			reply.put("reponse", "fail");
		}
		
		//put reply in output stream
		System.out.println("resourceListSize: " + resourceList.getSize());
		output.writeUTF(reply.toJSONString());
	}
	
	private void fetch(JSONObject command, DataOutputStream output) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject resourceJSON = (JSONObject) parser.parse((String) command.get("resourceTemplate"));
		
		Resource resourceTemplate = JSONObj2Resource(resourceJSON);
		//Use a known URI, need to check the file afterward ? 
		String URI = "serverFile/testFile.png";
		File f = new File(URI);
		if(f.exists()) {
			JSONObject reponse = new JSONObject();
			reponse.put("response", "success");
			output.writeUTF(reponse.toJSONString());
			
			RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			
			JSONObject resource = new JSONObject();
			resource.put("resourceSize", byteFile.length());
			output.writeUTF(resource.toJSONString());
			
			byte[] sendingBuffer = new byte[1024*1024];
			int num;
			while((num = byteFile.read(sendingBuffer)) > 0) {
				output.write(Arrays.copyOf(sendingBuffer, num));
			}
			byteFile.close();
			
			JSONObject resultFile = new JSONObject();
			resultFile.put("resultSize", 1);
			output.writeUTF(resultFile.toJSONString());
		}
		
	}
	
	private Resource JSONObj2Resource(JSONObject resource) {
		//handle default value here
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

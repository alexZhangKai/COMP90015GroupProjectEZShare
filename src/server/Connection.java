/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * Class that handles connections with each client
 * It runs as a separate thread
 * It implements the major functionality expected of the server e.g. Publish, Remove, etc.
 */

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
	
	@SuppressWarnings("unchecked")
	//JSONObject extends HashMap but does not have type parameters as HashMap would expect...
	
    @Override
	public void run() {
		System.out.println("Connection: " + id);
		try {
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());			
			JSONParser parser = new JSONParser();
			
			JSONObject client_request = (JSONObject) parser.parse(input.readUTF());
			
			if(client_request.containsKey("command")) {
				switch((String) client_request.get("command")) {
				case "PUBLISH":
					publish(client_request, output);
					break;
				case "REMOVE":
					remove(client_request, output);
					break;
				case "SHARE":
                    share(client_request, output);
                    break;
				case "QUERY":
                    query(client_request, output);
                    break;
				case "FETCH":
					fetch(client_request, output);
					break;
				case "EXCHANGE":
                    exchange(client_request, output);
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

	private void exchange(JSONObject client_request, DataOutputStream output) {
        // TODO Exchange method
        
    }

    private void query(JSONObject client_request, DataOutputStream output) {
        // TODO Query method
        
    }

    private void share(JSONObject client_request, DataOutputStream output) {
        // TODO Share method
        
    }

    @SuppressWarnings("unchecked")
    private void publish(JSONObject client_req, DataOutputStream output) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject resourceJSON = (JSONObject) parser.parse((String) client_req.get("resource"));
		
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
    private void fetch(JSONObject command, DataOutputStream output) throws ParseException, IOException {

	    //the following three lines not being used so far, thus commented out. Decomment when needed.
//		JSONParser parser = new JSONParser();
//		JSONObject resourceJSON = (JSONObject) parser.parse((String) command.get("resourceTemplate"));
//		Resource resourceTemplate = JSONObj2Resource(resourceJSON);
		
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

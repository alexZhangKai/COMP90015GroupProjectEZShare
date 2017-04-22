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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Connection implements Runnable {
	private int id;
	private Socket client;
	private ResourceList resourceList;
	private ServerList serverList;
	private String serverSecret;
	
	public Connection(int id, Socket client, ResourceList resourceList, ServerList serverList, String serverSecret) {
		this.id = id;
		this.client = client;
		this.resourceList = resourceList;
		this.serverList = serverList;
		this.serverSecret = serverSecret;
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
			
			if(!client_request.containsKey("command")) throw new ClassCastException();
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
				reply.put("response", "error");
				reply.put("errorMessage", "invalid command");
				output.writeUTF(reply.toJSONString());
			}	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException | ClassCastException e) {
			//TODO can be better?
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing or incorrect type for command");
			try {
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				output.writeUTF(reply.toJSONString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}		
	}

	@SuppressWarnings("unchecked")
	private void exchange(JSONObject client_request, DataOutputStream output) throws IOException {
		try{
			JSONArray newServerList = (JSONArray) client_request.get("serverList");
			try {
				this.serverList.update(newServerList);
				JSONObject reply = new JSONObject();
		        reply.put("response", "success");
		        output.writeUTF(reply.toJSONString());
			} catch (NumberFormatException | ClassCastException | UnknownHostException | serverException e) {
				JSONObject reply = new JSONObject();
				reply.put("response", "error");
				reply.put("errorMessage", "invaild server record");
				output.writeUTF(reply.toJSONString());
				e.printStackTrace();
			}
        
		} catch (ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing or invaild server list");
			output.writeUTF(reply.toJSONString());
		}
    }

    private void query(JSONObject client_req, DataOutputStream output) {
        // TODO Query method
        
    }

    @SuppressWarnings("unchecked")
    private void share(JSONObject client_req, DataOutputStream output) throws IOException {
    	JSONParser parser = new JSONParser();
		try{
			JSONObject resourceJSON = (JSONObject) parser.parse((String) client_req.get("resource"));
			Resource resource = JSONObj2Resource(resourceJSON);
			
			if(client_req.get("secret").equals(this.serverSecret)) throw new serverException("incorrect secret");
			
			resourceList.addResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
		} catch(ParseException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource");
			output.writeUTF(reply.toJSONString());
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
		}
    }

    @SuppressWarnings("unchecked")
    private void publish(JSONObject client_req, DataOutputStream output) throws IOException {
		JSONParser parser = new JSONParser();
		try{
			JSONObject resourceJSON = (JSONObject) parser.parse((String) client_req.get("resource"));
			Resource resource = JSONObj2Resource(resourceJSON);
			
			resourceList.addResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
		} catch(ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource");
			output.writeUTF(reply.toJSONString());
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
		}
	}
	
	@SuppressWarnings("unchecked")
    private void remove(JSONObject client_req, DataOutputStream output) throws IOException {
		JSONParser parser = new JSONParser();
		try{
			JSONObject resourceJSON = (JSONObject) parser.parse((String) client_req.get("resource"));
			Resource resource = JSONObj2Resource(resourceJSON);
			
			resourceList.removeResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
		} catch(ParseException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource");
			output.writeUTF(reply.toJSONString());
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
		}
	}
	
	@SuppressWarnings("unchecked")
    private void fetch(JSONObject command, DataOutputStream output) throws IOException{
		JSONParser parser = new JSONParser();
		JSONObject resourceJSON;
		try {
			resourceJSON = (JSONObject) parser.parse((String) command.get("resourceTemplate"));
		
			Resource resourceTemplate = JSONObj2Resource(resourceJSON);
		
			Resource match = resourceList.queryForChannelURI(resourceTemplate);
		
			//handle no match error
			if(match.equals(null)) {
				JSONObject reply = new JSONObject();
				reply.put("response", "error");
				reply.put("errorMessage", "no match resource");
				output.writeUTF(reply.toJSONString());
				return;
			}
		
			//Use a known URI, need to check the file afterward ? 
			String URI = match.getURI();
			File f = new File(URI);
			if(f.exists()) {
				JSONObject reponse = new JSONObject();
				reponse.put("response", "success");
				output.writeUTF(reponse.toJSONString());
			
				RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			
				JSONObject resource = Resource2JSONObject(match);
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
		} catch (ParseException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resourceTemplate");
			output.writeUTF(reply.toJSONString());
		} catch (serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
		}
	}
	
	private Resource JSONObj2Resource(JSONObject resource) throws serverException {
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
	
	@SuppressWarnings("unchecked")
	private JSONObject Resource2JSONObject(Resource resource) {
		JSONObject reJSON = new JSONObject();
		reJSON.put("name", resource.getName());
        //reJSON.put("tags", tags);
		reJSON.put("description", resource.getDescription());
		reJSON.put("uri", resource.getURI());
		reJSON.put("channel", resource.getChannel());
		reJSON.put("owner", resource.getOwner());
		reJSON.put("ezserver", resource.getEZserver());
		
		return reJSON;
	}
}

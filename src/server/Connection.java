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

    @SuppressWarnings("unchecked")
    private void query(JSONObject client_request, DataOutputStream output) throws IOException {                
        if (client_request.containsKey("resourceTemplate")) {
            try {
                Resource in_res;
                JSONParser parser = new JSONParser();
                
                // The following should throw an invalid resource exception
                in_res = this.JSONObj2Resource((JSONObject) parser.parse((String)client_request.get("resourceTemplate")));
                
                // If there was a resourceTemplate AND the template checked out as a valid Resource, report success
                JSONObject response = new JSONObject();
                response.put("response", "success");
                output.writeUTF(response.toJSONString());
                
                int result_cnt = 0;
                
                //get results from other servers first if relay == true
                ResourceList results = new ResourceList();
                if (client_request.containsKey("relay")) {
                    if ((Boolean)client_request.get("relay")) {
//                        results = propagateQuery(client_request);
                        result_cnt += results.getSize();
                    }
                }
                
                //Query current resource list for resources that match the template
                for (Resource curr_res: resourceList.getResList()){
                    if (in_res.getChannel().equals(curr_res.getChannel()) && 
                            in_res.getOwner().equals(curr_res.getOwner()) &&
//                            in_res.getTags().equals(curr_res.getTags()) &&
                            in_res.getURI().equals(curr_res.getURI()) &&
                                (curr_res.getName().contains(in_res.getName()) ||
                                curr_res.getDescription().contains(in_res.getDescription()))) {
                        
                        //Copy current resource into results list if it matches criterion
                        Resource tempRes = new Resource(curr_res.getName(), curr_res.getDescription(), curr_res.getTags().clone(), 
                                curr_res.getURI(), curr_res.getChannel(), 
                                curr_res.getOwner().equals("")? "":"*", curr_res.getEZserver());  //owner is never revealed

                        //Send found resource as JSON to client
                        results.addResource(tempRes);
                        result_cnt++;
                    }
                }
                
                //send each resource to client
                for (Resource res: results.getResList()){
                    output.writeUTF(this.Resource2JSONObject(res).toJSONString());
                }
                
                //Send number of results found to server
                JSONObject result_size = new JSONObject();
                result_size.put("resultSize", result_cnt);
                output.writeUTF(result_size.toJSONString());
                
            } catch (Exception e) { //invalid resourceTemplate
                JSONObject inv_res = new JSONObject();
                inv_res.put("response", "error");
                inv_res.put("errorMessage", "invalid resourceTemplate");
                output.writeUTF(inv_res.toJSONString());
            }            
        } else {
            // Missing resource template error - send to client
            JSONObject error = new JSONObject();
            error.put("response", "error");
            error.put("errorMessage", "missing resourceTemplate");
            output.writeUTF(error.toJSONString());
        }        
    }

    private ResourceList propagateQuery(JSONObject client_request) {
        // TODO Propagate query if relay is true
        return null;
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
	
	//TODO this needs to throw an error if JSON is NOT a proper resource i.e. does not contain the required fields
	private Resource JSONObj2Resource(JSONObject resource) throws serverException {
		//handle default value here
		String Name = resource.containsKey("name") ? (String) resource.get("name") : "";
		if (Name.contains("\0")) {
			throw new serverException("Invalid resource");
		}
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		if (Description.contains("\0")) {
			throw new serverException("Invalid resource");
		}
		
		//TODO String[] Tags is different deal with it later - unsure about check
		JSONArray[] Tags = new JSONArray[0];
		if (Tags.length == 0) {
			throw new serverException("Invalid resource");
		}
		
		//TODO When to check if URI is unique or not for a given channel?
		String URI = resource.containsKey("uri") ? (String) resource.get("uri") : "";
		if (URI.contains("\0")) {
			throw new serverException("Invalid resource");
		}
		
		try {
			URI uri = new URI(URI);
		}
		catch (URISyntaxException e) {
			throw new serverException("Invalid resrouce");
		}
		
		
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		if (Channel.contains("\0")) {
			throw new serverException("Invalid resource");
		}
		
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		if ((Owner.contains("\0")) || (Owner.contains("*"))) {
			throw new serverException("Invalid resource");
		}
		
		//TODO Store this server's server:port info - system supplied
		String EZserver = resource.containsKey("ezserver") ? (String) resource.get("ezserver") : "";
		if (EZserver.contains("\0")) {
			throw new serverException("Invalid resource");
		}
		
		//TODO problem with Resource class causing error
		return new Resource(Name, Description, Tags, URI, Channel, Owner, EZserver);
	}
	
	@SuppressWarnings("unchecked")
    private JSONObject Resource2JSONObject(Resource resource) {
        JSONObject jobj = new JSONObject();
	    
	    //handle default value here
        jobj.put("name", resource.getName());
        jobj.put("description", resource.getDescription());
        
        JSONArray tag_list = new JSONArray();
        for (String tag: resource.getTags()){
            tag_list.add(tag);
        }
        //TODO should value for "tags" be a string or is a JSONArray okay?
        jobj.put("tags", tag_list);
        
        jobj.put("uri", resource.getURI());
        jobj.put("channel", resource.getChannel());
        jobj.put("owner", resource.getOwner());
        jobj.put("ezserver", resource.getEZserver());
    
        return jobj;
	}
}

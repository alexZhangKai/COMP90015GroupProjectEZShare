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
					reply.put("response", "error");
					reply.put("errorMessage", "invalid command");
					output.writeUTF(reply.toJSONString());
				}
			}
			else {
			    JSONObject reply = new JSONObject();
			    reply.put("response", "error");
                reply.put("errorMessage", "missing or incorrect type for command");
                output.writeUTF(reply.toJSONString());
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}		
	}

	private void exchange(JSONObject client_request, DataOutputStream output) {
        // TODO Exchange method
        
    }

    @SuppressWarnings("unchecked")
    private void query(JSONObject client_request, DataOutputStream output) throws IOException {                
        if (client_request.containsKey("resourceTemplate")) {
            try {
                Resource in_res;
                // The following should throw an invalid resource exception
                in_res = this.JSONObj2Resource((JSONObject) client_request.get("resourceTemplate"));
                
                // If there was a resourceTemplate AND the template checked out as a valid Resource, report success
                JSONObject response = new JSONObject();
                response.put("response", "success");
                output.writeUTF(response.toJSONString());
                
                int result_cnt = 0;
                
                //get results from other servers first if relay == true
                ResourceList results = new ResourceList();
                if (client_request.containsKey("relay")) {
                    if ((Boolean)client_request.get("relay")) {
                        results = propagateQuery(client_request);
                        result_cnt += results.getSize();
                    }
                }
                
                //Query current resource list for resources that match the template
                for (Resource curr_res: resourceList.getResList()){
                    if (in_res.getChannel().equals(curr_res.getChannel()) && 
                            in_res.getOwner().equals(curr_res.getOwner()) &&
                            in_res.getTags().equals(curr_res.getTags()) &&
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
	
	//TODO this needs to throw an error if JSON is NOT a proper resource i.e. does not contain the required fields
	private Resource JSONObj2Resource(JSONObject resource) {
		//handle default value here
		String Name = resource.containsKey("name") ? (String) resource.get("name") : "";
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		
		//TODO String[] Tags is different deal with it later
		String[] Tags = new String[0];
		
		//TODO When to check if URI is unique or not for a given channel?
		String URI = resource.containsKey("uri") ? (String) resource.get("uri") : "";
		
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		
		//TODO Store this server's server:port info - system supplied
		String EZserver = resource.containsKey("ezserver") ? (String) resource.get("ezserver") : "";
		
		return new Resource(Name, Description, Tags, URI, Channel, Owner, EZserver);
	}
	
	@SuppressWarnings("unchecked")
    private JSONObject Resource2JSONObject(Resource resource) {
        JSONObject jobj = new JSONObject();
	    
	    //handle default value here
        jobj.put("name", resource.getName());
        jobj.put("description", resource.getDescription());
        jobj.put("name", resource.getName());
        
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

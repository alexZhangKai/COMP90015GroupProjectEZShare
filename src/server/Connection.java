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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Connection implements Runnable {
	private static final int NUM_SEC = 2;
    private int id;
	private Socket client;
	private ResourceList resourceList;
	private ServerList serverList;
	private String serverSecret;
	private Boolean debug;
	private String hostname;
	private int port;
	
	public Connection(CommandLine cmd, int id, Socket client, ResourceList resourceList, ServerList serverList) {
		this.id = id;
		this.client = client;
		this.resourceList = resourceList;
		this.serverList = serverList;
		this.serverSecret = cmd.getOptionValue("secret");
		this.hostname = cmd.getOptionValue("advertisedhostname");
		this.debug = cmd.hasOption("debug") ? true : false;
		this.port = Integer.parseInt(cmd.getOptionValue("port"));
	}
	
	@SuppressWarnings("unchecked")
	//JSONObject extends HashMap but does not have type parameters as HashMap would expect...
	
    @Override
	public void run() {
		try {
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());			
			JSONParser parser = new JSONParser();
			
			JSONObject client_request = (JSONObject) parser.parse(input.readUTF());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - RECEIVED: " + client_request);
            }
			
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
				if (debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
                }
			}	
		} catch (IOException e) {
      e.printStackTrace();
		} catch (ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing or incorrect type for command");
			try {
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				output.writeUTF(reply.toJSONString());
				if (debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
                }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}		
	}

	@SuppressWarnings("unchecked")
	private void exchange(JSONObject client_request, DataOutputStream output) throws IOException {
		JSONArray newServerList = new JSONArray();
		try{
			JSONParser parser = new JSONParser();
			newServerList = (JSONArray) parser.parse(client_request.get("serverList").toString());
		} catch (ClassCastException | ParseException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing or invaild server list");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		}
		try {
			this.serverList.update(newServerList, hostname, port);
			JSONObject reply = new JSONObject();
	        reply.put("response", "success");
	        output.writeUTF(reply.toJSONString());
	        if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch (NumberFormatException | ClassCastException | UnknownHostException | serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "invaild server record");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
			e.printStackTrace();
		}
    }

    @SuppressWarnings("unchecked")
    private void query(JSONObject client_request, DataOutputStream output) throws IOException {                

    	if (client_request.containsKey("resourceTemplate")) {
            try {
                Resource in_res;
                JSONParser parser = new JSONParser();
                
                // The following should throw an invalid resource exception
                in_res = this.JSONObj2Resource((JSONObject) parser.parse(client_request.get("resourceTemplate").toString()));
                
                // If there was a resourceTemplate AND the template checked out as a valid Resource, report success
                JSONObject response = new JSONObject();
                response.put("response", "success");
                output.writeUTF(response.toJSONString());
                if (debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + response.toJSONString());
                }
                int result_cnt = 0;
                
                //get results from other servers first if relay == true
                ResourceList results = new ResourceList();
                if (client_request.containsKey("relay")) {
<<<<<<< HEAD
                	//do propagate when there is a server in the server list
                    if ((Boolean)client_request.get("relay") && serverList.getLength() > 0) {
                        results = propagateQuery(client_request, in_res);
=======
                    //only propagate when there are other servers in the list
                    if ((Boolean)client_request.get("relay") && serverList.getLength() > 0) {
                        results = propagateQuery(client_request);
>>>>>>> master
                        result_cnt += results.getSize();
                    }
                }
                //TODO something wrong with the conditions, there is no channel value in relay, cannot match
                //Query current resource list for resources that match the template
                for (Resource curr_res: resourceList.getResList()){
                    if (in_res.getChannel().equals(curr_res.getChannel()) && 
                            in_res.getOwner().equals(curr_res.getOwner()) &&
                            compareTags(in_res.getTags(), curr_res.getTags()) &&
                            compareUri(in_res.getUri(), curr_res.getUri()) &&
                                (curr_res.getName().contains(in_res.getName()) ||
                                curr_res.getDescription().contains(in_res.getDescription()))) {
                        
                        //Copy current resource into results list if it matches criterion
                        Resource tempRes = new Resource(curr_res.getName(), curr_res.getDescription(), curr_res.getTags(), 
                                curr_res.getUri(), curr_res.getChannel(), 
                                curr_res.getOwner().equals("")? "":"*", curr_res.getEZserver());  //owner is never revealed

                        //Send found resource as JSON to client
                        results.addResource(tempRes);
                        result_cnt++;
                    }
                }
                
                //send each resource to client
                for (Resource res: results.getResList()){
                    JSONObject resource_temp = this.Resource2JSONObject(res); 
                    output.writeUTF(resource_temp.toJSONString());
                    if (debug) {
                        System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + resource_temp.toJSONString());
                    }
                }
                
                //Send number of results found to server
                JSONObject result_size = new JSONObject();
                result_size.put("resultSize", result_cnt);
                output.writeUTF(result_size.toJSONString());
                if (debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + result_size.toJSONString());
                }
                
            } catch (Exception e) { //invalid resourceTemplate
                JSONObject inv_res = new JSONObject();
                inv_res.put("response", "error");
                inv_res.put("errorMessage", "invalid resourceTemplate");
                output.writeUTF(inv_res.toJSONString());
                if (debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + inv_res.toJSONString());
                }
            }            
        } else {
            // Missing resource template error - send to client
            JSONObject error = new JSONObject();
            error.put("response", "error");
            error.put("errorMessage", "missing resourceTemplate");
            output.writeUTF(error.toJSONString());
            if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + error.toJSONString());
            }
        }      
    }

    //Compare the two URIs for Query purposes. If the incoming URI has no host i.e. is empty, it should match all URIs
    //...otherwise only an exact match is acceptable
    private boolean compareUri(URI in_uri, URI curr_uri) {
        if (in_uri.getHost() == null) {
            return true;
        } else {
            return in_uri.equals(curr_uri);
        }
    }

    private boolean compareTags(List<String> in_res, List<String> curr_res) {
        if (in_res.size() == 0 && curr_res.size() == 0) {
            return true;
        }
        else if (in_res.size() == 0 && curr_res.size() != 0){
            return false;
        }
        else if (in_res.size() != 0 && curr_res.size() == 0){
            return false;
        }
        else {
            for (String tag: in_res){
                if (!curr_res.contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

<<<<<<< HEAD
    @SuppressWarnings({ "unused", "unchecked" })
    //TODO I dont find you use in_res in this method
    private ResourceList propagateQuery(JSONObject client_request, Resource in_res) throws ParseException, UnknownHostException, IOException, serverException {
=======
    @SuppressWarnings("unchecked")
    private ResourceList propagateQuery(JSONObject client_request) throws ParseException, UnknownHostException, IOException, serverException {
>>>>>>> master
        ResourceList prop_results = new ResourceList();
        
        //construct the right query
        JSONParser parser = new JSONParser();
        JSONObject res = (JSONObject) parser.parse(client_request.get("resourceTemplate").toString());
        JSONObject command = new JSONObject();
        
        //remove owner and channel and set to "" + relay = false
        command.put("command", "QUERY");
        command.put("relay", false);
            res.put("owner", "");
            res.put("channel", "");
        command.put("resourceTemplate", res);

        //TODO implement this as threads, instead of sequential
        //for each server from server list
        JSONArray serv_list = serverList.getServerList();
        for (int i = 0; i < serv_list.size(); i++) {

            //Get server details
            JSONObject server = (JSONObject)serv_list.get(i);
<<<<<<< HEAD
            String hostname = (String)server.get("hostname");
=======
            String hostname = (String) server.get("hostname");
>>>>>>> master
            int port = Integer.parseInt((String) server.get("port"));            
            
            //Send QUERY command to that server
            Socket socket = new Socket(hostname, port);
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            
            //record start time
            long startTime = System.currentTimeMillis();
            
            //send request
            output.writeUTF(command.toJSONString());
            if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + command.toJSONString());
            }
            output.flush();
                        
            while(true) {
                if(input.available() > 0) {
                    //get results and store in results list
                    JSONObject temp_response = (JSONObject) parser.parse(input.readUTF());
                    if (debug) {
                        System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - RECEIVED: " + temp_response);
                    }
                    if (temp_response.containsKey("uri")) {
                        Resource temp_res = this.JSONObj2Resource(temp_response);
                        prop_results.addResource(temp_res);
                    }
                }
                //TODO set it large when debug like 100 times more
                if ((System.currentTimeMillis() - startTime) > NUM_SEC*1000){
                    break;
                }
            }
            socket.close();
        }               
        return prop_results;
    }

    @SuppressWarnings("unchecked")
    private void share(JSONObject client_req, DataOutputStream output) throws IOException {
    	JSONParser parser = new JSONParser();
		try{
			//throw class cast exception for missing resource and\/or secret
			JSONObject resourceJSON = (JSONObject) parser.parse(client_req.get("resource").toString());
			if(!client_req.containsKey("secret")) throw new ClassCastException();
			
			//throw server exception invalid resource
			Resource resource = JSONObj2Resource(resourceJSON);
			
			URI uri = resource.getUri();
			//check if URI is provided
			if(uri.toString().equals("")) throw new serverException("invalid resource");
			
			//check if URI is a file scheme
//			boolean isFile = "file".equalsIgnoreCase(uri.getScheme());
//			if(!isFile) throw new serverException("invalid resource");
			
			//check if the file exist
			//TODO uncomment when submit
			//File f = new File(uri.toString());
			//if(!f.exists()) throw new serverException("invalid resource");
			
			//check if secret is equal
			if(!client_req.get("secret").equals(this.serverSecret)) throw new serverException("incorrect secret");
			
			//throw cannot publish resource
			resourceList.addResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource and/or secret");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			if(e.toString().equals("connot publish resource")) {
				reply.put("errorMessage", "cannot share resource");
			} else {
				reply.put("errorMessage", e.toString());
			}
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		}
    }

    @SuppressWarnings("unchecked")
    private void publish(JSONObject client_req, DataOutputStream output) throws IOException {
    	JSONParser parser = new JSONParser();
		try{
			//throw class cast exception for missing resource
			JSONObject resourceJSON = (JSONObject) parser.parse(client_req.get("resource").toString());
			
			//throw server exception invalid resource
			Resource resource = JSONObj2Resource(resourceJSON);
			
			URI uri = resource.getUri();
			//check if URI is provided
			if(uri.toString().equals("")) throw new serverException("invalid resource");
			
			//check if URI is a URL
			boolean isWeb = "http".equalsIgnoreCase(uri.getScheme())
				    || "https".equalsIgnoreCase(uri.getScheme());
			if(!isWeb) throw new serverException("invalid resource");
			
			//throw cannot publish resource
			resourceList.addResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		}
	}
	
	@SuppressWarnings("unchecked")
    private void remove(JSONObject client_req, DataOutputStream output) throws IOException {
		JSONParser parser = new JSONParser();
		try{
			//throw class cast exception for missing resource
			JSONObject resourceJSON = (JSONObject) parser.parse(client_req.get("resource").toString());
			
			//throw server exception invalid resource
			Resource resource = JSONObj2Resource(resourceJSON);
			
			//throw server exception cannot remove resource
			resourceList.removeResource(resource);
			
			//create a reply
			JSONObject reply = new JSONObject();
			reply.put("response", "success");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resource");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch(serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", e.toString());
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		}
	}
	
	@SuppressWarnings("unchecked")
    private void fetch(JSONObject client_req, DataOutputStream output) throws IOException{
		JSONParser parser = new JSONParser();
		try{
			//throw class cast exception for missing resource
			JSONObject resourceJSON = (JSONObject) parser.parse(client_req.get("resourceTemplate").toString());
			
			//throw server exception invalid resource
			Resource resourceTemplate = JSONObj2Resource(resourceJSON);
		
			Resource match = resourceList.queryForChannelURI(resourceTemplate);
		
			//handle no match error
			if(match == null) {
				JSONObject reply = new JSONObject();
				reply.put("response", "error");
				reply.put("errorMessage", "no match resource");
				output.writeUTF(reply.toJSONString());
				if (debug) {
	                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
	            }
				
				//TODO Deal with this 'return'?
				return;
			}
		
			//Use a known URI, need to check the file afterward ? 
			URI uri = match.getUri();
			File f = new File(uri.getHost() + uri.getPath());
			if(f.exists()) {
				JSONObject response = new JSONObject();
				response.put("response", "success");
				output.writeUTF(response.toJSONString());
				if (debug) {
	                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + response.toJSONString());
	            }
			
				RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			
				JSONObject resource = Resource2JSONObject(match);
				resource.put("resourceSize", byteFile.length());
				output.writeUTF(resource.toJSONString());
				if (debug) {
	                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + resource.toJSONString());
	            }
			
				byte[] sendingBuffer = new byte[1024*1024];
				int num;
				while((num = byteFile.read(sendingBuffer)) > 0) {
					output.write(Arrays.copyOf(sendingBuffer, num));
				}
				byteFile.close();
			
				JSONObject resultFile = new JSONObject();
				resultFile.put("resultSize", 1);
				output.writeUTF(resultFile.toJSONString());
				if (debug) {
	                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + resultFile.toJSONString());
	            }
			}
		} catch (ParseException | ClassCastException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			reply.put("errorMessage", "missing resourceTemplate");
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch (serverException e) {
			JSONObject reply = new JSONObject();
			reply.put("response", "error");
			if(e.toString().equals("invalid resource")) {
				reply.put("errorMessage", "invalid resourceTemplate");
			} else {
				reply.put("errorMessage", e.toString());
			}
			output.writeUTF(reply.toJSONString());
			if (debug) {
                System.out.println(new Timestamp(System.currentTimeMillis())+" - [DEBUG] - SENT: " + reply.toJSONString());
            }
		} catch (Exception e){
		    e.printStackTrace();
		}
	}
	
	//TODO this needs to throw an error if JSON is NOT a proper resource i.e. does not contain the required fields
	//TODO Also check for null values when key exists!
	private Resource JSONObj2Resource(JSONObject resource) throws serverException {
		//handle default value here
		String Name = resource.containsKey("name") ? (String) resource.get("name") : "";
		if (Name.contains("\\0")) {
			throw new serverException("Invalid resource");
		}
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		if (Description.contains("\\0")) {
			throw new serverException("Invalid resource");
		}
		
		//TODO String[] Tags is different deal with it later - unsure about check
		JSONArray tags_jarr = resource.containsKey("tags")? (JSONArray) resource.get("tags") : null;
		List<String> tags_slist = new ArrayList<String>();
		
		if (tags_jarr.size() != 0) {
		    for (Object tag: tags_jarr){
		        tags_slist.add((String) tag);
		    }
		}
		
		
		//TODO When to check if URI is unique or not for a given channel?
		String uri_s = resource.containsKey("uri") ? (String) resource.get("uri") : "";
		if (uri_s.contains("\\0")) {
			throw new serverException("Invalid resource");
		}
		URI uri;
		try {
			uri = new URI(uri_s);
		}
		catch (URISyntaxException e) {
			throw new serverException("Invalid resrouce");
		}
		
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		if (Channel.contains("\\0")) {
			throw new serverException("Invalid resource");
		}
		
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		if (Owner.contains("\\0") || Owner.contains("*")) {
			throw new serverException("Invalid resource");
		}
		String EZserver = hostname+":"+port;
		
		return new Resource(Name, Description, tags_slist, uri, Channel, Owner, EZserver);
	}
	
	@SuppressWarnings("unchecked")
    private JSONObject Resource2JSONObject(Resource resource) {
        JSONObject jobj = new JSONObject();

        jobj.put("name", resource.getName());
        jobj.put("description", resource.getDescription());
        
        JSONArray tag_list = new JSONArray();
        for (String tag: resource.getTags()){
            tag_list.add(tag);
        }
        //TODO should value for "tags" be a string or is a JSONArray okay?
        jobj.put("tags", tag_list);
        
        jobj.put("uri", resource.getUri().toString());
        jobj.put("channel", resource.getChannel());
        jobj.put("owner", resource.getOwner());
        jobj.put("ezserver", resource.getEZserver());
    
        return jobj;
	}
}

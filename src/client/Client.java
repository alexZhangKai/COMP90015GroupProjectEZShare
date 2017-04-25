/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 *
 * Client-side application
 */

package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Client {
    private static String ip;
    private static int port;
    private static final int NUM_SEC = 3;
    private static final Map<String, Boolean> argOptions;
    static{
        argOptions = new HashMap<>();
        argOptions.put("channel", true);
        argOptions.put("debug", false);
        argOptions.put("description", true);
        argOptions.put("exchange", false);
        argOptions.put("fetch", false);
        argOptions.put("host", true);
        argOptions.put("name", true);
        argOptions.put("owner", true);
        argOptions.put("port", true);
        argOptions.put("publish", false);
        argOptions.put("query", false);
        argOptions.put("remove", false);
        argOptions.put("secret", true);
        argOptions.put("servers", true);
        argOptions.put("share", false);
        argOptions.put("tags", true);
        argOptions.put("uri", true);
        argOptions.put("invalidComm", false);
        argOptions.put("missingComm", false);
    }
    private static Boolean debug = false;
    
    public static void main(String[] args) {
        System.out.println("Client has started.");

        //Parse input arguments
        Options initOptions = new Options();
        for (String option: argOptions.keySet()){
            initOptions.addOption(option, argOptions.get(option), option);
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine initCmd = null;
        
        try {
            initCmd = parser.parse(initOptions, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (initCmd.hasOption("port") && initCmd.hasOption("host")) {
            port = Integer.parseInt(initCmd.getOptionValue("port"));
            ip = initCmd.getOptionValue("host");
        } else {
            System.out.println("Please provide IP and PORT options");
            System.exit(0);
        }
        if (initCmd.hasOption("debug")) {
            debug = true;
        }
        
        //Decipher command and call respective method
        if (initCmd.hasOption("publish")) {
            Client.PublishCmd(initCmd);
        } else if (initCmd.hasOption("remove")) {
            Client.RemoveCmd(initCmd);
        } else if (initCmd.hasOption("share")) {
            Client.ShareCmd(initCmd);
        } else if (initCmd.hasOption("query")) {
            Client.QueryCmd(initCmd);
        } else if (initCmd.hasOption("fetch")) {
            Client.FetchCmd(initCmd);
        } else if (initCmd.hasOption("exchange")) {
            Client.ExchangeCmd(initCmd);
        } else if (initCmd.hasOption("invalidComm")) {
            Client.InvalidCmd();
        } else if (initCmd.hasOption("missingComm")) {
            Client.MissingCmd();
        } else {
            System.out.println("Please use valid arguments.");
        }
    }
     
    @SuppressWarnings("unchecked")
    private static void MissingCmd() {
        JSONObject jobj = new JSONObject();
        jobj.put("uwotm8", "blah");
        Client.generalReply(jobj.toString());
    }
        
    @SuppressWarnings("unchecked")
    private static void InvalidCmd() {
        JSONObject jobj = new JSONObject();
        jobj.put("command", "blah");
        Client.generalReply(jobj.toString());   
    }
    
    @SuppressWarnings("unchecked")
	private static void ExchangeCmd(CommandLine initCmd) {
    	JSONObject command = new JSONObject();
    	if(!initCmd.hasOption("servers")) return;
    	String[] serversArr = initCmd.getOptionValue("servers").split(",");
    	JSONArray servers = new JSONArray();
    	for(String server : serversArr) {
    		String[] hostAndPort = server.split(":");
    		JSONObject ele = new JSONObject();
    		ele.put("host", hostAndPort[0]);
    		ele.put("port", hostAndPort[1]);
    		servers.add(ele);
    	}
    	command.put("command", "EXCHANGE");
    	command.put("serverList", servers.toJSONString());
    	generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    public static void ShareCmd(CommandLine initCmd) {
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        JSONObject resource = createResJSONObj(initCmd);
        if(resource == null) return;
        if(!initCmd.hasOption("secret")) return;
        String secret = initCmd.getOptionValue("secret");
        command.put("command", "SHARE");
        command.put("secret", secret);
        command.put("resource", resource.toJSONString());
        generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    private static void QueryCmd(CommandLine initCmd) {
        JSONObject command = new JSONObject();
        
        //TODO Likely cannot use this method to construct the template as it may NOT have a URI
        JSONObject resourceTemplate = createResJSONObj(initCmd);
        
        //TODO remove these returns in favour of proper error handling.
        if(resourceTemplate == null) return;
        
        command.put("command", "QUERY");
        command.put("relay", true);
        command.put("resourceTemplate", resourceTemplate.toJSONString());
        generalReply(command.toJSONString());
    }

    public static void generalReply(String request) {
    	try (Socket socket = new Socket(ip, port)){
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            //record start time
            long startTime = System.currentTimeMillis();
            
            //send request
            output.writeUTF(request);
            if (debug) {
                System.out.println("[SENT]: " + request);
            }
            output.flush();
                        
            while(true) {
            	if(input.available() > 0) {
            		System.out.println(input.readUTF());
            	}
            	if ((System.currentTimeMillis() - startTime) > NUM_SEC*1000){
            		break;
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")  
    private static JSONObject createResJSONObj(CommandLine initCmd) {
    	JSONObject resource = new JSONObject();
    	
    	//TODO There will not ALWAYS be a URI e.g. in the case of Query
//        if(!initCmd.hasOption("uri")) {
//        	System.out.println("Provide URI for publish.");
//        	return null;
//        }
       	String uri = initCmd.hasOption("uri")? initCmd.getOptionValue("uri") : "";
        String name = initCmd.hasOption("name") ? initCmd.getOptionValue("name") : "";
        
        JSONArray tag_list = new JSONArray();
        if (initCmd.hasOption("tags")) {
            String[] tags_arr = initCmd.getOptionValue("tags").split(",");
            tag_list = new JSONArray();
            for (String tag: tags_arr){
                tag_list.add(tag);
            }
            //tags = tag_list.toJSONString();
        }
        
        String description = initCmd.hasOption("description") ? initCmd.getOptionValue("description") : "";
        String channel = initCmd.hasOption("channel") ? initCmd.getOptionValue("channel") : "";
        String owner = initCmd.hasOption("owner") ? initCmd.getOptionValue("owner") : "";
                
        resource.put("name", name);            
        resource.put("tags", tag_list);
        resource.put("description", description);
        resource.put("uri", uri);
        resource.put("channel", channel);
        resource.put("owner", owner);
        resource.put("ezserver", null);
        
        return resource;
    }
    
    @SuppressWarnings("unchecked")  
    public static void PublishCmd(CommandLine initCmd) {
        //Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        JSONObject resource = createResJSONObj(initCmd);
        if(resource == null) return;
        command.put("command", "PUBLISH");
        command.put("resource", resource.toJSONString());
        generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    public static void RemoveCmd(CommandLine initCmd) {
        JSONObject command = new JSONObject();
        JSONObject resource = createResJSONObj(initCmd);
        if(resource == null) return;
        command.put("command", "REMOVE");
        command.put("resource", resource.toJSONString());
        generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    public static void FetchCmd(CommandLine initCmd) {
    	JSONObject command = new JSONObject();
    	JSONObject resourceTemplate = createResJSONObj(initCmd);
        if(resourceTemplate == null) return;
        command.put("command", "FETCH");
        command.put("resourceTemplate", resourceTemplate.toJSONString());
        
        try (Socket socket = new Socket(ip, port)){
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            
            //record start time
            long start = System.currentTimeMillis();
            
            //send request
            output.writeUTF(command.toJSONString());
            System.out.println("request sent");
            output.flush();
            
            JSONParser JSONparser = new JSONParser();
            
            while(true) {
            	if(input.available() > 0) {
            		String result = input.readUTF();
            		JSONObject reply = (JSONObject) JSONparser.parse(result);
            		//the first reply: response
            		if(reply.containsKey("response")) {
            			if(reply.get("response").equals("success")) {
            				continue;
            			} else {
            				System.out.println(reply.toJSONString());
            				break;
            			}
            		}
            		//the second reply: resource and file
            		if(reply.containsKey("resourceSize")) {
            			Long fileSizeRemaining = (Long) reply.get("resourceSize");            			
            			int chunkSize = setChunkSize(fileSizeRemaining);            			
            			byte[] receiveBuffer = new byte[chunkSize];
            			String fileName = "clientFile/test.png";
            			RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");
            			//remaining file size
            			int num;
            			
            			System.out.println("Downloading file of size" + fileSizeRemaining);
            			while((num = input.read(receiveBuffer)) > 0) {
            				downloadingFile.write(Arrays.copyOf(receiveBuffer, num));
            				fileSizeRemaining -= num;
            				
            				chunkSize = setChunkSize(fileSizeRemaining);
            				receiveBuffer = new byte[chunkSize];
            				
            				if(fileSizeRemaining == 0){
            					break;
            				}
            			}
            			System.out.println("File received!");
            			downloadingFile.close();
            		}
            		
            		//the last reply: resultSize
            		if(reply.containsKey("resultSize")) {
            			System.out.println(reply.toJSONString());
            		}
            		
            		//connection timeout
            		if((System.currentTimeMillis() - start) > 5*1000) {
            			break;
            		}
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static int setChunkSize(long fileSizeRemaining) {
    	int chunkSize = 1024*1024;
    	
    	if(fileSizeRemaining < chunkSize) {
    		chunkSize = (int) fileSizeRemaining;
    	}
    	
    	return chunkSize;
    }
}
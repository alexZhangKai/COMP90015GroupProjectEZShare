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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Client {
    private static String ip;
    private static int port;
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
    }
    
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
        } else {
            System.out.println("Please use valid arguments.");
        }
    }
     
    private static void ExchangeCmd(CommandLine initCmd) {
        // TODO Exchange command
        
    }

    private static void QueryCmd(CommandLine initCmd) {
        // TODO Query command
        
    }

    @SuppressWarnings("unchecked")
    //JSONObject extends HashMap but does not have type parameters as HashMap would expect...
    public static void ShareCmd(CommandLine initCmd) {
        //TODO Share command, finalize and share test code
        
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resource = new JSONObject();
        
        if(!initCmd.hasOption("uri")){
        	return;
        }
        else{
        	String uri = initCmd.getOptionValue("uri");
        	resource.put("uri", uri);
        }
        String secret = initCmd.hasOption("secret") ? initCmd.getOptionValue("secret") : "";
        String name = initCmd.hasOption("name") ? initCmd.getOptionValue("name") : "";
        String description = initCmd.hasOption("description") ? initCmd.getOptionValue("description") : "";
        String channel = initCmd.hasOption("channel") ? initCmd.getOptionValue("channel") : "";
        String owner = initCmd.hasOption("owner") ? initCmd.getOptionValue("owner") : "";
        
        String[] tags = {"tag1", "tag2"};
        resource.put("name", name);            
       /* resource.put("tags", tags.toString());*/
        resource.put("description", description);
        
        resource.put("channel", channel);
        resource.put("owner", owner);
        resource.put("ezserver", null);
        //create a test publish command
        command.put("command", "SHARE");
        command.put("resource", resource.toJSONString());
        
        generalReply(command.toJSONString());
    }

    public static void generalReply(String request) {
        //TODO is this still needed?
    	try (Socket socket = new Socket(ip, port)){
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            //send request
            output.writeUTF(request);
            System.out.println("request sent");
            output.flush();
                        
            while(true) {
            	if(input.available() > 0) {
            		System.out.println(input.readUTF());
            		break;
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @SuppressWarnings("unchecked")  
    //JSONObject extends HashMap but does not have type parameters as HashMap would expect...
    public static void PublishCmd(CommandLine initCmd) {
        //TODO Publish command, remove testing code
        
        //Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resource = new JSONObject();
        if(!initCmd.hasOption("uri")){
        	return;
        }
        else{
        	String uri = initCmd.getOptionValue("uri");
        	resource.put("uri", uri);
        }
        String name = initCmd.hasOption("name") ? initCmd.getOptionValue("name") : "";
        String description = initCmd.hasOption("description") ? initCmd.getOptionValue("description") : "";

        String channel = initCmd.hasOption("channel") ? initCmd.getOptionValue("channel") : "";
        String owner = initCmd.hasOption("owner") ? initCmd.getOptionValue("owner") : "";
        
        String[] tags = {"tag1", "tag2"};// confused about it
        
        resource.put("name", name);            
        resource.put("tags", tags.toString());
        resource.put("description", description);
        resource.put("channel", channel);
        resource.put("owner",owner );
        resource.put("ezserver", null);
        //create a test publish command
        command.put("command", "PUBLISH");
        command.put("resource", resource.toJSONString());
        
        generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    //JSONObject extends HashMap but does not have type parameters as HashMap would expect...
    public static void RemoveCmd(CommandLine initCmd) {
        //TODO Remove command, finalize and remove test code
        
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resource = new JSONObject();
        
        if(!initCmd.hasOption("uri")){
        	return;
        }
        else{
        	String uri = initCmd.getOptionValue("uri");
        	resource.put("uri", uri);
        }
        
        String[] tags = {"tag1", "tag2"};
        resource.put("name", "");            
        resource.put("tags", "");
        resource.put("description", "");

        resource.put("channel", "");
        resource.put("owner", "");
        resource.put("ezserver", null);
        //create a test publish command
        command.put("command", "REMOVE");
        command.put("resource", resource.toJSONString());
        
        generalReply(command.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
    //JSONObject extends HashMap but does not have type parameters as HashMap would expect...
    public static void FetchCmd(CommandLine initCmd) {
        //TODO Fetch command, remove test code and finalise
        
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resourceTemplate = new JSONObject();
        String[] tags = {"tag1", "tag2"};
        resourceTemplate.put("name", "testName");            
        resourceTemplate.put("tags", tags.toString());
        resourceTemplate.put("description", "testDescription");
        resourceTemplate.put("uri", initCmd);
        resourceTemplate.put("channel", "testChannel");
        resourceTemplate.put("owner", "");
        resourceTemplate.put("ezserver", null);
        //create a test publish command
        command.put("command", "FETCH");
        command.put("resourceTemplate", resourceTemplate.toJSONString());
        
        try (Socket socket = new Socket(ip, port)){
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
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
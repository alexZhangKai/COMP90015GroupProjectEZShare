package client;

/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 */

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
import java.util.Scanner;

class Client {
    private static String ip;
    private static int port;
    
    public static void main(String[] args) {
        System.out.println("Client has started.");

        //Parse input arguments
        //TODO Add all arguments for client
        Options initOptions = new Options();
        initOptions.addOption("PORT", true, "Server port");
        initOptions.addOption("IP", true, "Server IP address");
        CommandLineParser parser = new DefaultParser();
        CommandLine initCmd = null;
        
        try {
            initCmd = parser.parse(initOptions, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (initCmd.hasOption("PORT") && initCmd.hasOption("IP")) {
            port = Integer.parseInt(initCmd.getOptionValue("PORT"));
            ip = initCmd.getOptionValue("IP");
        } else {
            System.out.println("Please provide IP and PORT options");
            System.exit(0);
        }       

//------TESTING-------        
//TODO Move arguments from cmd to input
        
        //read console command        
        Options cmdOptions = new Options();
        cmdOptions.addOption("publish", false, "Publish command");
        cmdOptions.addOption("remove", false, "Remove command");
        cmdOptions.addOption("fetch", false, "Fetch command");
        cmdOptions.addOption("exchange", false, "Exchange command");
        cmdOptions.addOption("uri", false, "Resource URI");
        
        cmdOptions.addOption("exit", false, "Exit command");

        //TODO Remove scanner object for client
        Scanner scanner = new Scanner(System.in);
        CommandLine cmdl = null;
        String cmdName;
        String resURI = "";
        
        //Forever parse command line arguments, for testing, until "exit"
        readCMD: while (true) {
        	System.out.println("--------------------------------");
        	System.out.println("Please provide command and args");
        	System.out.println("e.g. -publish");
        	String[] cmdArg = scanner.nextLine().split(" ");
        	try {
				cmdl = parser.parse(cmdOptions, cmdArg);
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	
        	//Parse basic essential command
        	if (cmdl.hasOption("exchange")) {
        		cmdName = "exchange";
        	} else if (cmdl.hasOption("publish")) {
        		cmdName = "publish";
        	} else if (cmdl.hasOption("exit")){
        	    cmdName = "exit";
        	} else if (cmdl.hasOption("remove")) {
                cmdName = "remove";
            }
        	else {
        		System.out.println("Enter valid command e.g. -publish, -exit, etc.");
        		continue;
        	}
        	if (cmdl.hasOption("uri")) {
                resURI = cmdl.getOptionValue("uri");
            }
        	
//------END TESTING-------
        	
        	switch (cmdName) {
            	case "publish":
            		PublishCmd(resURI);
            		break;
            	case "remove":
            		RemoveCmd(resURI);
            		break;
            	case "fetch":
            		FetchCmd(resURI);
            		break;
            	case "exit":
            	//TODO Remove exit when commands are provided as input
            		System.out.println("\nClient terminated.");
            		break readCMD;
        	}
        }
        scanner.close();
    }
     
    public static void generalReply(String request) {
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
    
    public static void PublishCmd(String URI) {
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resource = new JSONObject();
        String[] tags = {"tag1", "tag2"};
        resource.put("name", "testName");            
        resource.put("tags", tags.toString());
        resource.put("description", "testDescription");
        resource.put("uri", URI);
        resource.put("channel", "testChannel");
        resource.put("owner", "");
        resource.put("ezserver", null);
        //create a test publish command
        command.put("command", "PUBLISH");
        command.put("resource", resource.toJSONString());
        
        generalReply(command.toJSONString());
    }
    
    public static void RemoveCmd(String URI) {
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resource = new JSONObject();
        String[] tags = {"tag1", "tag2"};
        resource.put("name", "testName");            
        resource.put("tags", tags.toString());
        resource.put("description", "testDescription");
        resource.put("uri", URI);
        resource.put("channel", "testChannel");
        resource.put("owner", "");
        resource.put("ezserver", null);
        //create a test publish command
        command.put("command", "REMOVE");
        command.put("resource", resource.toJSONString());
        
        generalReply(command.toJSONString());
        
    }
    
    public static void FetchCmd(String URI) {
    	//Create a JSONObject command and send it to server
        JSONObject command = new JSONObject();
        
        //create a test resource
        JSONObject resourceTemplate = new JSONObject();
        String[] tags = {"tag1", "tag2"};
        resourceTemplate.put("name", "testName");            
        resourceTemplate.put("tags", tags.toString());
        resourceTemplate.put("description", "testDescription");
        resourceTemplate.put("uri", URI);
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

/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * Server-side application; relies on Resource, ResourceList and Connection classes
 */

package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ServerSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;

public class Server extends TimerTask {

    private static final int MAX_THREADS = 2;
    private static int connections_cnt = 0;
    private static int port;
    private static ResourceList resourceList = new ResourceList();
    private static ServerList serverList = new ServerList();
    private static HashMap<String, Long> clientIPList = new HashMap<String, Long>();
    private static long intervalLimit = 1*1000;
    
    private static final Map<String, Boolean> argOptions;
    static{
        argOptions = new HashMap<>();
        argOptions.put("advertisedhostname", true);
        argOptions.put("connectionintervallimit", true);
        argOptions.put("exchangeinterval", true);
        argOptions.put("port", true);
        argOptions.put("secret", true);
        argOptions.put("debug", false);
    }
    
    public static void main(String[] args) {
        System.out.println("Server has started.");
        
        //Parse CMD options
        Options options = new Options();
        for (String option: argOptions.keySet()){
            options.addOption(option, argOptions.get(option), option);
        }
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        if (cmd.hasOption("port") && cmd.hasOption("secret")) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        } else {
            System.out.println("Please provide enough options.");
            System.exit(0);
        }
        
        //factory for server sockets
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        
        //Create server socket that auto-closes, and bind to port
        try (ServerSocket server = factory.createServerSocket(port)){
            System.out.println("Waiting for client connection...");
             
            //Set exchange schema
            TimerTask timerTask = new Server();
    		Timer timer = new Timer(true);
    		timer.scheduleAtFixedRate(timerTask, 1, 10*60*1000);
            
            //Keep listening for connections and use a thread pool with 2 threads
            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
            while (true){
                Socket client = server.accept();
                
                //check connection interval time limit
                String clientIP = client.getInetAddress().toString();
                if(clientIPList.containsKey(clientIP)) {
                	if((System.currentTimeMillis() - clientIPList.get(clientIP)) > intervalLimit) {
                		clientIPList.put(clientIP, System.currentTimeMillis());
                	} else {
                		throw new serverException("break connection interval limit");
                	}
                } else {
                	clientIPList.put(clientIP, System.currentTimeMillis());
                }
                
                connections_cnt++;
                System.out.println("Client " + connections_cnt + " requesting connection.");
                
                //Create, and start, a new thread that processes incoming connections
                executor.submit(new Connection(cmd, connections_cnt, client, resourceList, serverList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Send EXCHANGE command every 10 minutes
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if(serverList.getLength() > 0) {
			JSONObject receiver = serverList.select();
			String ip = (String) receiver.get("hostname");
			int port = (int) receiver.get("port");
			try(Socket soc = new Socket(ip, port)){
				DataInputStream input = new DataInputStream(soc.getInputStream());
	            DataOutputStream output = new DataOutputStream(soc.getOutputStream());
	            long startTime = System.currentTimeMillis();
	            JSONObject command = new JSONObject();
	            command.put("command", "EXCHANGE");
	            command.put("serverList", serverList.getServerList());
	            output.writeUTF(command.toJSONString());
	            output.flush();
	            
	            while(true) {
	            	if(input.available() > 0) {
	            		System.out.println(input.readUTF());
	            	}
	            	if ((System.currentTimeMillis() - startTime) > 5*1000){
	            		break;
	            	}
	            }
			} catch (IOException e) {
				serverList.remove(receiver);
				e.printStackTrace();
			}
		}		
	}
	
	/*
	 * don't use this method or we have to make the clientIPList synchronized
	private void updateClientIPList() {
	    //TODO Update client IP list method not used - needs to be implemented?
		Iterator<Entry<String, Long>> it = clientIPList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if((System.currentTimeMillis() - (long)pair.getValue()) > intervalLimit) {
	        	clientIPList.remove(pair.getKey());
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}
	*/
}
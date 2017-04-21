/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * Server-side application; relies on Resource, ResourceList and Connection classes
 */

package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ServerSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Server {

    private static final int MAX_THREADS = 2;
    private static int connections_cnt = 0;
    private static int port;
    private static ResourceList resourceList = new ResourceList();
    
    private static final Map<String, Boolean> argOptions;
	private static ServerSocket socket;
    static{
        argOptions = new HashMap<>();
        argOptions.put("advertisedhostname", true);
        argOptions.put("connectionintervallimit", true);
        argOptions.put("exchangeinterval", true);
        argOptions.put("port", true);
        argOptions.put("secret", true);
        argOptions.put("debug", false);
    }
    
    public static void main(String[] args) throws SocketException {
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
        
        if (cmd.hasOption("port")) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        } else {
            System.out.println("Please provide PORT option.");
            System.exit(0);
        }
        //-----------
        
        //factory for server sockets
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        
        socket = null;
		socket.setSoTimeout(10*1000);
        
        //Create server socket that auto-closes, and bind to port
        try (ServerSocket server = factory.createServerSocket(port)){
            System.out.println("Waiting for client connection...");
            
            //Keep listening for connections and use a thread pool with 2 threads
            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
            while (true){
                Socket client = server.accept();
                connections_cnt++;
                System.out.println("Client " + connections_cnt + " requesting connection.");
                
                //Create, and start, a new thread that processes incoming connections
                executor.submit(new Connection(connections_cnt, client, resourceList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
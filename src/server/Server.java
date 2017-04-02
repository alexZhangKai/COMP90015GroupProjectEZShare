package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class Server {

    private static int counter = 0;     //keep count of number of client connections
    private static int port;
    
    public static void main(String[] args) {
        System.out.println("Server has started.");
        
        //Parse CMD options
        Options options = new Options();
        options.addOption("PORT", true, "Server port");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        if (cmd.hasOption("PORT")) {
            port = Integer.parseInt(cmd.getOptionValue("PORT"));
        } else {
            System.out.println("Please provide PORT option.");
            System.exit(0);
        }
        //-----------
        
        //factory for server sockets
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        
        //Create server socket that auto-closes, and bind to port
        try (ServerSocket server = factory.createServerSocket(port)){
            System.out.println("Waiting for client connection...");
            
            //Keep listening for connections and use a thread pool with 2 threads
            ExecutorService executor = Executors.newFixedThreadPool(2);
            while (true){
                Socket client = server.accept();
                counter++;
                System.out.println("Client " + counter + " requesting connection.");
                
                //Create, and start, a new thread that processes incoming connections
                executor.submit(new Connection(counter, client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
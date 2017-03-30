/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * Client-Server Template
 * AB 
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
            
            //Keep listening for connections forever
            while (true){
                Socket client = server.accept();
                counter++;
                System.out.println("Client " + counter + " requesting connection.");
                
                //Create, and start, a new thread that processes incoming connections
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void serveClient(Socket client) {
        try (Socket clientSocket = client){
            //Assign streams for the client connection
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            
            //Echo whatever the client says, and respond to the client
            System.out.println("Client: " + input.readUTF());
            output.writeUTF("Server: Hi Client " + counter + " .");

        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}
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
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class Client {
    private static String ip;
    private static int port;
    
    public static void main(String[] args) {
        System.out.println("Client has started.");

        //Parse CMD options
        Options options = new Options();
        options.addOption("PORT", true, "Server port");
        options.addOption("IP", true, "Server IP address");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cmd.hasOption("PORT") && cmd.hasOption("IP")) {
            port = Integer.parseInt(cmd.getOptionValue("PORT"));
            ip = cmd.getOptionValue("IP");
        } else {
            System.out.println("Please provide IP and PORT options");
            System.exit(0);
        }       
        //-----------
        
            //Create client socket that auto-closes upon TRY exit
            //...and connect to a server socket
        try (Socket socket = new Socket(ip, port)){
            
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            
            //Write something on outgoing stream and send it through
            output.writeUTF("I want to connect!");
            output.flush();
            
           try {
                 String message = input.readUTF();
                 System.out.println(message);
            } catch (IOException e) {
                System.out.println("Server seems to have closed connection.");
            }
       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

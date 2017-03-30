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

public class Server {

    private static int counter = 0;     //keep count of number of client connections
    private static int port = 3000;
    
    public static void main(String[] args) {
        System.out.println("Server has started.");
        
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
            if (input.available() != 0){
                System.out.println("Client: " + input.readUTF());
                output.writeUTF("Server: Hi Client " + counter + " .");
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}
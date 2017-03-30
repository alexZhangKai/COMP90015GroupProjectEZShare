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
import java.net.Socket;

class Client {

    private static String ip = "localhost";
    private static int port = 3000;
    
    public static void main(String[] args) {
        System.out.println("Client has started.");

            //Create client socket that auto-closes upon TRY exit
            //...and connect to a server socket
        try (Socket socket = new Socket(ip, port)){
            
            //Get I/O streams for connection
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            
            //Write something on outgoing stream and send it through
            output.writeUTF("I want to connect!");
            output.flush();
            
            //Read anything the client may have sent, and print
            if (input.available() != 0) {
                String message = input.readUTF();
                System.out.println(message);
            }       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

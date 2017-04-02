package server;

import java.io.*;
import java.net.*;

public class Connection implements Runnable{

	private int id;
	private Socket client;
	
	public Connection(int id, Socket client) {
		this.id = id;
		this.client = client;
	}
	
	@Override
	public void run() {
		System.out.println("Connection: " + id);
		try {
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			//print client request
			System.out.println("Client said: " + input.readUTF());
			
			Thread.sleep(5000);
			
			output.writeUTF("Server reply: 6666666");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}		
	}

}

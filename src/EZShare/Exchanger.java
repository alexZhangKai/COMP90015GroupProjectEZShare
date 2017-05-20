package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.TimerTask;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Exchanger extends TimerTask{
    private Boolean secure = false;
    
    public Exchanger(int i) {
        secure = i==1;
    }

    //Send EXCHANGE command every 10 minutes
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        ServerList sList = null;
        if (secure){
            sList = ServerListManager.getSecServerList();
            System.out.println("\n" + new Timestamp(System.currentTimeMillis()) 
                    + " - [INFO] - started secure Exchanger\n");
        } else {
            sList = ServerListManager.getUnsecServerList();
            System.out.println("\n" + new Timestamp(System.currentTimeMillis()) 
                    + " - [INFO] - started Exchanger\n");
        }
        
        if (sList.getLength() > 0) {
            //select a random server from the list
            JSONObject receiver = sList.select();
            String ip = (String) receiver.get("hostname");
            int port = Integer.parseInt(receiver.get("port").toString());
            
            //connect to it and exchange list of servers
            try {
                
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslsocket = null;
                Socket socket = null;
                DataInputStream input;
                DataOutputStream output;
                
                if (secure){
                    sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);
                  //Get I/O streams for connection
                    input = new DataInputStream(sslsocket.getInputStream());
                    output = new DataOutputStream(sslsocket.getOutputStream());
                    sslsocket.setSoTimeout(Server.SOCKET_TIMEOUT_MS);
                } else{
                    socket = new Socket(ip, port);
                    input = new DataInputStream(socket.getInputStream());
                    output = new DataOutputStream(socket.getOutputStream());
                    socket.setSoTimeout(Server.SOCKET_TIMEOUT_MS);
                }
                
                JSONObject command = new JSONObject();
                command.put("command", "EXCHANGE");
                
                JSONArray serverArr = sList.getCopyServerList();
                JSONObject host = new JSONObject();
                host.put("hostname", Server.hostname);
                host.put("port", port);
                serverArr.add(host);

                command.put("serverList", serverArr);
                output.writeUTF(command.toJSONString());
                if (Server.debug) {
                    System.out.println(new Timestamp(System.currentTimeMillis())
                            + " - [DEBUG] - SENT: " + command.toJSONString());
                }
                output.flush();
                String recv_response;
                while(true) {
                    try {
                        if ((recv_response = input.readUTF()) != null) {
                            
                            if (Server.debug) {
                                System.out.println(new Timestamp(System.currentTimeMillis())
                                        + " - [DEBUG] - RECEIVED: " + recv_response);
                            }
                        }
                    } catch (SocketException e) {
                        break;
                    } catch (SocketTimeoutException e){ //socket timed out
                        break;
                    }
                }
            } catch (ConnectException e) {
                System.out.println(new Timestamp(System.currentTimeMillis())
                        + " - [ERROR] - Connection timed out.");
                sList.remove(receiver);
            } 
            catch (IOException e) {
                System.out.println(new Timestamp(System.currentTimeMillis())
                        + " - [ERROR] - IO Exception occurred.");
                sList.remove(receiver);
            }
        }       
    }
}

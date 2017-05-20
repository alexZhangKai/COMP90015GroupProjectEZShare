package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.TimerTask;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Exchanger extends TimerTask{

    //Send EXCHANGE command every 10 minutes
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        System.out.println("\n" + new Timestamp(System.currentTimeMillis()) 
                + " - [INFO] - started Exchanger\n");
        
        if (ServerList.getLength() > 0) {
            //select a random server from the list
            JSONObject receiver = ServerList.select();
            String ip = (String) receiver.get("hostname");
            int port = Integer.parseInt(receiver.get("port").toString());
            
            //connect to it and exchange list of servers
            try (Socket soc = new Socket(ip, port)){
                soc.setSoTimeout(Server.SOCKET_TIMEOUT_MS);
                
                DataInputStream input = new DataInputStream(soc.getInputStream());
                DataOutputStream output = new DataOutputStream(soc.getOutputStream());
                long startTime = System.currentTimeMillis();
                JSONObject command = new JSONObject();
                command.put("command", "EXCHANGE");
                
                JSONArray serverArr = ServerList.getCopyServerList();
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
                
                while(true) {
                    if (input.available() > 0) {
                        String recv_response = input.readUTF();
                        if (Server.debug) {
                            System.out.println(new Timestamp(System.currentTimeMillis())
                                    + " - [DEBUG] - RECEIVED: " + recv_response);
                        }
                    }
                    if ((System.currentTimeMillis() - startTime) > Server.SOCKET_TIMEOUT_MS){
                        soc.close();
                        break;
                    }
                }
            } catch (ConnectException e) {
                System.out.println(new Timestamp(System.currentTimeMillis())
                        + " - [ERROR] - Connection timed out.");
                ServerList.remove(receiver);
            } 
            catch (IOException e) {
                System.out.println(new Timestamp(System.currentTimeMillis())
                        + " - [ERROR] - IO Exception occurred.");
                ServerList.remove(receiver);
            }
        }       
    }
    

}

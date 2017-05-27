package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;

import org.json.simple.JSONObject;

class ListenRelay extends Thread{
    private Socket server;
    private DataOutputStream sendToClient;
    private volatile boolean newTemplateFlag = false;
    private volatile boolean unsubscribeFlag = false;
    private List<Resource> template;
    private Resource newTemplate;
    private String id;
    private Boolean debug = false;
    
    public ListenRelay(DataOutputStream sendToClient, Socket server, 
            List<Resource> template, String id, Boolean debug) {
        this.sendToClient = sendToClient;
        this.server = server;
        this.template = template;
        this.id = id;
        this.debug = debug;
    }

    public void setNewTemplateFlag(Resource newTemplate) {
        this.newTemplate = newTemplate;
        this.newTemplateFlag = true;
    }
    
    public void setUnsubscribeFlag() {
        this.unsubscribeFlag = true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        DataInputStream serverReply;
        DataOutputStream sendToServer;
        
        try {
            serverReply = new DataInputStream(server.getInputStream());
            sendToServer = new DataOutputStream(server.getOutputStream());
            JSONObject command;
            JSONObject resourceTemplate;
            
            for(Resource res : template) {
                //send the first subscribe command here
                command = new JSONObject();
                resourceTemplate = Connection.Resource2JSONObject(res);

                command.put("command", "SUBSCRIBE");
                command.put("relay", false);
                command.put("id",this.id);
                command.put("resourceTemplate", resourceTemplate);

                sendToServer.writeUTF(command.toJSONString());
            }
            String result;
            while(true) {
                if((result = serverReply.readUTF()) != null) {
                    if (debug) {
                        System.out.println(new Timestamp(System.currentTimeMillis())
                                + " - [DEBUG] - RECEIVED: " + result);
                    }
                    if(!result.contains("response") && !result.contains("success")) {
                        sendToClient.writeUTF(result);
                    }
                }
                
                if(newTemplateFlag) {
                    //TODO ?? send new subscribe command here
                    command = new JSONObject();
                    resourceTemplate = Connection.Resource2JSONObject(this.newTemplate);
                    
                    command.put("command", "SUBSCRIBE");
                    command.put("relay", false);
                    command.put("id",this.id);
                    command.put("resourceTemplate", resourceTemplate);
                    
                    sendToServer.writeUTF(command.toJSONString());
                }
                
                if(this.unsubscribeFlag) {
                    break;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
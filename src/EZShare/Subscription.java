package EZShare;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

public class Subscription {
	private Resource resourceTemplate;
	private Socket client;
	private List<Socket> serverRelay;
	private String id;
	
	public Subscription(Resource resourceTemplate, Socket client, String id) {
		this.resourceTemplate = resourceTemplate;
		this.client = client;
		this.id = id;
	}
	
	@SuppressWarnings("unchecked")
	void matchResource() throws IOException {
		//store local query results here
        List<Resource> res_results = new ArrayList<Resource>();                
        res_results = ResourceList.queryForQuerying(this.resourceTemplate);
        int result_cnt = res_results.size();

       //Create output stream
        DataOutputStream output = new DataOutputStream(this.client.getOutputStream());
        
        //send each resource to client
        for (Resource res: res_results){
            JSONObject resource_temp = Connection.Resource2JSONObject(res); 
            output.writeUTF(resource_temp.toJSONString());
            //TODO handle debug here
        }
        
        //Send number of results found to server
        JSONObject result_size = new JSONObject();
        result_size.put("resultSize", result_cnt);
        output.writeUTF(result_size.toJSONString());
	}
	
	public String getId() {
		return this.id;
	}
}

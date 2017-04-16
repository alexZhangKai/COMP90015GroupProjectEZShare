package server;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ResourceList {
	private ArrayList<Resource> resourceList;
	
	public ResourceList() {
		this.resourceList = new ArrayList<Resource>();
	}
	
	public synchronized boolean addResource(JSONObject resource) {
		System.out.println("add resource");
		Resource newResource = createResource(resource);
		//query resource in current list
		if(queryResource(newResource) == -1) {
			resourceList.add(newResource);
			return true;
		}
		return false;
	}
	
	public synchronized boolean removeResource(JSONObject resource) {
		Resource oldResource = createResource(resource);
		//query resource in current list
		int index = queryResource(oldResource);
		if(index != -1) {
			resourceList.remove(index);
			return true;
		}
		return false;
	}
	
	public int queryResource(Resource re) {
		int len = resourceList.size();
		
		//return -1 when list is empty
		if(len == 0) return -1;
		
		for(int i = 0; i < len; i++) {
			if(Resource.compare(re, (Resource) resourceList.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	public Resource createResource(JSONObject resource) {
		//get attributes
		String Name = resource.containsKey("name") ? (String) resource.get("name") : "";
		String Description = resource.containsKey("description") ? (String) resource.get("description") : "";
		String[] Tags = {"a"};
		//String[] Tags = resource.containsKey("tags") ? (String[]) resource.get("tags") : new String[0];
		String URI = (String) resource.get("uri");
		String Channel = resource.containsKey("channel") ? (String) resource.get("channel") : "";
		String Owner = resource.containsKey("owner") ? (String) resource.get("owner") : "";
		String EZserver = resource.containsKey("ezserver") ? (String) resource.get("ezserver") : "";
		
		return new Resource(Name, Description, Tags, URI, Channel, Owner, EZserver);
	}
}

package server;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ResourceList {
	private ArrayList<Resource> resourceList;
	
	public ResourceList() {
		this.resourceList = new ArrayList<Resource>();
	}
	
	public synchronized boolean addResource(Resource newResource) {
		//query resource in current list
		if(queryResource(newResource) == -1) {
			resourceList.add(newResource);
			return true;
		}
		
		return false;
	}
	
	public synchronized boolean removeResource(Resource oldResource) {
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
			if(re.equals(resourceList.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	public int getSize() {
		return resourceList.size();
	}
	
}

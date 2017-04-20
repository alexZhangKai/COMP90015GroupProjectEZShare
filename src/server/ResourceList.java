/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * This class keeps track of which resources exist on this server.
 * It implements the basic list functions [add, remove, indexOf] using 'synchronised' for concurrency protection.
 */

package server;

import java.util.ArrayList;

public class ResourceList {
	private ArrayList<Resource> resourceList;
	public ResourceList() {
		this.resourceList = new ArrayList<Resource>();
	}
	
	public synchronized String addResource(Resource newResource) {
		//Check if resource already exists...
		String response="";
		int index = queryResource(newResource);
		if(index == -1) {    //...add it if it doesn't.
			resourceList.add(newResource);
			response = "success";
		}
		
		else if(index != -1) {  //remove existing resource and add new resource
			resourceList.remove(index);
			resourceList.add(newResource);
			response = "success";
	
		}
		
	
		
		// else if(5) 
			
		return response;
		
	}
	
	public synchronized boolean removeResource(Resource oldResource) {
		//Check if resource already exists...
		int index = queryResource(oldResource);
		
		if(index != -1) { //...remove if it does.
			resourceList.remove(index);
			return true;
		}
		return false;
	}
	
	public int queryResource(Resource re) {
	    //Check if resource already exists...
		int len = resourceList.size();
		if(len == 0) return -1;
		
		for(int i = 0; i < len; i++) {    //...return its position in the list if it does.
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
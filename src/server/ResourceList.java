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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ResourceList {
	private static ArrayList<Resource> resourceList = new ArrayList<Resource>();
  
	public synchronized static void addResource(Resource newResource) throws serverException {
		//Check if resource already exists with same channel and URI
		Resource match = queryForChannelURI(newResource);
		
		if(match == null) {
			if(!resourceList.add(newResource)) {
				throw new serverException("cannot publish resource");
			}
		} else {
			if(!match.getOwner().equals(newResource.getOwner()))
				throw new serverException("cannot publish resource");
			if(!resourceList.remove(match) || !resourceList.add(newResource)) 
				throw new serverException("cannot publish resource");
		}
		
	}
	
	public synchronized static void removeResource(Resource oldResource) throws serverException {
		Resource match = queryForPK(oldResource);
		if(match == null) {
			throw new serverException("cannot remove resource");
		} else {
			if(!resourceList.remove(match)) throw new serverException("cannot remove resource");
		}
	}

	public static Resource queryForPK(Resource re) {
	    //Check if resource already exists...
		int len = resourceList.size();
		if(len == 0) return null;
		
		for(int i = 0; i < len; i++) {    //...return its position in the list if it does.
			if(re.getChannel().equals(resourceList.get(i).getChannel()) && 
				re.getOwner().equals(resourceList.get(i).getOwner()) &&
				re.getUri().equals(resourceList.get(i).getUri())) {
				return resourceList.get(i);
			}
		}
		return null;
	}
	
	public static Resource queryForChannelURI(Resource re) {
		int len = resourceList.size();
		if(len == 0) return null;
		
		for(int i = 0; i < len; i++) {    //...return its position in the list if it does.
			if((re.getChannel().equals(resourceList.get(i).getChannel())) &&
					(re.getUri().equals(resourceList.get(i).getUri()))) {
				return resourceList.get(i);
			}
		}
		return null;
	}
	
	public int getSize() {
		return resourceList.size();
	}
	

    public synchronized static List<Resource> queryForQuerying(Resource in_res) {
        List<Resource> results = new ArrayList<>();
        
        // Query current resource list for resources that match the template
        for (Resource curr_res: resourceList){
            if (in_res.getChannel().equals(curr_res.getChannel()) && 
                    in_res.getOwner().equals(curr_res.getOwner()) &&
                    compareTags(in_res.getTags(), curr_res.getTags()) &&
                    compareUri(in_res.getUri(), curr_res.getUri()) &&
                        (curr_res.getName().contains(in_res.getName()) ||
                        curr_res.getDescription().contains(in_res.getDescription()))) 
            {
                
                //Copy current resource into results list if it matches criterion
                Resource tempRes = new Resource(curr_res.getName(), curr_res.getDescription(), curr_res.getTags(), 
                        curr_res.getUri(), curr_res.getChannel(), 
                        curr_res.getOwner().equals("")? "":"*", curr_res.getEZserver());  //owner is never revealed

                //Send found resource as JSON to client
                results.add(tempRes);
            }
        }
        
        return results;
    }
    
    //Compare the two URIs for Query purposes. If the incoming URI has no host i.e. is empty, it should match all URIs
    //...otherwise only an exact match is acceptable
    private static boolean compareUri(URI in_uri, URI curr_uri) {
        if (in_uri.getHost() == null) {
            return true;
        } else {
            return in_uri.equals(curr_uri);
        }
    }

    //Compare the two tag sets. The incoming resource template's tags should be a subset of the current resource's.
    private static boolean compareTags(List<String> in_res, List<String> curr_res) {
        if (in_res.size() == 0 && curr_res.size() == 0) {
            return true;
        }
        else if (in_res.size() == 0 && curr_res.size() != 0){
            return false;
        }
        else if (in_res.size() != 0 && curr_res.size() == 0){
            return false;
        }
        else {
            for (String tag: in_res){
                if (!curr_res.contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

}
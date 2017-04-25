/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * This class is used to represent each resource.
 */

package server;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;

//Instantiate for each resource in ResourceList
public class Resource {
	private String Name;
	private String Description;
	private List<String> Tags;
	private URI uri;
	private String Channel;
	private String Owner;
	private String EZserver;
	
	public Resource(String Name, String Description, List<String> Tags, 
			URI uri, String Channel, String Owner, String EZserver) {
		this.Name = Name;
		this.Description = Description;
		this.Tags = new ArrayList<>(Tags);
		this.uri = uri;
		this.Channel = Channel;
		this.Owner = Owner;
		this.EZserver = EZserver;
	}	
	public Resource(Resource res){
	    this(res.getName(), res.getDescription(), res.getTags(), res.getUri(), 
	            res.getChannel(), res.getOwner(), res.getEZserver());
	}

	//Each resource is uniquely identified by its channel, owner and URI; thus compare
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (Channel == null) {
            if (other.Channel != null)
                return false;
        } else if (!Channel.equals(other.Channel))
            return false;
        if (Owner == null) {
            if (other.Owner != null)
                return false;
        } else if (!Owner.equals(other.Owner))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    public String getOwner() {
		return this.Owner;
	}
	
	public String getChannel() {
		return this.Channel;
	}
	
	public URI getUri() {
		return this.uri;
	}

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public List<String> getTags() {
        return Tags;
    }

    public String getEZserver() {
        return EZserver;
    }
}

//Maybe better to auto-generate the equals method based on primary key attributes of this class, see equals method above
//public static boolean compare(Resource re1, Resource re2) {
//  if (re1.getChannel().equals(re2.getChannel()) && 
//          re1.getOwner().equals(re2.getOwner()) && 
//          re1.getURI().equals(re2.getURI())) 
//  {
//      return true;
//  }
//  return false;
//}
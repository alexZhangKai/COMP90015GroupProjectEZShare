/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * This class is used to represent each resource.
 */

package server;

//Instantiate for each resource in ResourceList
public class Resource {
	private String Name;
	private String Description;
	private String[] Tags;
	private String URI;
	private String Channel;
	private String Owner;
	private String EZserver;
	
	public Resource(String Name, String Description, String[] Tags, 
			String URI, String Channel, String Owner, String EZserver) {
		this.Name = Name;
		this.Description = Description;
		this.Tags = Tags;
		this.URI = URI;
		this.Channel = Channel;
		this.Owner = Owner;
		this.EZserver = EZserver;
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
        if (URI == null) {
            if (other.URI != null)
                return false;
        } else if (!URI.equals(other.URI))
            return false;
        return true;
    }

    public String getOwner() {
		return this.Owner;
	}
	
	public String getChannel() {
		return this.Channel;
	}
	
	public String getURI() {
		return this.URI;
	}

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String[] getTags() {
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
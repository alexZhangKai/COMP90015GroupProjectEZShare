package server;

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
	
	public static boolean compare(Resource re1, Resource re2) {
		if(re1.getChannel().equals(re2.getChannel()) && 
				re1.getOwner().equals(re2.getOwner()) && 
				re1.getURI().equals(re2.getURI())) {
			return true;
		}
		return false;
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
}

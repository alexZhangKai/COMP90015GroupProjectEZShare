package server;

public class serverException extends Exception {
	private String errorMsg;
	
	public serverException(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String toString() {
		return errorMsg;
	}
}

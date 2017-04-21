/*
 * Distributed Systems
 * Group Project 1
 * Sem 1, 2017
 * Group: AALT
 * 
 * This is the Error Handling class for the JSON2Resource method, where an error will throw an exception.
 */

package server;

public class serverException extends Exception {
	private String errorMSG;
	
	public serverException(String errorMSG) {
		this.errorMSG = errorMSG;
	}
	
	public String toString() {
		return errorMSG;
	}
}

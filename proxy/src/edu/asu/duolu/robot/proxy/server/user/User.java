package edu.asu.duolu.robot.proxy.server.user;

import java.util.ArrayList;
import java.util.Arrays;

public class User implements Comparable<User> {

	// These fields are persistent
	String userID;
	String role; // Not it can only be admin, privileged, or normal
	long salt;
	byte[] hashedPassword;
	
	// These fields are transient
	String state;
	
	protected User(String userID, String role, long salt, byte[] hashedPassword) {
		super();
		this.userID = userID;
		this.role = role;
		this.salt = salt;
		this.hashedPassword = hashedPassword;
	}

	public String getUserID() {
		return userID;
	}

	public String getRole() {
		return role;
	}

	public long getSalt() {
		return salt;
	}

	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	protected void setRole(String role) {
		this.role = role;
	}

	protected void setSalt(long salt) {
		this.salt = salt;
	}

	protected void setHashedPassword(byte[] hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public boolean isAdmin() {
		
		return userID.equals("admin") && role.equals("admin");
	}

	@Override
	public String toString() {
		return "User [userID=" + userID + ", role=" + role + ", salt=" + salt + ", hashedPassword="
				+ Arrays.toString(hashedPassword) + ", state=" + state + "]";
	}
	
	public String toCSV() {
		
		StringBuilder sb = new StringBuilder();
	    for (byte b : hashedPassword) {
	        sb.append(String.format("%02x", b));
	    }
	    String hexPassword = Long.toHexString(salt);
		
	    //return userID + "," + sb.toString() + "," + hexPassword + "," + role;
	    
	    return "| " + String.format("%10s | ", userID) + String.format("%20s | ", hexPassword) + String.format("%20d | ", salt) + String.format("%10s | ", role);
	    
	}

	// List all ACLs
	public static String UsersToCSV(ArrayList<User> us) {

		if(us == null)
			return "";

		StringBuffer sb = new StringBuffer();
		
		String h1 = "+------------+----------------------+----------------------+------------+\n";
		String h2 = "| " + String.format("%10s | ", "user ID") 
				+ String.format("%20s | ", "hashed password") 
				+ String.format("%20s | ", "salt") 
				+ String.format("%10s |", "role");
		
		sb.append("\n");
		sb.append(h1);
		sb.append(h2);
		sb.append("\n");
		sb.append(h1);
		
		for(User acl : us) {
			
			sb.append(acl.toCSV());
			sb.append("\n");
		}
		
		sb.append(h1);
		
		return sb.toString();
	}

	@Override
	public int compareTo(User other) {
		
		return this.userID.compareTo(other.userID);
	}
}

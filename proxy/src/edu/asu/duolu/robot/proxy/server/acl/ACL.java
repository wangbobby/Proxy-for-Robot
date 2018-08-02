package edu.asu.duolu.robot.proxy.server.acl;

import java.util.ArrayList;

public class ACL implements Comparable<ACL> {

	protected int id;
	protected String resource;
	protected String role;
	protected String action;
	
	
	protected ACL(int id, String resource, String role, String action) {
		super();
		this.id = id;
		this.resource = resource;
		this.role = role;
		this.action = action;
	}


	protected void setResource(String resource) {
		this.resource = resource;
	}


	protected void setRole(String role) {
		this.role = role;
	}


	protected void setAction(String action) {
		this.action = action;
	}


	public int getId() {
		return id;
	}


	public String getResource() {
		return resource;
	}


	public String getRole() {
		return role;
	}


	public String getAction() {
		return action;
	}


	@Override
	public String toString() {
		return "ACL [id=" + id + ", resource=" + resource + ", role=" + role + ", action=" + action + "]";
	}
	
	public String toCSV() {
		
		//return id + "," + resource + "," + role + "," + action;
		
		return "| " + String.format("%4d | ", id) + String.format("%30s | ", resource) + String.format("%10s | ", role) + String.format("%10s |", action);
	}


	@Override
	public int compareTo(ACL other) {
		
		return id - other.id;
	}
	
	// List all ACLs
	public static String ACLsToCSV(ArrayList<ACL> as) {

		if(as == null)
			return "";

		StringBuffer sb = new StringBuffer();
		
		String h1 = "+------+--------------------------------+------------+------------+\n";
		String h2 = "| " + String.format("%4s | ", "ID") 
				+ String.format("%30s | ", "resource") 
				+ String.format("%10s | ", "role") 
				+ String.format("%10s |", "action");
		
		sb.append("\n");
		sb.append(h1);
		sb.append(h2);
		sb.append("\n");
		sb.append(h1);
		
		for(ACL acl : as) {
			
			sb.append(acl.toCSV());
			sb.append("\n");
		}
		
		sb.append(h1);
		
		return sb.toString();
	}

	
}

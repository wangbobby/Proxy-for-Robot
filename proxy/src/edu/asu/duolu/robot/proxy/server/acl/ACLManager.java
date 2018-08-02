package edu.asu.duolu.robot.proxy.server.acl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ACLManager {

	// ID to ACL map, one to one
	protected HashMap<Integer, ACL> acls = new HashMap<Integer, ACL>();

	// Role + Resource to ACL map, one to one
	protected HashMap<String, ACL> RRMatch = new HashMap<String, ACL>();

	// resource to ACL map, one to many
	protected HashMap<String, ArrayList<ACL>> resourceMatch = new HashMap<String, ArrayList<ACL>>();

	// role to ACL map, one to many
	protected HashMap<String, ArrayList<ACL>> roleMatch = new HashMap<String, ArrayList<ACL>>();

	
	// TODO: synchronize to database
	
	protected List<ACL> addedACLs = new LinkedList<ACL>();
	protected List<ACL> removedACLs = new LinkedList<ACL>();
	protected List<ACL> updatedACLs = new LinkedList<ACL>();

	protected int nextACLID = 0;

	public ACLManager() {

		// Deny all by default, except admin
		addACL("*", "admin", "allow");
		addACL("*", "priv", "deny");
		addACL("*", "free", "deny");

		// TODO: add some default ACLs

	}

	protected boolean doesResourceContains(String resource, String s) {

		String[] rs = resource.split("\\.");

		for (String r : rs) {
			if (r.equals(s))
				return true;
		}
		return false;
	}

	// Generate a list of strings with wildcard that covers the given resource.
	// For example, if the given resource is "robot.object.method", the
	// generated list is:
	// *,
	// robot.*,
	// robot.object.*,
	// robot.object.method
	//
	protected ArrayList<String> getWildcardedResourceList(String resource) {

		ArrayList<String> result = new ArrayList<String>();

		String[] rs = resource.split("\\.");
		int len = rs.length;

		StringBuffer sb = new StringBuffer();

		result.add("*");
		if (resource.equals("*")) {
			return result;
		}

		sb.append(rs[0]);

		for (int i = 1; i < len; i++) {

			result.add(sb.toString() + ".*");

			if (rs[i].equals("*")) {
				break;
			} else {

				sb.append(".");
				sb.append(rs[i]);
			}
		}

		if (!rs[len - 1].equals("*")) {
			result.add(sb.toString());
		}

		return result;
	}

	// Check whether r1 covers r2.
	// For example, "robot.object.*" covers "robot.object.method".
	// Another example, both "*" and "robot.*" covers "robot.object.method".
	protected boolean doesResourceCoversResource(String r1, String r2) {

		ArrayList<String> res = getWildcardedResourceList(r2);
		return res.contains(r1);
	}

	// Check whether the role can access the resource.
	// NOTE: resource here can not contain any wildcard here.
	public synchronized ReturnCode checkACL(String resource, String role) {

		ArrayList<String> res = getWildcardedResourceList(resource);
		int len = res.size();

		ACL acl = null;
		for (int i = len - 1; i >= 0; i--) {

			String r = res.get(i);
			String key = r + "+" + role;
			acl = RRMatch.get(key);
			if (acl == null)
				continue;
			else
				break;
		}

		if (acl != null && acl.getAction().equals("allow"))
			return new ReturnCode(true, "OK", "allow");
		else
			return new ReturnCode(true, "Permission denied.", "deny");
	}

	// List all ACLs
	public synchronized ReturnCode listAllACLs() {

		ArrayList<ACL> as = new ArrayList<ACL>(acls.values());
		Collections.sort(as);

		return new ReturnCode(true, "OK", as);
	}
	

	// Search an ACL by ID
	public synchronized ReturnCode searchACLByID(int aclid) {

		ACL match = acls.get(aclid);
		if (match == null) {
			return new ReturnCode(false, "No such ACL exists.");
		} else {
			return new ReturnCode(true, "OK", match);
		}

	}

	// Search all ACLs related to a resource
	// NOTE: Here resource can have wildcard.
	public synchronized ReturnCode searchACLByResource(String resource) {

		ArrayList<ACL> result = new ArrayList<ACL>();

		ArrayList<String> res = getWildcardedResourceList(resource);
		int len = res.size();

		for (int i = len - 1; i >= 0; i--) {

			String r = res.get(i);
			ArrayList<ACL> match = resourceMatch.get(r);
			if(match != null)
				result.addAll(match);
		}

		Collections.sort(result);
		
		if (result.isEmpty()) {
			return new ReturnCode(false, "No such ACL exists.");
		} else {
			return new ReturnCode(true, "OK", result);
		}
	}

	// Search all ACLs related to a role
	public synchronized ReturnCode searchACLByRole(String role) {

		ArrayList<ACL> result = new ArrayList<ACL>();
		
		ArrayList<ACL> match = roleMatch.get(role);
		if(match != null)
			result.addAll(match);
		
		Collections.sort(result);

		if (result.isEmpty()) {
			return new ReturnCode(false, "No such ACL exists.");
		} else {
			return new ReturnCode(true, "OK", result);
		}
	}
	
	// Search ACLs by both resource and role
	public synchronized ReturnCode searchACLByRR(String resource, String role) {

		ArrayList<ACL> result = new ArrayList<ACL>();
		
		ArrayList<String> res = getWildcardedResourceList(resource);
		int len = res.size();

		ACL acl = null;
		for (int i = len - 1; i >= 0; i--) {

			String r = res.get(i);
			String key = r + "+" + role;
			acl = RRMatch.get(key);
			if (acl == null)
				continue;
			else
				result.add(acl);
		}

		Collections.sort(result);
		
		if (result.isEmpty())
			return new ReturnCode(false, "No such ACL exists.");
		else
			return new ReturnCode(true, "Permission denied.", "deny");
	}


	protected void addACLInIndex(ACL acl) {

		String resource = acl.getResource();
		String role = acl.getRole();
		String key = resource + "+" + role;

		// Role + Resource to ACL map
		RRMatch.put(key, acl);

		// resource to ACL map
		ArrayList<ACL> racls = resourceMatch.get(resource);
		if (racls == null)
			racls = new ArrayList<ACL>();
		racls.add(acl);
		resourceMatch.put(resource, racls);

		// role to ACL map
		ArrayList<ACL> oacls = roleMatch.get(role);
		if (oacls == null)
			oacls = new ArrayList<ACL>();
		oacls.add(acl);
		roleMatch.put(role, oacls);

	}

	// Add an ACL
	public synchronized ReturnCode addACL(String resource, String role, String action) {

		// Check duplication

		String key = resource + "+" + role;
		ACL dup = RRMatch.get(key);
		if (dup != null) {
			return new ReturnCode(false, "ACL with the specified resource and role already exists.");
		}

		int id = getNextACLID();

		ACL acl = new ACL(id, resource, role, action);

		// Add it
		acls.put(id, acl);

		// Also add it to indexes
		addACLInIndex(acl);

		// Put it to added ACL list
		addedACLs.add(acl);

		return new ReturnCode(true, "OK", acl);
	}

	protected void removeACLFromIndex(ACL acl) {

		String resource = acl.getResource();
		String role = acl.getRole();
		String key = resource + "+" + role;

		// Role + Resource to ACL map
		RRMatch.remove(key);

		// resource to ACL map
		ArrayList<ACL> racls = resourceMatch.get(resource);
		if (racls == null) {
			System.out.println("BUG in ACLManager.removeACL!!! " + "resourceMatch does not contain the ACL entry!!!");
		}
		racls.remove(acl);
		resourceMatch.put(resource, racls);

		// role to ACL map
		ArrayList<ACL> oacls = roleMatch.get(role);
		if (oacls == null) {
			System.out.println("BUG in ACLManager.removeACL!!! " + "roleMatch does not contain the ACL entry!!!");
		}
		oacls.remove(acl);
		roleMatch.put(role, oacls);

	}

	// Remove an ACL
	public synchronized ReturnCode removeACL(int id) {

		ACL acl = acls.get(id);
		if (acl == null) {

			// No such ACL exists.
			return new ReturnCode(false, "No such ACL exists.");
		} else {

			// Remove the acl
			acls.remove(acl.getId());

			// Also remove it from indexes
			removeACLFromIndex(acl);

			// Put it to removed ACL list
			removedACLs.add(acl);

			return new ReturnCode(true, "OK");
		}

	}

	// Update an ACL
	public synchronized ReturnCode updateACL(int id, String resource, String role, String action) {

		ACL acl = acls.get(id);
		if (acl == null) {

			// No such ACL exists.
			return new ReturnCode(false, "No such ACL exists.");
		} else {

			String resourceOld = acl.getResource();
			String roleOld = acl.getRole();

			// NOTE: The ACL ID does not change.
			
			// Also change indexes if necessary
			if (resourceOld != resource || roleOld != role) {
				removeACLFromIndex(acl);
			}

			acl.setResource(resource);
			acl.setRole(role);
			acl.setAction(action);

			// Also change indexes if necessary
			if (resourceOld != resource || roleOld != role) {
				addACLInIndex(acl);
			}
			
			updatedACLs.add(acl);

			return new ReturnCode(true, "OK", acl);
		}

	}

	public synchronized void syncACLs() {

	}


	protected synchronized int getNextACLID() {

		return nextACLID++;
	}

	public static class ReturnCode {

		protected boolean ok;
		protected String reason;
		protected ACL acl; // for searchACL()
		protected String action; // for checkACL()
		protected ArrayList<ACL> acls; // for listAllACL() and searchACLXXX()

		protected ReturnCode(boolean ok, String reason) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.acl = null;
			this.action = null;
		}

		protected ReturnCode(boolean ok, String reason, ACL acl) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.acl = acl;
			this.action = null;
		}

		protected ReturnCode(boolean ok, String reason, String action) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.acl = null;
			this.action = action;
		}

		protected ReturnCode(boolean ok, String reason, ArrayList<ACL> acls) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.acl = null;
			this.action = null;
			this.acls = acls;
		}

		public boolean isOK() {
			return ok;
		}

		public String getReason() {
			return reason;
		}

		public ACL getACL() {
			return acl;
		}

		public String getAction() {
			return action;
		}

		public ArrayList<ACL> getACLs() {
			return acls;
		}


	}

	// testing
	public static void main(String[] args) {

		ACLManager aclm = new ACLManager();

		
		// testing basic helper
		
		ArrayList<String> res = aclm.getWildcardedResourceList("robot.object.method");

		System.out.println(res);
		System.out.println();

		System.out.println(aclm.doesResourceCoversResource("*", "robot.object.method"));
		System.out.println(aclm.doesResourceCoversResource("robot.*", "robot.object.method"));
		System.out.println(aclm.doesResourceCoversResource("robot.object.*", "robot.object.method"));
		System.out.println(aclm.doesResourceCoversResource("robot.object.method", "robot.object.method"));
		System.out.println();

		System.out.println(aclm.doesResourceCoversResource("*", "robot.object.*"));
		System.out.println(aclm.doesResourceCoversResource("robot.*", "robot.object.*"));
		System.out.println(aclm.doesResourceCoversResource("robot.object.*", "robot.object.*"));
		System.out.println(aclm.doesResourceCoversResource("robot.object.method", "robot.object.*"));
		System.out.println();

		
		// testing ACL
		
		System.out.println(ACL.ACLsToCSV(aclm.listAllACLs().getACLs()));
		System.out.println();
		
		// test add
		
		aclm.addACL("robot.object1.method1", "admin", "deny");
		aclm.addACL("robot.object1.method3", "admin", "deny");
		aclm.addACL("robot.object2.method2", "admin", "allow");
		aclm.addACL("robot.object2.method4", "admin", "allow");
		
		aclm.addACL("robot.object1.*", "admin", "allow");
		aclm.addACL("robot.object2.*", "admin", "deny");
		
		aclm.addACL("robot.*", "admin", "deny");
		
		System.out.println(ACL.ACLsToCSV(aclm.listAllACLs().getACLs()));
		System.out.println();

		System.out.println(aclm.RRMatch);
		System.out.println();

		System.out.println(aclm.resourceMatch);
		System.out.println();

		System.out.println(aclm.roleMatch);
		System.out.println();

		// test check
		
		System.out.println(aclm.checkACL("robot.object1.method1", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object1.method2", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object1.method3", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object1.method4", "admin").getAction());
		System.out.println();
		System.out.println(aclm.checkACL("robot.object2.method1", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object2.method2", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object2.method3", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object2.method4", "admin").getAction());
		System.out.println();
		System.out.println(aclm.checkACL("robot.object3.method1", "admin").getAction());
		System.out.println(aclm.checkACL("robot.object3.method2", "admin").getAction());
		System.out.println();
		System.out.println(aclm.checkACL("xxx.yyy.zzz", "admin").getAction());
		System.out.println();
		
		
		// test search
		
		System.out.println(ACL.ACLsToCSV(aclm.searchACLByResource("robot.object1.method1").getACLs()));
		System.out.println();
		System.out.println(ACL.ACLsToCSV(aclm.searchACLByResource("robot.object1.*").getACLs()));
		System.out.println();
		System.out.println(ACL.ACLsToCSV(aclm.searchACLByResource("robot.*").getACLs()));
		System.out.println();

		
		System.out.println(ACL.ACLsToCSV(aclm.searchACLByRole("admin").getACLs()));
		System.out.println();

		
		// test update
		
		System.out.println(aclm.updateACL(3, "xxx.yyy.zzz", "free", "allow"));
		System.out.println();

		System.out.println(ACL.ACLsToCSV(aclm.listAllACLs().getACLs()));
		System.out.println();

		System.out.println(ACL.ACLsToCSV(aclm.searchACLByResource("xxx.yyy.zzz").getACLs()));
		System.out.println();
		
		System.out.println(ACL.ACLsToCSV(aclm.searchACLByRole("free").getACLs()));
		System.out.println();

		
		// test remove
		
		aclm.removeACL(3);
		aclm.removeACL(4);
		aclm.removeACL(5);
		
		System.out.println(ACL.ACLsToCSV(aclm.listAllACLs().getACLs()));
		System.out.println();

		
	}

}

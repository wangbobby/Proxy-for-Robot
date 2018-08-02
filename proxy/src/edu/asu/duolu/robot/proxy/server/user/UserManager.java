package edu.asu.duolu.robot.proxy.server.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class UserManager {

	// Random number generator, for generating salt.
	protected Random r = new Random();
	
	protected HashMap<String, User> users = new HashMap<String, User>();

	protected List<User> addedUsers = new LinkedList<User>();
	protected List<User> removedUsers = new LinkedList<User>();
	protected List<User> modifiedUsers = new LinkedList<User>();

	public UserManager() {

		// TODO: initialize from database
		
		// CAUTION: These are just for testing or demo usage.
		User admin = constructUser("admin", "admin", "admin");
		users.put("admin", admin);
		User priv = constructUser("priv", "priv", "priv");
		users.put("priv", priv);
		User free = constructUser("free", "free", "free");
		users.put("free", free);
		
		
	}

	
	public synchronized ReturnCode searchUser(String userid) {

		User user = users.get(userid);
		if (user == null) {
			return new ReturnCode(false, "No such User exists.");
		} else {
			return new ReturnCode(true, "OK", user);
		}
	}
	
	public synchronized ReturnCode listAllUsers() {
		
		ArrayList<User> us = new ArrayList<User>(users.values());
		Collections.sort(us);
		
		return new ReturnCode(true, "OK", us);
	}

	public synchronized String listAllUsersInCSV() {
		
		ArrayList<User> us = new ArrayList<User>(users.values());
		Collections.sort(us);
		
		StringBuffer sb = new StringBuffer();
		
		for(User user : us) {
			
			sb.append(user.toCSV());
			sb.append("\n");
		}
		return sb.toString();
		
	}
	
	protected byte[] hashPassword(String password, long salt) {
		
		try {
			String saltedPassword = password + salt;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] data = saltedPassword.getBytes();
			md.update(data);
			
			return md.digest();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected boolean compareHash(byte[] hash, byte[] stored) {
		
		return Arrays.equals(hash, stored);
	}
	
	public synchronized ReturnCode checkUser(String userid, String password) {
		
		User user = users.get(userid);
		if (user == null) {
			return new ReturnCode(false, "No such User exists.");
		} else {
			
			
			byte[] stored = user.getHashedPassword();
			long salt = user.getSalt();
			
			byte[] hash = hashPassword(password, salt);
			
			if(compareHash(hash, stored))
				return new ReturnCode(true, "success", user);
			else
				return new ReturnCode(false, "failure#User ID and Password do not match!", user);
		}
		
		
	}
	
	protected User constructUser(String userid, String password, String role) {
		
		long salt = r.nextLong();
		byte[] hashedPassword = hashPassword(password, salt);
		
		User user = new User(userid, role, salt, hashedPassword);
		
		return user;
	}
	
	public synchronized ReturnCode addUser(String userid, String password, String role) {

		// check duplication
		
		User dup = users.get(userid);
		if(dup != null) {
			
			return new ReturnCode(false, "User ID already exists.", dup);
		}
		
		User user = constructUser(userid, password, role);
		
		users.put(userid, user);
		addedUsers.add(user);
		
		return new ReturnCode(true, "OK", user);
		
	}
	
	public synchronized ReturnCode removeUser(String userid) {
		
		User user = users.get(userid);
		if(user == null) {
			
			// No such user exists.
			return new ReturnCode(false, "No such user exists.");

		} else {
			
			users.remove(userid);
			removedUsers.add(user);
			return new ReturnCode(true, "OK");
		}
		
	}
	
	public synchronized ReturnCode updateUser(String userid, String password, String role) {
		
		User user = users.get(userid);
		if(user == null) {
			
			// No such user exists.
			return new ReturnCode(false, "No such user exists.");

		} else {

			long salt = r.nextLong();
			byte[] hashedPassword = hashPassword(password, salt);

			user.setRole(role);
			user.setSalt(salt);
			user.setHashedPassword(hashedPassword);
			
			return new ReturnCode(true, "OK", user);
		}
		
	}

	public synchronized void syncUsers() {

		syncUserAdd();
		syncUserRemove();
		syncUserUpdate();
	}

	protected synchronized void syncUserAdd() {

		// TODO: synchronize with database

	}

	protected synchronized void syncUserRemove() {

		// TODO: synchronize with database

	}

	protected synchronized void syncUserUpdate() {

		// TODO: synchronize with database

	}

	public static class ReturnCode {

		protected boolean ok;
		protected String reason;
		protected User user;
		protected ArrayList<User> users;

		protected ReturnCode(boolean ok, String reason) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.user = null;
		}

		protected ReturnCode(boolean ok, String reason, User user) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.user = user;
			this.users = null;
		}
		protected ReturnCode(boolean ok, String reason, ArrayList<User> users) {
			super();
			this.ok = ok;
			this.reason = reason;
			this.user = null;
			this.users = users;
		}


		public boolean isOK() {
			return ok;
		}

		public String getReason() {
			return reason;
		}

		public User getUser() {
			return user;
		}

		public ArrayList<User> getUsers() {
			return users;
		}

	}

	// testing
	public static void main(String[] args) {
		
		UserManager um = new UserManager();
		
		System.out.println(User.UsersToCSV(um.listAllUsers().getUsers()));
		System.out.println();
		
		
		
		
		ReturnCode ret1 = um.addUser("someone", "somepass", "somerole");
		System.out.println("Add user (someone, somepass, somerole): " + ret1.getReason());
		System.out.println();
		
		System.out.println(User.UsersToCSV(um.listAllUsers().getUsers()));
		System.out.println();

		ReturnCode ret2 = um.checkUser("someone", "somepass");
		System.out.println("Check user (someone, somepass): " + ret2.getReason());
		System.out.println();

		ReturnCode ret3 = um.updateUser("someone", "somepass1", "somerole2");
		System.out.println("Update user (someone, somepass1, somerole2): " + ret3.getReason());
		System.out.println();

		System.out.println(User.UsersToCSV(um.listAllUsers().getUsers()));
		System.out.println();

		
		ReturnCode ret4 = um.removeUser("someone");
		System.out.println("Remove user (someone): " + ret4.getReason());
		System.out.println();
	
		System.out.println(User.UsersToCSV(um.listAllUsers().getUsers()));
		System.out.println();
		

	}

}

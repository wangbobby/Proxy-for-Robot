package edu.asu.duolu.robot.proxy.server;

import java.util.HashMap;
import java.util.Random;

import com.rabbitmq.client.Connection;

import edu.asu.duolu.robot.proxy.log.LogManager;
import edu.asu.duolu.robot.proxy.server.acl.ACLManager;
import edu.asu.duolu.robot.proxy.server.user.User;
import edu.asu.duolu.robot.proxy.server.user.UserManager;

public class ClientConnectionManager {

	protected Random r = new Random();
	protected RobotConnection rc;
	protected UserManager um;
	protected ACLManager aclm;
	protected LogManager lm;
	
	HashMap<String, ClientConnection> conns = new HashMap<String, ClientConnection>();
	
	
	protected ClientConnectionManager(RobotConnection rc, UserManager um, ACLManager aclm, LogManager lm) {
		super();
		this.rc = rc;
		this.um = um;
		this.aclm = aclm;
		this.lm = lm;
	}

	public synchronized ClientConnection getClientConnection(String userid) {
		
		return conns.get(userid);

	}
	
	// Create a new client connection with a pair of queues.
	// @user: the user associated with the client connection
	// @conn: RabbitMQ connection
	public synchronized ClientConnection newClientConnection(User user, Connection conn) {
		
		String userid = user.getUserID();
		String txQ = userid + Long.toHexString(r.nextLong());
		String rxQ = userid + Long.toHexString(r.nextLong());
		
		ClientConnection cc = new ClientConnection(this, txQ, rxQ, user, conn, rc, um, aclm, lm);
		
		conns.put(userid, cc);
		
		
		return cc;
	}
	
	// Destroy a client connection, called when user logout.
	public synchronized boolean distroyClientConnection(User user) {
		
		String userid = user.getUserID();
		
		ClientConnection cc = conns.get(userid);
		if(cc == null) {
			return false;
		}
		conns.remove(userid);
		
		return true;
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

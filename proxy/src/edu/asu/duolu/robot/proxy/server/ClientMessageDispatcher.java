package edu.asu.duolu.robot.proxy.server;

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import edu.asu.duolu.robot.proxy.log.Log;
import edu.asu.duolu.robot.proxy.log.LogManager;
import edu.asu.duolu.robot.proxy.message.ACLMsg;
import edu.asu.duolu.robot.proxy.message.InvalidMessageFormatException;
import edu.asu.duolu.robot.proxy.message.ListenMsg;
import edu.asu.duolu.robot.proxy.message.LogMsg;
import edu.asu.duolu.robot.proxy.message.LogoutMsg;
import edu.asu.duolu.robot.proxy.message.MethodCallMsg;
import edu.asu.duolu.robot.proxy.message.RequestMessage;
import edu.asu.duolu.robot.proxy.message.UserMsg;
import edu.asu.duolu.robot.proxy.server.acl.ACL;
import edu.asu.duolu.robot.proxy.server.acl.ACLManager;
import edu.asu.duolu.robot.proxy.server.user.User;
import edu.asu.duolu.robot.proxy.server.user.UserManager;

class ClientMessageDispatcher {

	protected ClientConnection cc;
	protected RobotConnection rc;
	protected UserManager um;
	protected ACLManager aclm;
	protected LogManager lm;

	// Mark whether this connection is in the process of one request.
	protected boolean busy;

	public ClientMessageDispatcher(ClientConnection cc, RobotConnection rc, UserManager um, ACLManager aclm,
			LogManager lm) {
		this.cc = cc;
		this.rc = rc;
		this.um = um;
		this.aclm = aclm;
		this.lm = lm;
	}

	public void dispatch(String message)
			throws IOException {

		// Request message is logged here.
		lm.addLog(cc.user.getUserID(), true, message);

		// Dispatch the message.
		
		try {

			RequestMessage msg = RequestMessage.parseRequestMessage(message);

			if (msg instanceof LogoutMsg) {
				
				String reply = "Bye!";
				cc.reply(reply);
				cc.disconnect();
				
				lm.addLog(cc.user.getUserID(), false, reply);


			} else if (msg instanceof MethodCallMsg) {

				MethodCallMsg mcm = (MethodCallMsg) msg;

				ACLManager.ReturnCode ret = aclm.checkACL(mcm.getMethod(), cc.getUser().getRole());
				if(ret.getAction().equals("allow")) {
					String result = rc.callMethod(mcm.getMethod(), mcm.getArgv(), mcm.getArgstring(), cc);
				
					// CAUTION: Now we use blocking method of method call.
					
					cc.reply(result);
					
				} else {
					
					String reply = "Failure#Permission denied!";
					cc.reply(reply);
					
					lm.addLog(cc.user.getUserID(), false, reply);

				}

			} else if (msg instanceof ListenMsg) {

				ListenMsg lmsg = (ListenMsg) msg;
				if (lmsg.isListen()) {
					
					ACLManager.ReturnCode ret = aclm.checkACL(lmsg.getEventSource(), cc.getUser().getRole());
					if(ret.getAction().equals("allow")) {
						rc.listenToEventSource(lmsg.getEventSource(), cc);
					} else {
						
						String reply = "Failure#Permission denied!";
						cc.reply(reply);
						
						lm.addLog(cc.user.getUserID(), false, reply);

					}
				} else {
					rc.unlistenToEventSource(lmsg.getEventSource(), cc);
				}
				
				// CAUTION: Reply is the actual event.
				
				

			} else if (msg instanceof UserMsg) {

				if (!cc.getUser().isAdmin()) {

					// Only admin can manage user
					String reply = "Failure#Only admin can manage user.";
					cc.reply(reply);

				}

				UserMsg umsg = (UserMsg) msg;
				String reply = processUserMsg(umsg);
				
				cc.reply(reply);
				
				// Log user operations other than LISTALL
				if(umsg.getOp() != UserMsg.Operation.LISTALL) {
					lm.addLog(cc.user.getUserID(), false, reply);
				}
				

			} else if (msg instanceof ACLMsg) {

				if (!cc.getUser().isAdmin()) {

					// Only admin can manage user
					String reply = "Failure#Only admin can manage ACL.";
					cc.reply(reply);

				}

				ACLMsg aclmsg = (ACLMsg) msg;
				String reply = processACLMsg(aclmsg);
				
				cc.reply(reply);
				
				// Log ACL operations other than LISTALL or SEARCH
				if(aclmsg.getOp() != ACLMsg.Operation.LISTALL && aclmsg.getOp() != ACLMsg.Operation.SEARCH) {
					lm.addLog(cc.user.getUserID(), false, reply);
				}
				
			} else if (msg instanceof LogMsg) {
				
				if (!cc.getUser().isAdmin()) {

					// Only admin can manage user
					String reply = "Failure#Only admin can manage ACL.";
					cc.reply(reply);

				}
				
				// LogMsg lmsg = (LogMsg) msg;
				
				// Since there is only one type of log message, the processing logic is put here.
				
				StringBuffer sb = new StringBuffer();
				ArrayList<Log> logs = lm.listLog();
				for(Log l: logs) {
					sb.append(l);
					sb.append("\n");
				}
				String reply = sb.toString();
				cc.reply(reply);
				
				// NOTE: Do not log the reply of list log!!!
			}

		} catch (InvalidMessageFormatException e) {

			// Unexpected message received.
			String reply = "Failure#Invalid message format.";
			cc.reply(reply);

		}

	} // dispatch
	

	protected String processUserMsg(UserMsg umsg) {
		
		String reply = "";
		
		switch (umsg.getOp()) {

		case LIST: {

			UserManager.ReturnCode ret = um.searchUser(umsg.getUserid());
			if(ret.isOK()) {
				reply = ret.getUser().toCSV();
			} else {
				reply = "Failure#" + ret.getReason();
			}
			break;

		}
		case LISTALL: {

			UserManager.ReturnCode ret = um.listAllUsers();
			if(ret.isOK()) {
				
				reply = User.UsersToCSV(ret.getUsers());
			} else {
				reply = "Failure#" + ret.getReason();
			}
			
			break;
		}
		case ADD: {

			UserManager.ReturnCode ret = um.addUser(umsg.getUserid(), umsg.getPassword(), umsg.getRole());
			if(ret.isOK()) {
				reply = "Success";
			} else {
				reply = "Failure#" + ret.getReason();
			}
			
			break;
		}
		case REMOVE: {

			UserManager.ReturnCode ret = um.removeUser(umsg.getUserid());
			if(ret.isOK()) {
				reply = "Success";
			} else {
				reply = "Failure#" + ret.getReason();
			}
			
			break;
		}
		case UPDATE: {

			UserManager.ReturnCode ret = um.updateUser(umsg.getUserid(), umsg.getPassword(), umsg.getRole());
			if(ret.isOK()) {
				reply = "Success";
			} else {
				reply = "Failure#" + ret.getReason();
			}
			
			break;
		}
		default: {
			reply = "Failure#Invalid operation.";
		}
		
		}
		
		return reply;
		
	} // processUserMsg

	protected String processACLMsg(ACLMsg aclmsg) {
		
		String reply = "";
		
		switch (aclmsg.getOp()) {

		case LIST: {

			ACLManager.ReturnCode ret = aclm.searchACLByID(aclmsg.getAclid());
			if(ret.isOK()) {
				reply = ret.getACL().toCSV();
			} else {
				reply = "failure#" + ret.getReason();
			}
			break;

		}
		case LISTALL: {

			ACLManager.ReturnCode ret = aclm.listAllACLs();
			if(ret.isOK()) {
				
				reply = ACL.ACLsToCSV(ret.getACLs());
				
			} else {
				reply = "failure#" + ret.getReason();
			}
			
			break;
		}
		
		case SEARCH: {

			ACLManager.ReturnCode ret = null;
			
			if(aclmsg.hasResource() && !aclmsg.hasRole()) {
				
				// search by resource
				ret = aclm.searchACLByResource(aclmsg.getResource());
				
			} else if (!aclmsg.hasResource() && aclmsg.hasRole()) {
				
				// search by role
				ret = aclm.searchACLByRole(aclmsg.getRole());
				
			} else if (aclmsg.hasResource() && aclmsg.hasRole()) {
				
				// search by both resource and role
				ret = aclm.searchACLByRR(aclmsg.getResource(), aclmsg.getRole());
				
			}
			
			 
			if(ret != null && ret.isOK()) {
				
				reply = ACL.ACLsToCSV(ret.getACLs());
				
			} else {
				reply = "failure#" + ret.getReason();
			}
			
			break;
		}
		case ADD: {

			ACLManager.ReturnCode ret = aclm.addACL(aclmsg.getResource(), aclmsg.getRole(), aclmsg.getAction());
			if(ret.isOK()) {
				reply = "success";
			} else {
				reply = "failure#" + ret.getReason();
			}
			
			break;
		}
		case REMOVE: {

			ACLManager.ReturnCode ret = aclm.removeACL(aclmsg.getAclid());
			if(ret.isOK()) {
				reply = "success";
			} else {
				reply = "failure#" + ret.getReason();
			}
			
			break;
		}
		case UPDATE: {

			ACLManager.ReturnCode ret = aclm.updateACL(aclmsg.getAclid(), aclmsg.getResource(), aclmsg.getRole(), aclmsg.getAction());
			if(ret.isOK()) {
				reply = "success";
			} else {
				reply = "failure#" + ret.getReason();
			}
			
			break;
		}
		default: {
			reply = "failure#Invalid operation.";
		}
		
		}
		
		return reply;
		
	}
	
} // ClientConsumer































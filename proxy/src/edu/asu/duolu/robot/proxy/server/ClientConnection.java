package edu.asu.duolu.robot.proxy.server;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import edu.asu.duolu.robot.proxy.log.LogManager;
import edu.asu.duolu.robot.proxy.server.acl.ACLManager;
import edu.asu.duolu.robot.proxy.server.user.User;
import edu.asu.duolu.robot.proxy.server.user.UserManager;

public class ClientConnection implements Runnable {

	protected Connection conn; // RabbitMQ connection
	protected Channel channel; // RabbitMQ channel

	protected ClientConnectionManager ccm;
	
	protected ClientMessageDispatcher cmd;
	
	protected RobotConnection rc;
	protected UserManager um;
	protected ACLManager aclm;
	protected LogManager lm;


	// queue name for talk to this client. tx and rx are from proxy perspective
	protected String txQueueName;
	protected String rxQueueName;

	protected User user;

	protected Thread ccThread = new Thread(this);
	protected boolean running = true;
	
	
	protected ClientConnection(ClientConnectionManager ccm, String txQueueName, String rxQueueName, User user, Connection conn, RobotConnection rc,
			UserManager um, ACLManager aclm, LogManager lm) {
		super();
		this.ccm = ccm;
		this.txQueueName = txQueueName;
		this.rxQueueName = rxQueueName;
		this.user = user;
		this.conn = conn;
		this.rc = rc;
		this.um = um;
		this.aclm = aclm;
		this.lm = lm;

		cmd = new ClientMessageDispatcher(this, rc, um, aclm, lm);
	}

	public String getTxQueueName() {
		return txQueueName;
	}

	public String getRxQueueName() {
		return rxQueueName;
	}

	public User getUser() {
		return user;
	}

	// CAUTION: This can only be called by the dispatcher, otherwise the the receiving thread will stuck.
	protected void disconnect() throws IOException {

		// stop the receving thread, remove the pair of queue
		running = false;
		channel.queueDelete(rxQueueName);
		channel.queueDelete(txQueueName);
		
		ccm.distroyClientConnection(user);
	}

	// CAUTION: reply() is only called by client message dispatcher
	protected synchronized void reply(String reply) throws IOException {
		
		System.out.println("[proxy -> " + user.getUserID() + "] " + reply);
		
		channel.basicPublish("", txQueueName, null, reply.getBytes());
	}
	
	// CAUTION: replyMethodReturn() is only called by robot connection
	protected synchronized void replyMethodReturn(String reply) {
		
		// TODO:
		
	}
	
	// CAUTION: replyEvent() is only called by robot connection
	protected synchronized void replyEvent(String reply) throws IOException {
		
		System.out.println("[proxy -> " + user.getUserID() + "] " + reply);
		channel.basicPublish("", txQueueName, null, reply.getBytes());
	}

	
	@Override
	public void run() {

		System.out.println("Client connection to " + user.getUserID() + " started.");

		try {

			channel = conn.createChannel();
			channel.queueDeclare(txQueueName, false, false, false, null);
			channel.queueDeclare(rxQueueName, false, false, false, null);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(rxQueueName, false, consumer);

			while (running) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				String message = new String(delivery.getBody());
				
				
				System.out.println("[proxy <- " + user.getUserID() + "] " + message);
				
				cmd.dispatch(message);

				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	

}

package edu.asu.duolu.robot.proxy.server;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import edu.asu.duolu.robot.proxy.log.LogManager;
import edu.asu.duolu.robot.proxy.message.InvalidMessageFormatException;
import edu.asu.duolu.robot.proxy.message.LoginMsg;
import edu.asu.duolu.robot.proxy.message.RequestMessage;
import edu.asu.duolu.robot.proxy.server.acl.ACLManager;
import edu.asu.duolu.robot.proxy.server.user.UserManager;

public class Listener implements Runnable {

	protected Connection conn; // RabbitMQ connection
	protected Channel channel; // RabbitMQ channel

	protected String QUEUE_NAME = "listen";

	protected ClientConnectionManager ccm;
	protected UserManager um;
	protected ACLManager aclm;
	protected LogManager lm;

	protected Thread lThread = new Thread(this);

	public Listener(Connection conn, ClientConnectionManager ccm, UserManager um, ACLManager aclm, LogManager lm) {

		this.conn = conn;
		this.ccm = ccm;
		this.um = um;
		this.aclm = aclm;
		this.lm = lm;
	}

	@Override
	public void run() {

		System.out.println("Proxy listener started.");
		
		try {

			channel = conn.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			channel.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, false, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				String message = new String(delivery.getBody());
				dispatch(message);

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

	public void start() {

		lThread.start();
	}

	protected void dispatch(String message) throws IOException {

		try {

			RequestMessage msg = RequestMessage.parseRequestMessage(message);
			if (!(msg instanceof LoginMsg)) {

				// Unexpected message received. Currently just ignore it.
				System.out.println("Unrecognized message!!!");

				return;
			}

			System.out.println("Login message received: " + message);

			LoginMsg lmsg = (LoginMsg) msg;

			String userid = lmsg.getUserid();
			String password = lmsg.getPassword();
			String clientQueueName = lmsg.getClientQueueName();

//			DeclareOk ok = channel.queueDeclarePassive(clientQueueName);
//			if (ok.getMessageCount() <= 0) {
//
//				// The queue declared by the client does not exist, just ignore
//				// the message.
//				
//				return;
//			}

			lm.addLog(userid, true, message);
			
			String reply = "";
			
			UserManager.ReturnCode ret = um.checkUser(userid, password);

			if (ret.isOK()) {

				// Check whether the user is already online
				ClientConnection dup = ccm.getClientConnection(userid);
				if (dup != null) {

					reply = "Failure#The user is already online!";

				} else {

					ClientConnection cc = ccm.newClientConnection(ret.getUser(), conn);

					// Start the client connection receive thread
					cc.ccThread.start();

					reply = "Success#tx=" + cc.getTxQueueName() + ";rx=" + cc.getRxQueueName();

				}

			} else {

				reply = ret.getReason();
				
				
			}
			
			channel.basicPublish("", clientQueueName, null, reply.getBytes());

			System.out.println("Login reply sent: " + reply);
			
			lm.addLog(userid, false, reply);
			
		} catch (InvalidMessageFormatException e) {

			// Unexpected message received.
			System.out.println("Unrecognized message: " + message);

		}

	} // dispatch

}

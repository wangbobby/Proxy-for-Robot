package edu.asu.duolu.robot.proxy.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class RobotConnection implements Runnable {

	// protected HashMap<Integer, ClientConnection> pendingRPC;

	// event source map to client connection
	protected HashMap<String, ArrayList<ClientConnection>> subscribers = new HashMap<String, ArrayList<ClientConnection>>();

	// user id + event source map to subscriber queue name
	protected HashMap<String, String> subQueues = new HashMap<String, String>();

	protected Connection conn;
	protected Channel channel;
	protected QueueingConsumer eventConsumer;

	protected Thread rcThread = new Thread(this);

	public RobotConnection(Connection conn) {

		this.conn = conn;

	}

	// CAUTION: Do not block!!!
	public synchronized String callMethod(String method, Map<String, String> argmap, String argstring,
			ClientConnection cc)
					throws IOException {

		String response = "";

		Channel c = conn.createChannel();
		try {
			// Check whether the method exists.
			
			c.queueDeclarePassive(method);
		} catch (IOException e) {

			
			c.abort();
			
			// Method does not exist.
			response = "Failure#No such method.";

			return response;
		}
		try {
			c.close();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}

		String replyQueueName = "";

		try {

			replyQueueName = cc.channel.queueDeclare().getQueue();
			QueueingConsumer consumer = new QueueingConsumer(cc.channel);
			cc.channel.basicConsume(replyQueueName, true, consumer);

			String corrId = UUID.randomUUID().toString();

			BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();

			cc.channel.basicPublish("", method, props, argstring.getBytes("UTF-8"));

			// TODO: Make it asynchronous! We should never block here!!!

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				if (delivery.getProperties().getCorrelationId().equals(corrId)) {
					response = new String(delivery.getBody(), "UTF-8");
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cc.channel.queueDelete(replyQueueName);
		}

		return response;
	}

	public synchronized String listenToEventSource(String eventSource, ClientConnection cc) throws IOException {

		String response = "";

		Channel c = conn.createChannel();
		try {
			// Check whether the eventSource exists.
			
			c.exchangeDeclarePassive(eventSource);
		} catch (IOException e) {

			
			c.abort();
			
			// eventSource does not exist.
			response = "Failure#No such event source.";

			return response;
		}
		try {
			c.close();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}

		// Keep track of subscriber
		ArrayList<ClientConnection> ccs = subscribers.get(eventSource);
		if (ccs == null) {
			ccs = new ArrayList<ClientConnection>();
		}
		ccs.add(cc);
		subscribers.put(eventSource, ccs);

		String queueName = cc.channel.queueDeclare().getQueue();
		cc.channel.queueBind(queueName, eventSource, "");
		// Also keep track of the queue
		subQueues.put(cc.getUser().getUserID() + "+" + eventSource, queueName);

		Consumer consumer = new DefaultConsumer(cc.channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");

				String eventSource = envelope.getExchange();

				ArrayList<ClientConnection> ccs = subscribers.get(eventSource);
				if (ccs != null) {

					for (ClientConnection cc : ccs) {
						cc.replyEvent(message);
					}

				}

			}
		};
		cc.channel.basicConsume(queueName, true, consumer);

		return "Success";
	}

	public synchronized String unlistenToEventSource(String eventSource, ClientConnection cc) throws IOException {

		String response = "";

		// Keep track of subscriber
		ArrayList<ClientConnection> ccs = subscribers.get(eventSource);
		if (ccs == null || !ccs.contains(cc)) {

			response = "Failure#You are not listening to the event source currently.";

			return response;
		}

		ccs.remove(cc);
		
		String queueName = subQueues.get(cc.getUser().getUserID() + "+" + eventSource);
		channel.queueUnbind(queueName, eventSource, "");
		channel.queueDelete(queueName);

		return "Success";
	}

	@Override
	public void run() {

		try {
			channel = conn.createChannel();

			// TODO: process received event here
			while (true) {

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		try {
			Connection connection = factory.newConnection();

			RobotConnection rc = new RobotConnection(connection);

			rc.run();

			String res = rc.callMethod("robot1.mctrl.setSpeed", null, "v=0;omega=0", null);

			System.out.println(res);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}

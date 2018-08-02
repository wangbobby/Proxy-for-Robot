package edu.asu.duolu.robot.proxy.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class Client {

	static String txQueueName = "xxx";
	static String rxQueueName = "yyy";

	static String clientQueue = "test-client";

	public static void main(String[] args) throws IOException, TimeoutException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException {
	      
			char[] keyPassphrase = "dkswlgur1!".toCharArray();
	      KeyStore ks = KeyStore.getInstance("PKCS12");
	      ks.load(new FileInputStream("/home/kyungyong/client/keycert.p12"), keyPassphrase);

	      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	      kmf.init(ks, keyPassphrase);

	      char[] trustPassphrase = "dkswlgur1!".toCharArray();
	      KeyStore tks = KeyStore.getInstance("JKS");
	      tks.load(new FileInputStream("/home/kyungyong/rabbitstore"), trustPassphrase);

	      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	      tmf.init(tks);

	      SSLContext c = SSLContext.getInstance("TLSv1.1");
	      c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		
		ConnectionFactory factory = new ConnectionFactory();
	      factory.setUsername("kyungyong");
	      factory.setPassword("1q2w3e4r");
	      //factory.setVirtualHost(virtualHost);
	      factory.setHost("localhost");
	      factory.setPort(5671);
	      factory.useSslProtocol(c); 

		Connection conn = factory.newConnection();
		Channel channel = conn.createChannel();

		channel.queueDeclare(clientQueue, false, false, false, null);

		// login

		String login = "login?userid=admin;password=admin;queue=test-client";
		channel.basicPublish("", "listen", null, login.getBytes());

		System.out.println("Login sent: " + login);
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(clientQueue, false, consumer);

		// wait for login relpy
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();

		String message = new String(delivery.getBody());
		System.out.println("Login reply received: " + message);

		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		
		// parse reply
		String[] ms = message.split("#");

		if (ms[0].equals("Success")) {

			String[] qs = ms[1].split(";");
			if (qs.length != 2) {

				System.out.println("Invalid login reply message format!!!");
			}
			for (String q : qs) {
				String[] strs = q.split("=");
				
				// CAUTION: Here tx and rx are from proxy's perspective.
				if (strs[0].equals("rx")) {
					txQueueName = strs[1];
				} else if (strs[0].equals("tx")) {
					rxQueueName = strs[1];
				} else {
					System.out.println("Invalid login reply message format!!!");
				}
			}
		} else {

			System.out.println("Login error!!!");
			return;
		}

		// login OK, start message communication

		
		
		// receiving...

		Consumer recv = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(message);
				
				if(message.equals("Bye!")) {
					System.exit(0);
				}
				
			}
		};
		channel.basicConsume(rxQueueName, true, recv);

		// sending...

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {

			String command = in.readLine();
			
			// System.out.println("Command: " + command);

			channel.basicPublish("", txQueueName, null, command.getBytes());
			
		}

	}

}
package edu.asu.duolu.robot.proxy.robot;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.rabbitmq.client.AMQP.BasicProperties;

public class Robot {

	public static void main(String[] argv) {
		RobotMotionControl robotmotioncon;
		Connection connection = null;
		Channel channel = null;

		String setSpeed = "robot1.mctrl.setSpeed";
		String getSpeed = "robot1.mctrl.getSpeed";
		String setAcc = "robot1.mctrl.setAcc";
		String getAcc = "robot1.mctrl.getAcc";
		String getLoc = "robot1.mctrl.getLocation";
		
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");

			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(setSpeed, false, false, false, null);
			channel.queueDeclare(getSpeed, false, false, false, null);
			channel.queueDeclare(setAcc, false, false, false, null);
			channel.queueDeclare(getAcc, false, false, false, null);
			channel.queueDeclare(getLoc, false, false, false, null);

			channel.basicQos(1);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(setSpeed, false, consumer);
			channel.basicConsume(getSpeed, false, consumer);
			channel.basicConsume(setAcc, false, consumer);
			channel.basicConsume(getAcc, false, consumer);
			channel.basicConsume(getLoc, false, consumer);

			robotmotioncon = new RobotMotionControl(connection);

			while (true) {
				String response = null;

				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId())
						.build();

				try {
					String message = new String(delivery.getBody(), "UTF-8");

					System.out.println("[proxy - > robot] " + message);
					
					// Parse message
					int numOfArgs = 0;
					Map<String, String> argmap = new TreeMap<String, String>();
					
					if (!message.equals("void")) {

						String[] args = message.split(";");

						numOfArgs = args.length;

						for (String arg : args) {
							String[] sstrs = arg.split("=");
							if (sstrs.length != 2) {
								// Invalid message.
								throw new InvalidMessageFormatException();
							}

							// Whatever before "=" is parameter name, and whatever after "="
							// is parameter value.
							argmap.put(sstrs[0], sstrs[1]);

						}
					}
					
					
					
					
					if (delivery.getEnvelope().getRoutingKey().equals(setSpeed)) {
						
						if(argmap.get("v") == null || argmap.get("omega") == null)
							throw new InvalidMessageFormatException();
						
						robotmotioncon.setSpeed(Double.parseDouble(argmap.get("v")),
								Double.parseDouble(argmap.get("omega")));
						response = "v=" + argmap.get("v") + ";omega="
								+ argmap.get("omega");
						
					} else if (delivery.getEnvelope().getRoutingKey().equals(getSpeed)) {
						
						
						double[] value = robotmotioncon.getSpeed();
						response = "v=" + value[0] + ";omega=" + value[1];
						
					} else if (delivery.getEnvelope().getRoutingKey().equals(setAcc)) {
						
						if(argmap.get("acc") == null)
							throw new InvalidMessageFormatException();
						
						robotmotioncon.setAcc(Double.parseDouble(argmap.get("acc")));
						response = "acc=" + argmap.get("acc");
						
					} else if (delivery.getEnvelope().getRoutingKey().equals(getAcc)) {
						
						
						double acc = robotmotioncon.getAcc();
						response = "acc=" + acc;
						
					} else if (delivery.getEnvelope().getRoutingKey().equals(getLoc)) {
						
						double[] value = robotmotioncon.getLocation();
						response = "x=" + value[0] + ";y=" + value[1];
					}
					

				} catch (InvalidMessageFormatException e) {
					System.out.println("Failure#" + e.toString());
					response = "Invalid RPC message format!!!";
				} catch (IOException e) {
					System.out.println("Failure#" + e.toString());
					response = "RPC error!!!";
				} finally {
					
					System.out.println("[robot - > proxy] " + response);
					
					channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ignore) {
				}
			}
		}

	}
}
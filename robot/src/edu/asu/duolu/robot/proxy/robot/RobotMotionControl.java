package edu.asu.duolu.robot.proxy.robot;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RobotMotionControl extends Thread {

	protected double speed, omega, acceleration, accomega, coordinatex, coordinatey, temporaryspeed;
	protected String locationmessage, speedmessage, accmessage;
	Connection conn;
	Channel channel;

	String reportLocation = "robot1.mctrl.reportLocation";
	String reportSpeed = "robot1.mctrl.reportSpeed";
	String reportAcc = "robot1.mctrl.reportAcc";
	
	RobotMotionControl(Connection conn) throws IOException {
		this.conn = conn;
		this.channel = conn.createChannel();
		this.locationmessage = "";
		this.speedmessage = "";
		this.accmessage = "";
		this.speed = 0;
		this.omega = 0;
		this.temporaryspeed = 0;
		this.acceleration = 0;
		this.accomega = 0;
		this.coordinatex = 0;
		this.coordinatey = 0;
		
				
		
		channel.exchangeDeclare(reportLocation, "fanout");
		channel.exchangeDeclare(reportSpeed, "fanout");
		channel.exchangeDeclare(reportAcc, "fanout");
		start();
	}

	protected void updateState() {
		
		
		temporaryspeed = speed;
		speed = Math.sqrt(Math
				.pow(speed * Math.cos(Math.toRadians(omega)) + acceleration * Math.cos(Math.toRadians(accomega)), 2)
				+ Math.pow(
						speed * Math.sin(Math.toRadians(omega)) + acceleration * Math.sin(Math.toRadians(accomega)),
						2));

		if (speed == 0 && acceleration == 0)
			;
		else {
			if ((accomega - 5) <= omega && omega <= (accomega + 5)) {
				omega = accomega;
			} else
				omega = Math.toDegrees(Math.atan(((temporaryspeed * Math.sin(Math.toRadians(omega)))
						+ (acceleration * Math.sin(Math.toRadians(accomega))))
						/ ((temporaryspeed * Math.cos(Math.toRadians(omega)))
								+ (acceleration * Math.cos(Math.toRadians(accomega))))));
		}

		coordinatex += speed * Math.cos(Math.toRadians(omega));
		coordinatey += speed * Math.sin(Math.toRadians(omega));

		
	}
	
	
	public void run() {
		while (true) {
			try {
				sleep(1000);
				locationmessage = reportLocation + "#x=" + this.coordinatex + ";y=" + this.coordinatey;
				speedmessage = reportSpeed + "#v=" + this.speed + ";omega=" + this.omega;
				accmessage = reportAcc + "#acc=" + this.acceleration;
				
				channel.basicPublish(reportLocation, "", null, locationmessage.getBytes());
				channel.basicPublish(reportSpeed, "", null, speedmessage.getBytes());
				channel.basicPublish(reportAcc, "", null, accmessage.getBytes());
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
			
			updateState();
		}
	}

	protected void setSpeed(double v, double o) {
		speed = v;
		omega = o;
		acceleration = 0;
		accomega = 0;
	}

	protected double[] getSpeed() {
		double[] value = new double[2];
		value[0] = this.speed;
		value[1] = this.omega;

		return value;
	}

	protected void setAcc(double acc) {
		acceleration = acc;
	}

	protected double getAcc() {
		
		return acceleration;
	}

	protected double[] getLocation() {
		double[] value = new double[2];
		value[0] = this.coordinatex;
		value[1] = this.coordinatey;

		return value;
	}

}
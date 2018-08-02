package edu.asu.duolu.robot.proxy.log;

import java.util.Calendar;
import java.util.Date;

public class Log implements Comparable<Log> {

	protected Date timestamp;
	protected String userid;
	protected boolean direction;
	protected String msg;
	
	protected Log(Date timestamp, String userid, boolean direction, String msg) {
		super();
		this.timestamp = timestamp;
		this.userid = userid;
		this.direction = direction;
		this.msg = msg;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMsg() {
		return msg;
	}

	@Override
	public String toString() {
		
		String direct;
		if(direction)
			direct = " <- ";
		else
			direct = " -> ";
		
		return "[" + timestamp + "] [proxy" + direct + userid + "] " + msg;
	}
	
	
	
	public static void main(String[] args) {
		
		
		Log l = new Log(Calendar.getInstance().getTime(), "system", true, "xxx");
		
		System.out.println(l);
		

	}

	@Override
	public int compareTo(Log other) {
		
		return timestamp.compareTo(other.timestamp);
	}
	
}

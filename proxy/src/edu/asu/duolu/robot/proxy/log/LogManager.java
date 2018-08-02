package edu.asu.duolu.robot.proxy.log;

import java.util.ArrayList;
import java.util.Calendar;

public class LogManager {

	protected ArrayList<Log> logs = new ArrayList<Log>();
	
	
	public void addLog(String userid, boolean direction, String msg) {
		
		logs.add(new Log(Calendar.getInstance().getTime(), userid, direction, msg));
		
	}
	
	public ArrayList<Log> listLog() {
		return logs;
	}
	
	
	public void syncToFile() {
		
		// TODO
		
		
	}
	
	
	
	public static void main(String[] args) {
		
		
		
		

	}
}

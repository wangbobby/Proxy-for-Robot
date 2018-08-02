package edu.asu.duolu.robot.proxy.sshproxy;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class Service implements Runnable
{
	public static final int backLog = 5;
	public static final int bufferSize = 4096;
	private volatile boolean onOff = true;
	private volatile int countClient = 0;	//client number count
	private int id = -1;
	private String header = "";
	private String log = "";
	private String error = "";
	private int srcPort;
	private int dstPort;
	private InetAddress srcAddress;
	private InetAddress dstAddress;
	private volatile ServerSocket serverSocket;
	private Object locker = new Object();
	private Socket socketOnServer;
	private Socket dstSocket;
	private DuplicateStream serverToRobot;
	private DuplicateStream robotToServer;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyy.MM.dd 'at' hh:mm:ss z");
	private static Date currentTime;
	
	public Service(int srcPort, InetAddress dstAddress, int dstPort)
	{
		try 
		{
			this.serverSocket = new ServerSocket(srcPort, backLog);
		} catch (IOException e) {
			System.out.println("Service Constructor Error!");
			e.printStackTrace();
		}
		
		this.srcAddress = srcAddress;
		this.srcPort = srcPort;
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
	}
	
	public Service(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort)
	{
		try 
		{
			this.serverSocket = new ServerSocket(srcPort, backLog, srcAddress);
		} catch (IOException e) {
			System.out.println("Service Constructor Error!");
			this.error += this.header + e + "\n\n";
			e.printStackTrace();
		}
		
		this.srcAddress = srcAddress;
		this.srcPort = srcPort;
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
		
		this.header = srcAddress.toString() + ":" + srcPort + " ---> " + dstAddress.toString()
				  + ":" + dstPort;
		
		this.log += getTimeString() + "   " + this.header + "\n  Start Record...\n\n";
		
		this.error += getTimeString() + "   " + this.header + "\n  Start Record...\n\n";
	}
	
	public Service(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort, 
				   String log, String error)
	{
		try 
		{
			this.serverSocket = new ServerSocket(srcPort, backLog, srcAddress);
		} catch (IOException e) {
			System.out.println("Service Constructor Error!");
			e.printStackTrace();
		}
		
		this.srcAddress = srcAddress;
		this.srcPort = srcPort;
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
		this.log = log;
		this.error = error;
		this.header = srcAddress.toString() + ": " + srcPort + " ---> " + dstAddress
					  + ": " + dstPort;
	}
	
	public void run() 
	{
		if(FrontPanel.debug)
		{
			System.out.println(getTimeString());
			System.out.println(header + " Start running...");
		}
		
		try
		{
			while(onOff)
			{
				//when client is more than the MAX_CLIENT_NUM, the server stops to accept
				if(countClient < FrontPanel.MAX_CLIENT_NUM)
				{
					//server's socket connected to the outside clients
					socketOnServer = serverSocket.accept();
									
					countClient++;
					
					//dstSocket -- robot
					dstSocket = new Socket(dstAddress, dstPort);
					
					serverToRobot = new DuplicateStream(socketOnServer, dstSocket);
					robotToServer = new DuplicateStream(dstSocket, socketOnServer);
					
					log += getTimeString() + "   " + socketOnServer.getInetAddress() + 
							" connected to the port " + this.srcPort + "...\n";
					
					synchronized(locker)
					{
						serverToRobot.start();
						robotToServer.start();
					}
				}
			}
			
			if(!onOff && FrontPanel.debug)
				System.out.println("run is set to off!");
			
		} catch(IOException ioe) {
			if(FrontPanel.debug)
				System.out.println("Exception in run method of Service class...");
			this.error += getTimeString() + "\n";
			this.error += this.header + ioe + "\n\n";
			ioe.printStackTrace();
		}
		
	}
	
	protected void stopService()
	{
		if(FrontPanel.debug)
			System.out.println(this.onOff);
		this.onOff = false;
			
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			this.error += this.header + e + "\n\n";
			e.printStackTrace();
		}

		this.socketOnServer = null;
		this.dstSocket = null;
		this.serverSocket = null;
		this.robotToServer = null;
		
		log += getTimeString() + "\n";
		log += header + " is Stoped...\n\n";
		
		if(FrontPanel.debug)
		{
			System.out.println(this.onOff);
			System.out.println("The server" +  this.serverSocket.getInetAddress() + "is turned off!");
		}
	}
	
	public static String getTimeString()
	{
		 currentTime = new Date();
		return sdf.format(currentTime);
	}
	
	protected class DuplicateStream extends Thread
	{
		private Socket sockIn;
		private Socket sockOut;
		
		public DuplicateStream(Socket sockIn, Socket sockOut) 
		{
			this.sockIn = sockIn;
			this.sockOut = sockOut;
		}
		
		public void run() 
		{
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			
			try
			{
				InputStream ips = sockIn.getInputStream();
				OutputStream ops = sockOut.getOutputStream();
				
				while((length = ips.read(buffer)) > 0 && onOff)
				{
					ops.write(buffer, 0, length);
					ops.flush();
				}
				if(!onOff)
					System.out.println("DuplicateStream is set to off!");
			
			}catch(Exception e) {
				if(FrontPanel.debug)
				{
					System.out.println("DuplicateStream run method Exception...");
					e.printStackTrace();
				}
				error += getTimeString() + "\n";
				error += header + e + "\n\n";
				countClient--;
				if(FrontPanel.debug)
					System.out.println("Connection between " + sockIn.getInetAddress() + " and " 
							+ sockOut.getInetAddress() + " break...");
			}finally {
				log += getTimeString() + "   Connection between " + sockIn.getInetAddress() + 
						" and " + sockOut.getInetAddress() + " break...\n";
			}
			
			try
			{
				sockIn.close();
				sockOut.close();
			}catch(Exception ee) {
				if(FrontPanel.debug)
					System.out.println("Socket Close Error!");
				error += getTimeString() + "\n";
				error += header + ee + "\n\n";
				ee.printStackTrace();
			}
		}
		
	}
	
	protected int getId()
	{
		return this.id;
	}
	
	protected void setId(int id)
	{
		this.id = id;
	}
	
	protected String getLog()
	{
		return this.log;
	}
	
	protected String getError()
	{
		return this.error;
	}
	
	protected int getSrcPort()
	{
		return this.srcPort;
	}
	
	protected String getSrcAddress()
	{
		return this.srcAddress.toString();
	}
}

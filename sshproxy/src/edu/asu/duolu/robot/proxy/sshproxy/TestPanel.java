package edu.asu.duolu.robot.proxy.sshproxy;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.*;

public class TestPanel extends JFrame
{
	static JFrame frame;
	static FrontPanel fp;
   public static void main(String[] args)
   {
      frame = new JFrame("Proxy Server");
   
      fp = new FrontPanel();
      frame.getContentPane().add(fp);
   	
      frame.pack();
      frame.setVisible(true);
      
      frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter()
      	{
    	  public void windowClosing(WindowEvent e)
    	  {
    		  try {
				printFile("log.txt", fp.getFinalLog());
				printFile("error.txt", fp.getFinalError());
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally{
				System.exit(0);
			}
    	  }
      	});      
   }
    
   private static void printFile(String file, String content) throws IOException
   {
      FileWriter fw = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter outFile = new PrintWriter(bw);
      outFile.print(content);
      outFile.close();
   }

}
package edu.asu.duolu.robot.proxy.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.concurrent.TimeoutException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.rabbitmq.client.*;

import edu.asu.duolu.robot.proxy.log.LogManager;
import edu.asu.duolu.robot.proxy.server.acl.ACLManager;
import edu.asu.duolu.robot.proxy.server.user.UserManager;

public class Proxy {

	protected UserManager um = new UserManager();
	protected ACLManager aclm = new ACLManager();
	protected LogManager lm = new LogManager();

	protected ClientConnectionManager ccm;
	protected Listener lc;
	protected RobotConnection rc;

	
	public Proxy() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
	FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException, TimeoutException {
		
		// Initialize SSL.

//		char[] keyPassphrase = "dkswlgur1!".toCharArray();
//		KeyStore ks = KeyStore.getInstance("PKCS12");
//		ks.load(new FileInputStream("/home/kyungyong/client/keycert.p12"), keyPassphrase);
//
//		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//		kmf.init(ks, keyPassphrase);
//
//		char[] trustPassphrase = "dkswlgur1!".toCharArray();
//		KeyStore tks = KeyStore.getInstance("JKS");
//		tks.load(new FileInputStream("/home/kyungyong/rabbitstore"), trustPassphrase);
//
//		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//		tmf.init(tks);
//
//		SSLContext c = SSLContext.getInstance("TLSv1.1");
//		c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//		
//		// Connect to RabbitMQ server.
//		
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setUsername("kyungyong");
//		factory.setPassword("1q2w3e4r");
//		// factory.setVirtualHost(virtualHost);
//		factory.setHost("localhost");
//		factory.setPort(5671);
//		factory.useSslProtocol(c);

		// Non-SSL
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
		
		Connection conn = factory.newConnection();
		
		rc = new RobotConnection(conn);
		
		
		ccm = new ClientConnectionManager(rc, um, aclm, lm);
		
		
		
		lc = new Listener(conn, ccm, um, aclm, lm);
		
		
	}
	
	public static void main(String[] args) throws Exception  {

		Proxy p = new Proxy();
		
		p.lc.lThread.start();
		
		p.rc.rcThread.start();
		
		p.lc.lThread.join();
		
		
		
	}

}

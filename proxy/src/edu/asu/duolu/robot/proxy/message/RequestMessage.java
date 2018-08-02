package edu.asu.duolu.robot.proxy.message;

public class RequestMessage {

	
	
	
	public RequestMessage(String first, String last) throws InvalidMessageFormatException {
		
	}
	
	
	
	public static RequestMessage parseRequestMessage(String message) throws InvalidMessageFormatException {
		
		
		String[] strs = message.split("\\?");

		if (strs.length != 2) {

			// Invalid message.
			throw new InvalidMessageFormatException();
		}
		
		String first = strs[0];
		String last = strs[1];
		
		String[] firsts = strs[0].split("\\.");
		
		String cat = firsts[0];
		
		if(cat.equals("login")) {
			
			return new LoginMsg(first, last);
			
		} else if(cat.equals("logout")) {
			
			return new LogoutMsg(first, last);
			
		} else if(cat.equals("listen") || cat.equals("unlisten")) {
			
			return new ListenMsg(first, last);
			
		} else if(cat.equals("acl")) {
			
			return new ACLMsg(first, last);
			
		} else if(cat.equals("user")) {
			
			return new UserMsg(first, last);
			
		} else if(cat.equals("log")) {
			
			return new LogMsg(first, last);
			
		} else {
			
			// All other format may be method call message, because object name can be arbitrary.
			return new MethodCallMsg(first, last);
		}
		
		
	}
	
	
	// test
	public static void main(String[] args) throws InvalidMessageFormatException {
		
		// login
		
		RequestMessage msg1 = parseRequestMessage("login?userid=xxx;password=yyy;queue=zzz");
		System.out.println(msg1);
		System.out.println();

		// logout
		
		RequestMessage msg2 = parseRequestMessage("logout?void");
		System.out.println(msg2);
		System.out.println();
		
		// method call
		
		RequestMessage msg3 = parseRequestMessage("robotx.object.method?arg1=value1;arg2=value2");
		System.out.println(msg3);
		System.out.println();

		RequestMessage msg4 = parseRequestMessage("robotx.object.method?void");
		System.out.println(msg4);
		System.out.println();

		// event source
		
		RequestMessage msg5 = parseRequestMessage("listen?robotx.object.eventsource");
		System.out.println(msg5);
		System.out.println();
		
		RequestMessage msg6 = parseRequestMessage("unlisten?robotx.object.eventsource");
		System.out.println(msg6);
		System.out.println();
	
		// user
		
		RequestMessage msg7 = parseRequestMessage("user.list?userid=id");
		System.out.println(msg7);
		System.out.println();

		RequestMessage msg8 = parseRequestMessage("user.listall?void");
		System.out.println(msg8);
		System.out.println();

		RequestMessage msg9 = parseRequestMessage("user.add?userid=id;password=pw;role=free");
		System.out.println(msg9);
		System.out.println();

		RequestMessage msg10 = parseRequestMessage("user.remove?userid=id");
		System.out.println(msg10);
		System.out.println();

		RequestMessage msg11 = parseRequestMessage("user.update?userid=id;password=pw_new;role=role_new");
		System.out.println(msg11);
		System.out.println();

		// ACL
		
		RequestMessage msg12 = parseRequestMessage("acl.list?aclid=123");
		System.out.println(msg12);
		System.out.println();

		RequestMessage msg13 = parseRequestMessage("acl.listall?void");
		System.out.println(msg13);
		System.out.println();

		RequestMessage msg14 = parseRequestMessage("acl.search?resource=x.y.*");
		System.out.println(msg14);
		System.out.println();

		RequestMessage msg15 = parseRequestMessage("acl.search?role=abc");
		System.out.println(msg15);
		System.out.println();

		RequestMessage msg16 = parseRequestMessage("acl.add?resource=xxx;role=abc;action=allow");
		System.out.println(msg16);
		System.out.println();

		RequestMessage msg17 = parseRequestMessage("acl.remove?aclid=123");
		System.out.println(msg17);
		System.out.println();

		RequestMessage msg18 = parseRequestMessage("acl.update?aclid=123;resource=xxx;role=abc;action=allow");
		System.out.println(msg18);
		System.out.println();
		
		RequestMessage msg19 = parseRequestMessage("aclx.update?aclid=123;resource=xxx;role=abc;action=allow");
		System.out.println(msg19);
		System.out.println();
		
		
	}
	
}

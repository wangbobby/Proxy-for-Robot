package edu.asu.duolu.robot.proxy.message;

public class LoginMsg extends RequestMessage {

	protected String userid;
	protected String password;
	protected String clientQueueName;

	public LoginMsg(String first, String last) throws InvalidMessageFormatException {

		super(first, last);
		
		// login?userid=xxx;password=yyy;queue=xxx


		if (first.equals("login")) {

			String[] args = last.split(";");
			if (args.length != 3) {
				// Invalid message format.
				throw new InvalidMessageFormatException();
			}

			for (String arg : args) {
				String[] sstrs = arg.split("=");
				if (sstrs.length != 2) {
					// Invalid message.
					throw new InvalidMessageFormatException();
				}
				if(sstrs[0].equals("userid")) {
					userid = sstrs[1];
				} else if(sstrs[0].equals("password")) {
					password = sstrs[1];
				} else if(sstrs[0].equals("queue")) {
					clientQueueName = sstrs[1];
				} else {
					// Invalid message.
					throw new InvalidMessageFormatException();					
				}
			}
		} else {
			// Invalid message.
			throw new InvalidMessageFormatException();					
		}

	}
	
	public String getUserid() {
		return userid;
	}

	public String getPassword() {
		return password;
	}

	public String getClientQueueName() {
		return clientQueueName;
	}

	@Override
	public String toString() {
		return "LoginMsg [userid=" + userid + ", password=" + password + ", clientQueueName=" + clientQueueName + "]";
	}


}

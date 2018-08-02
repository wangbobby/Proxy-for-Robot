package edu.asu.duolu.robot.proxy.message;

public class UserMsg extends RequestMessage {
	
	public enum Operation {
		LIST, LISTALL, ADD, REMOVE, UPDATE
	}

	protected Operation op;
	protected String userid;
	protected String password;
	protected String role;

	protected int numOfArgs = 0;
	protected boolean hasUserID = false;
	protected boolean hasPassword = false;
	protected boolean hasRole = false;

	public UserMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);

		// Parse the argument list first.

		if (!last.equals("void")) {
			String[] args = last.split(";");
			numOfArgs = args.length;
			if (numOfArgs > 3) {
				throw new InvalidMessageFormatException();
			}
			for (String arg : args) {
				String[] sstrs = arg.split("=");
				if (sstrs.length != 2) {
					// Invalid message.
					throw new InvalidMessageFormatException();
				}
				if (sstrs[0].equals("userid")) {
					userid = sstrs[1];
					hasUserID = true;
				} else if (sstrs[0].equals("password")) {
					password = sstrs[1];
					hasPassword = true;
				} else if (sstrs[0].equals("role")) {
					role = sstrs[1];
					hasRole = true;
				} else {
					// Invalid message.
					throw new InvalidMessageFormatException();
				}
			}
		}

		// Then parse the operation code to check error

		String[] firsts = first.split("\\.");
		if (firsts.length != 2) {
			throw new InvalidMessageFormatException();
		}
		if (firsts[1].equals("list")) {

			// user.list?userid=id

			if (!(numOfArgs == 1 && hasUserID && !hasPassword && !hasRole)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.LIST;

		} else if (firsts[1].equals("listall")) {

			// user.listall?void

			if (!(numOfArgs == 0 && !hasUserID && !hasPassword && !hasRole)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.LISTALL;

		} else if (firsts[1].equals("add")) {

			// user.add?userid=id;password=pw;role=free

			if (!(numOfArgs == 3 && hasUserID && hasPassword && hasRole)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.ADD;

		} else if (firsts[1].equals("remove")) {

			// user.remove?userid=id

			if (!(numOfArgs == 1 && hasUserID && !hasPassword && !hasRole)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.REMOVE;

		} else if (firsts[1].equals("update")) {

			// user.update?userid=id;password=pw_new;role=role_new

			if (!(numOfArgs == 3 && hasUserID && hasPassword && hasRole)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.UPDATE;

		} else {
			// Invalid message.
			throw new InvalidMessageFormatException();
		}

	}

	public Operation getOp() {
		return op;
	}

	public String getUserid() {
		return userid;
	}

	public String getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}

	public int getNumOfArgs() {
		return numOfArgs;
	}

	public boolean hasUserID() {
		return hasUserID;
	}

	public boolean hasPassword() {
		return hasPassword;
	}

	public boolean hasRole() {
		return hasRole;
	}

	@Override
	public String toString() {
		return "UserMsg [op=" + op + ", userid=" + userid + ", password=" + password + ", role=" + role + ", numOfArgs="
				+ numOfArgs + ", hasUserID=" + hasUserID + ", hasPassword=" + hasPassword + ", hasRole=" + hasRole
				+ "]";
	}

}

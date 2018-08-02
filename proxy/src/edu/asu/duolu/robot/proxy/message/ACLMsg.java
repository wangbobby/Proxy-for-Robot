package edu.asu.duolu.robot.proxy.message;

public class ACLMsg extends RequestMessage {

	public enum Operation {
		LIST, LISTALL, SEARCH, ADD, REMOVE, UPDATE
	}

	protected Operation op;
	protected int aclid;
	protected String resource;
	protected String role;
	protected String action;

	protected int numOfArgs = 0;
	protected boolean hasACLID = false;
	protected boolean hasResource = false;
	protected boolean hasRole = false;
	protected boolean hasAction = false;

	public ACLMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);

		// Parse the argument list first.
		
		
		if (!last.equals("void")) {
			String[] args = last.split(";");
			numOfArgs = args.length;
			if(numOfArgs > 4) {
				throw new InvalidMessageFormatException();
			}
			for (String arg : args) {
				String[] sstrs = arg.split("=");
				if (sstrs.length != 2) {
					// Invalid message.
					throw new InvalidMessageFormatException();
				}
				if (sstrs[0].equals("aclid")) {
					try {
						aclid = Integer.parseInt(sstrs[1]);
						hasACLID = true;
					} catch (NumberFormatException e) {
						throw new InvalidMessageFormatException();
					}
				} else if (sstrs[0].equals("resource")) {
					resource = sstrs[1];
					hasResource = true;
				} else if (sstrs[0].equals("role")) {
					role = sstrs[1];
					hasRole = true;
				} else if (sstrs[0].equals("action")) {
					action = sstrs[1];
					hasAction = true;
					
					// Action must be either "allow" or "deny"
					if(!(action.equals("allow") || action.equals("deny"))) {
						throw new InvalidMessageFormatException();
					}
					
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
			
			// acl.list?aclid=id
			
			if(!(numOfArgs == 1 && hasACLID && !hasResource && !hasRole && !hasAction)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.LIST;

		} else if (firsts[1].equals("listall")) {
			
			// acl.listall?void
			
			if(!(numOfArgs == 0 && !hasACLID && !hasResource && !hasRole && !hasAction)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.LISTALL;

		} else if (firsts[1].equals("search")) {
			
			// acl.search?resource=x.y.*
			// acl.search?role=abc
			
			if(!(numOfArgs == 1 && !hasACLID && (hasResource || hasRole) && !hasAction)) {
				throw new InvalidMessageFormatException();
			}
			
			op = Operation.SEARCH;

		} else if (firsts[1].equals("add")) {

			// acl.add?resource=xxx;role=abc;action=[“allow” or “deny”]
			
			if(!(numOfArgs == 3 && !hasACLID && hasResource && hasRole && hasAction)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.ADD;
			
		} else if (firsts[1].equals("remove")) {

			// acl.remove?aclid=id
			
			if(!(numOfArgs == 1 && hasACLID && !hasResource && !hasRole && !hasAction)) {
				throw new InvalidMessageFormatException();
			}
			op = Operation.REMOVE;
			
		} else if (firsts[1].equals("update")) {

			// acl.update?aclid=id;resource=xxx;role=abc;action=[“allow” or “deny”]
			
			if(!(numOfArgs == 4 && hasACLID && hasResource && hasRole && hasAction)) {
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

	public int getAclid() {
		return aclid;
	}

	public String getResource() {
		return resource;
	}

	public String getRole() {
		return role;
	}

	public String getAction() {
		return action;
	}

	public int getNumOfArgs() {
		return numOfArgs;
	}

	public boolean hasACLID() {
		return hasACLID;
	}

	public boolean hasResource() {
		return hasResource;
	}

	public boolean hasRole() {
		return hasRole;
	}

	public boolean hasAction() {
		return hasAction;
	}

	@Override
	public String toString() {
		return "ACLMsg [op=" + op + ", aclid=" + aclid + ", resource=" + resource + ", role=" + role + ", action="
				+ action + ", numOfArgs=" + numOfArgs + ", hasACLID=" + hasACLID + ", hasResource=" + hasResource
				+ ", hasRole=" + hasRole + ", hasAction=" + hasAction + "]";
	}
	
	

}

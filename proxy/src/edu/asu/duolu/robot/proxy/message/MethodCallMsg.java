package edu.asu.duolu.robot.proxy.message;

import java.util.Map;
import java.util.TreeMap;

public class MethodCallMsg extends RequestMessage {

	protected String method;
	protected String argstring;
	protected Map<String, String> argv = new TreeMap<String, String>();
	protected int numOfArgs = 0;

	public MethodCallMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);

		// robotx.object.method?arg1=value1;arg2=value2
		// robotx.object.method?void

		method = first;
		argstring = last;

		if (!last.equals("void")) {

			String[] args = last.split(";");

			numOfArgs = args.length;

			for (String arg : args) {
				String[] sstrs = arg.split("=");
				if (sstrs.length != 2) {
					// Invalid message.
					throw new InvalidMessageFormatException();
				}

				// Whatever before "=" is parameter name, and whatever after "="
				// is parameter value.
				argv.put(sstrs[0], sstrs[1]);

			}
		}

	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getArgv() {
		return argv;
	}

	public int getNumOfArgs() {
		return numOfArgs;
	}

	public String getArgstring() {
		return argstring;
	}

	@Override
	public String toString() {
		return "MethodCallMsg [method=" + method + ", argv=" + argv + "]";
	}

}

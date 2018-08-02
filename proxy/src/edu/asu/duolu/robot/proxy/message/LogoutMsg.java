package edu.asu.duolu.robot.proxy.message;

public class LogoutMsg extends RequestMessage {

	public LogoutMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);

		// No need to parse anything
	}

	@Override
	public String toString() {
		return "LogoutMsg []";
	}

}

package edu.asu.duolu.robot.proxy.message;

public class LogMsg extends RequestMessage {

	public LogMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);
		
		if (!last.equals("void")) {
			throw new InvalidMessageFormatException();
		}
		
		if(!first.equals("log.list")) {
			throw new InvalidMessageFormatException();
		}
		
	}

	@Override
	public String toString() {
		return "LogMsg []";
	}

}

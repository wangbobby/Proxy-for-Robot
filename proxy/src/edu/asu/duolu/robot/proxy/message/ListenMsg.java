package edu.asu.duolu.robot.proxy.message;

public class ListenMsg extends RequestMessage {

	protected boolean listen;
	protected String eventSource;
	
	public ListenMsg(String first, String last) throws InvalidMessageFormatException {
		super(first, last);
		
		// listen?robotx.object.eventsource
		// unlisten?robotx.object.eventsource
		
		if(first.equals("listen")) {
			listen = true;
		} else if(first.equals("unlisten")) {
			listen = false;
		} else {
			// Invalid message format.
			throw new InvalidMessageFormatException();
		}
		eventSource = last;
		
	}

	public boolean isListen() {
		return listen;
	}

	public String getEventSource() {
		return eventSource;
	}

	@Override
	public String toString() {
		return "ListenMsg [listen=" + listen + ", eventSource=" + eventSource + "]";
	}



}

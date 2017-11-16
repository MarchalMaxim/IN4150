package exercises.exercise1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Message extends UnicastRemoteObject implements MessageRMI {
	
	private final Integer origin;
	
	private final MessageType type;
	
	private final Integer time;
	
	public Message(Integer origin, MessageType type, Integer time) throws RemoteException {
		this.origin = origin;
		this.type = type;
		this.time = time;
	}

	@Override
	public Integer getOrigin() {
		return this.origin;
	}
	
	@Override
	public MessageType getType() {
		return this.type;
	}

	@Override
	public Integer getTime() {
		return this.time;
	}
	
}

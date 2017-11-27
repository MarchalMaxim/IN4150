package exercises.exercise1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class Message extends UnicastRemoteObject implements MessageRMI {
	
	private final Integer origin;
	
	private final MessageType type;
	
	private final Integer time;
	
	private final Integer content;
	
	public Message(Integer origin, MessageType type, Integer time) throws RemoteException {
		this.origin = origin;
		this.type = type;
		this.time = time;
		this.content = new Random().nextInt(42);
	}
	
	public Message(Integer origin, MessageType type) throws RemoteException{
		this.origin = origin;
		this.type = type;
		this.time = 0;
		this.content = new Random().nextInt(42);
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

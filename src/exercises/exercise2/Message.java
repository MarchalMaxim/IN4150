package exercises.exercise2;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import exercises.exercise1.MessageRMI;
import exercises.exercise1.MessageType;

public class Message extends UnicastRemoteObject implements MessageRMI {
	private final Integer origin, time;
	private Integer content;
	private final MessageType type;
	private static final long serialVersionUID = 7526472295622776147L;  // unique id
	
	public Message(Integer origin, MessageType type, Integer time) throws RemoteException {
		this.origin = origin;
		this.type = type;
		this.time = time;
		this.content = new Random().nextInt(42);
	}
	public Message(Integer origin, MessageType type) throws RemoteException {
		this.origin = origin;
		this.type = type;
		this.time = 0;
		this.content = new Random().nextInt(42);
	}
	
	public Integer getContent() {
		return this.content;
	}
	public void setContent(int content) {
		this.content = content;
	}
	
	@Override
	public Integer getOrigin() throws RemoteException {
		return this.origin;
	}

	@Override
	public MessageType getType() throws RemoteException {
		return this.type;
	}

	@Override
	public Integer getTime() throws RemoteException {
		return 0;
	}

}

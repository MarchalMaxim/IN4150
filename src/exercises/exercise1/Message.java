package exercises.exercise1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class Message extends UnicastRemoteObject implements MessageRMI {
	
	private final Integer origin;
	
	private final MessageType type;
	
	private final Integer time;
	
	private final Integer content;
	
	private final MessageRMI originalMessage;
	
	public Message(Integer origin, MessageType type, Integer time, Integer content, MessageRMI originalMessage) throws RemoteException {
		this.origin = origin;
		this.type = type;
		this.time = time;
		this.content = content;
		this.originalMessage = originalMessage;
	}
	
	public Message(Integer origin, MessageType type, Integer time, Integer content) throws RemoteException {
		this(origin, type, time, content, null);
	}
	
	public Message(Integer origin, MessageType type, Integer time) throws RemoteException {
		this(origin, type, time, new Random().nextInt(42));
	}
	
	public Message(Integer origin, MessageType type) throws RemoteException {
		this(origin, type, 0);
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
	
	@Override
	public Integer getContent() {
		return this.content;
	}

	@Override
	public MessageRMI getOriginalMessage() {
		return this.originalMessage;
	}
	
	// Java peculiarities...
	// hashCode and equals is overrided by RemoteObject class
	// So, implementing them is useless
	
	/*@Override
	public int hashCode() {
		String str = "" + this.origin + this.type.getValue() + this.time + this.content;
		return Integer.parseInt(str);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Message other = (Message) obj;
		if (!Objects.equals(this.origin, other.origin)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		if (!Objects.equals(this.time, other.time)) {
			return false;
		}
		if (!Objects.equals(this.content, other.content)) {
			return false;
		}
		return true;
	}*/
	
}

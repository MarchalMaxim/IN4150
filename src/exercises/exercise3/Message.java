package exercises.exercise3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class to implement the message for RMI.
 */
public class Message extends UnicastRemoteObject implements MessageRMI {
	
	private final Integer senderId;
	
	private final MessageType type;
	
	private final Integer contentLevel;
	
	private final Integer contentId;
	
	/**
	 * Constructor.
	 * @param senderId - id of the sender
	 * @param type - type of the message
	 * @param contentLevel - level of the message
	 * @param contentId - id of the content
	 * @throws RemoteException - something went wrong
	 */
	public Message(Integer senderId, MessageType type, Integer contentLevel, Integer contentId) throws RemoteException {
		this.senderId = senderId;
		this.type = type;
		this.contentLevel = contentLevel;
		this.contentId = contentId;
	}

	@Override
	public Integer getSenderId() {
		return this.senderId;
	}
	
	@Override
	public MessageType getType() {
		return this.type;
	}
	
	@Override
	public Integer getContentLevel() {
		return this.contentLevel;
	}
	
	@Override
	public Integer getContentId() {
		return this.contentId;
	}
	
}

package exercises.exercise3;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class for the message in RMI.
 */
public interface MessageRMI extends Remote {
	
	/**
	 * Get the id of the sender.
	 * @return id of the sender
	 * @throws RemoteException - something went wrong
	 */
	public Integer getSenderId() throws RemoteException;
	
	/**
	 * Get the type of the message.
	 * @return type
	 * @throws RemoteException - something went wrong
	 */
	public MessageType getType() throws RemoteException;
	
	/**
	 * Get the level of the message.
	 * @return level of the content
	 * @throws RemoteException - something went wrong
	 */
	public Integer getContentLevel() throws RemoteException;
	
	/**
	 * Get the id of the content.
	 * @return id of the content
	 * @throws RemoteException 
	 */
	public Integer getContentId() throws RemoteException;
			
}

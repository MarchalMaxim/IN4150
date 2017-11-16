package exercises.exercise1;

import java.rmi.Remote;

public interface ProcessRMI extends Remote {
	
	/**
	 * Event triggers when a BROADCAST message is received
	 * @param message 
	 * @throws java.rmi.RemoteException 
	 */
	public void onReceived(MessageRMI message) throws java.rmi.RemoteException;
	
}

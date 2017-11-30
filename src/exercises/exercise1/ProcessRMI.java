package exercises.exercise1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessRMI extends Remote {
	
	/**
	 * Event triggers when a BROADCAST message is received
	 * @param message 
	 * @throws java.rmi.RemoteException 
	 */
	public void onReceived(MessageRMI message) throws RemoteException;
	
}

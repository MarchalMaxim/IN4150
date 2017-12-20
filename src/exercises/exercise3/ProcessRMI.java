package exercises.exercise3;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class to send a process through RMI.
 */
public interface ProcessRMI extends Remote {
	
	/**
	 * Event triggers when a message is received.
	 * @param message 
	 * @throws java.rmi.RemoteException 
	 * @throws java.lang.InterruptedException 
	 */
	public void onReceived(MessageRMI message) throws RemoteException, InterruptedException;
	
}

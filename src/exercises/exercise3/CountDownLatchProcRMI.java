package exercises.exercise3;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Countdown latch for syncing processes.
 */
public interface CountDownLatchProcRMI extends Remote {
	
	/**
	 * Call countDown in the latch.
	 * @throws RemoteException 
	 */
	public void countDown() throws RemoteException;
	
	/**
	 * Await for the rest of processes.
	 * @throws RemoteException - something wrong
	 * @throws InterruptedException 
	 */
	public void await() throws RemoteException, InterruptedException;
	
	/**
	 * Reset the countdown latch.
	 * @throws RemoteException 
	 */
	public void reset() throws RemoteException;
	
}

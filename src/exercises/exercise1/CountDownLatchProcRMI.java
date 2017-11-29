package exercises.exercise1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CountDownLatchProcRMI extends Remote {
	
	public void countDown() throws RemoteException;
	
	public void await() throws RemoteException, InterruptedException;
	
	public void reset() throws RemoteException;
	
}

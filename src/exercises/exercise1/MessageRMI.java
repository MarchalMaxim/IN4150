package exercises.exercise1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageRMI extends Remote {
	
	public Integer getOrigin() throws RemoteException;
	
	public MessageType getType() throws RemoteException;
	
	public Integer getTime() throws RemoteException;
	
}

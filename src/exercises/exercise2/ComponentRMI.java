package exercises.exercise2;
import java.rmi.*;
import exercises.exercise1.*;

public interface ComponentRMI extends Remote{
	public void onReceive(MessageRMI message) throws RemoteException;
	public void debugPrint() throws RemoteException;
	public void printId() throws RemoteException;
}

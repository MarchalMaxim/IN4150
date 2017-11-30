package exercises.exercise1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

public class CountDownLatchProc extends UnicastRemoteObject implements CountDownLatchProcRMI {

	private final int initialCount;
	private transient CountDownLatch latch;
	
	public CountDownLatchProc(int initialCount) throws RemoteException {
		this.initialCount = initialCount;
		this.latch = new CountDownLatch(initialCount);
	}

	@Override
	public void countDown() throws RemoteException {
		this.latch.countDown();
	}

	@Override
	public void await() throws RemoteException, InterruptedException {
		this.latch.await();
	}
	
	@Override
	public void reset() throws RemoteException {
		this.latch = new CountDownLatch(this.initialCount);
	}
	
}

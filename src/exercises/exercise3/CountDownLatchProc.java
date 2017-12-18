package exercises.exercise3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

/**
 * Countdown latch to sync processes.
 */
public class CountDownLatchProc extends UnicastRemoteObject implements CountDownLatchProcRMI {

	private final int initialCount;
	private transient CountDownLatch latch;
	
	/**
	 * Constructor.
	 * @param initialCount - initial count
	 * @throws RemoteException 
	 */
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

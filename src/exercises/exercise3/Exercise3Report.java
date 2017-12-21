package exercises.exercise3;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to execute multiple process in the same JVM.
 */
public final class Exercise3Report {
	
	// Report stadistics
	public static volatile int captureMessages;
	public static volatile int killedMessages;
	public static volatile int ackMessages;
	public static volatile int maxLevel = 1;
	public static volatile Map<Integer, Integer> capturedTimes = new HashMap<>();
	
	private Exercise3Report() {
		throw new RuntimeException("No supported");
	}
	
	/**
	 * Method to start multiple threads.
	 * @param args 
	 * @throws java.lang.InterruptedException 
	 * @throws java.rmi.RemoteException 
	 * @throws java.rmi.AlreadyBoundException 
	 */
	public static void main(String[] args) throws InterruptedException, RemoteException, AlreadyBoundException {
		int numOrdinary = Integer.parseInt(args[0]);
		int numCandidates = Integer.parseInt(args[1]);
		int numProcesses = numCandidates + numOrdinary;
		
		// Setup registry
		String[] registryParams = {
				String.valueOf(numProcesses),
				"-1",
				String.valueOf(numOrdinary),
				String.valueOf(numCandidates)
		};
		Exercise3.main(registryParams);
		
		// Start the rest of processes
		Thread[] threads = new Thread[numProcesses];
		for (int i = 0; i < numProcesses; i++) {
			final int procId = i;
			
			threads[i] = new Thread(() -> {
				try {
					String[] params = {
						String.valueOf(numProcesses),
						String.valueOf(procId),
						String.valueOf(numOrdinary),
						String.valueOf(numCandidates)
					};
					
					Exercise3 exercise3 = new Exercise3();
					exercise3.execute(params);
				} catch (RemoteException | AlreadyBoundException | InterruptedException ex) {
					Logger.getLogger(Exercise3Report.class.getName()).log(Level.SEVERE, null, ex);
				}
			});
			
			// Start thread
			threads[i].start();
		}
		
		// Join all threads
		for (int j = 0; j < numProcesses; j++) {
			threads[j].join();
		}
	}
	
}

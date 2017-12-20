package exercises.exercise3;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to execute multiple process in the same JVM.
 */
public final class Exercise3Report {
	
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
		int numProcesses = Integer.parseInt(args[0]);
		
		// Setup registry
		String[] registryParams = {
				args[0],
				"-1"
		};
		Exercise3.main(registryParams);
		
		Thread[] threads = new Thread[numProcesses];
		for (int i = 0; i < numProcesses; i++) {
			
			final int totalProcesses = numProcesses;
			final int procId = i;
			
			threads[i] = new Thread(() -> {
				try {
					String[] params = {
						String.valueOf(totalProcesses),
						String.valueOf(procId)
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

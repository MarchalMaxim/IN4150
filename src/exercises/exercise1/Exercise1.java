package exercises.exercise1;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exercise1 {
	
	// Registry URL
	public static final String HOST = "localhost";
	public static final Integer PORT = 1099;
	
	public static int numProcesses;
	public static int procId;
	
	public static Registry reg;
	
	private static boolean validateParams(String[] args) {
		// Check num of params
		switch (args.length) {
			case 0:
				System.out.println("Error: No parameters found");
				return false;
			case 1:
				System.out.println("Error: At least 2 parameters");
				return false;
			default:
				try {
					int num = Integer.parseInt(args[0]);
					int proc = Integer.parseInt(args[1]);
					
					if (proc >= num || proc < -1) {
						System.out.println("Error: Proc needs to be [0, num_of_processes] or -1 for registry");
						return false;
					}
					numProcesses = num;
					procId = proc;
				} catch (NumberFormatException e) {
					System.out.println("Error: parameter is not a number");
					return false;
				}
				break;
		}
		
		return true;
	}
	
	private static void setup() {
		System.setProperty("java.security.policy", "./my.policy");
		
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		// Start a registry
		try {
			// Setup only for "central" process
			if (procId == -1) {
				reg = LocateRegistry.createRegistry(PORT);
				
				try {
					// Create countdownlatch for sync
					reg.bind("SyncLatch", new CountDownLatchProc(numProcesses));
				} catch (AlreadyBoundException | AccessException ex) {
					Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				reg = LocateRegistry.getRegistry(HOST, PORT);
			}
		} catch (RemoteException ex) {
			Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {
		// Check params for proc ud
		if (!validateParams(args)) {
			System.out.println("Use: Exercise1 [num_of_processes] (index_of_process)");
			System.out.println("Example for creating the registry: Exercise1 5 -1");
			System.out.println("Example for creating a process: Exercise1 5 0");
			return;
		}
		
		// Setup
		setup();
		
		// If "central" process, just finish
		if (procId == -1) {
			System.out.println("Central registry RUNNING");
			return;
		}
		
		try {
			// Check if process is already defined
			reg.lookup("Process-" + procId);
			System.out.println("Error: process with that id already binded");
			reg.unbind("Process-" + procId);
			System.out.println("Process successfuly unbinded. Try again");
			return;
		} catch (RemoteException | NotBoundException ex) {
			// All good, process is not yet registered
		}
		
		// Run each proccess
		final Process process;
		process = new Process(HOST, PORT, procId, numProcesses);
		reg.bind("Process-" + procId, process);
		Thread thread = new Thread(() -> {
			try {
				// Wait for the rest of processes
				CountDownLatchProcRMI latch = (CountDownLatchProcRMI) reg.lookup("SyncLatch");
				latch.countDown();
				System.out.println("PROCESS " + process.getId() + " WAITING FOR THE REST");
				latch.await();
				
				// Do something
				System.out.println("PROCESS " + process.getId() + " RUNNING");
				//if (procId == 2) Thread.sleep(2000);
				process.broadcastMessage();
				
				// Reset countdown
				latch.reset();
			} catch (RemoteException | NotBoundException | InterruptedException ex) {
				Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		thread.start();
		
		// Wait for thread
		thread.join();
	}
	
}

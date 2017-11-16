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
	
	public static void main(String[] args) throws InterruptedException {
		System.setProperty("java.security.policy", "./my.policy");
		
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		// Start a registry
		Registry reg;
		try {
			reg = LocateRegistry.createRegistry(PORT);
		} catch (RemoteException ex) {
			Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		
		// Create N processes
		final int n = 2;
		Process[] processList = new Process[n];
		for (int i = 0; i < n; i++) {
			try {
				processList[i] = new Process(HOST, PORT, i, n);
				reg.bind("Process-" + i, processList[i]);
			} catch (RemoteException | AlreadyBoundException ex) {
				Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	
		
		// Run each proccess
		for (int i = 0; i < n; i++) {
			final Process process = processList[i];
			Thread thread = new Thread(() -> {
				try {
					process.run();
					
				} catch (RemoteException ex) {
					Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
				}
			});
			thread.start();
			thread.join();
		}
	}
	
}

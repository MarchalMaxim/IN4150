package exercises.exercise1;

import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Exercise1Test {
	
	// Registry URL
	private static final String HOST = "localhost";
	private static final Integer PORT = 1099;
	
	private Registry reg;
	
	private Process[] processList;
	
	@Before
	public void setUp() throws InterruptedException {
		System.setProperty("java.security.policy", "./my.policy");
		
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		// Start a registry
		try {
			this.reg = LocateRegistry.createRegistry(PORT);
		} catch (RemoteException ex) {
			Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@After
	public void tearDown() {
		// Nothing
	}

	private void createProcesses(int num) {
		// Create N processes
		final int n = num;
		this.processList = new Process[n];
		for (int i = 0; i < n; i++) {
			try {
				processList[i] = new Process(HOST, PORT, i, n);
				this.reg.bind("Process-" + i, processList[i]);
			} catch (RemoteException | AlreadyBoundException ex) {
				Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	@Test
	public void testProcesses() {
		// Init num
		int n = 5;
		
		// Create process
		this.createProcesses(n);
		
		// Run each proccess
		Thread[] threads = new Thread[n];
		for (int i = 0; i < n; i++) {
			final Process process = processList[i];
			final int aux = i;
			threads[i] = new Thread(() -> {
				try {
					switch (aux) {
						case 0:
							Thread.sleep(3000);
							assertTrue(process.getTime().equals(4));
							process.broadcastMessage();
							assertTrue(process.getTime().equals(5));
							break;
						case 1:
							Thread.sleep(2000);
							assertTrue(process.getTime().equals(3));
							process.broadcastMessage();
							assertTrue(process.getTime().equals(4));
							break;
						case 2:
							Thread.sleep(1000);
							assertTrue(process.getTime().equals(1));
							process.broadcastMessage();
							process.broadcastMessage();
							assertTrue(process.getTime().equals(3));
							break;
						default:
							// 3 and 4
							// Send nothing, only ACKs
							break;
					}
				} catch (RemoteException ex) {
					Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InterruptedException ex) {
					Logger.getLogger(Exercise1Test.class.getName()).log(Level.SEVERE, null, ex);
				}
			});
			threads[i].start();
		}
		
		// Wait for all threads
		for (int i = 0; i < n; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException ex) {
				Logger.getLogger(Exercise1Test.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	@Test
	public void testProcessesFile() {
		
		// TODO: make tests with a file for each process
		
	}
	
}

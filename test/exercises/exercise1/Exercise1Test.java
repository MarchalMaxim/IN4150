package exercises.exercise1;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class Exercise1Test {
	
	// Registry URL
	private static final String HOST = "localhost";
	private static final Integer PORT = 1099;
	
	private static Registry reg;
	
	private Process[] processList;
	
	@BeforeClass
	public static void setupClass() throws InterruptedException {
		System.setProperty("java.security.policy", "./my.policy");
		
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		// Start a registry
		try {
			reg = LocateRegistry.createRegistry(PORT);
		} catch (RemoteException ex) {
			Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void registerProcesses(int num) {
		// Create N processes
		final int n = num;
		this.processList = new Process[n];
		for (int i = 0; i < n; i++) {
			try {
				// Unbind any current process
				try {
					reg.unbind("Process-" + i);
				} catch (NotBoundException | AccessException ex) {
					// Do nothing
				}
				// Bind to registry
				processList[i] = new Process(HOST, PORT, i, n);
				reg.bind("Process-" + i, processList[i]);
			} catch (RemoteException | AlreadyBoundException ex) {
				Logger.getLogger(Exercise1.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	/*@Test
	public void testProcesses() {
		// Init num processes
		int n = 5;
		
		// Register process
		this.registerProcesses(n);
		
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
							//assertTrue(process.getTime().equals(4));
							process.broadcastMessage();
							//assertTrue(process.getTime().equals(5));
							break;
						case 1:
							Thread.sleep(2000);
							//assertTrue(process.getTime().equals(3));
							process.broadcastMessage();
							//assertTrue(process.getTime().equals(4));
							break;
						case 2:
							Thread.sleep(1000);
							//assertTrue(process.getTime().equals(1));
							process.broadcastMessage();
							process.broadcastMessage();
							//assertTrue(process.getTime().equals(3));
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
	}*/
	
	@Test
	public void testProcessesFile() throws FileNotFoundException, IOException {
		// Init num processes
		int n = 2;
		
		// Register process
		this.registerProcesses(n);
		
		// Redirect output to baos
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		
		// Run each proccess
		Thread[] threads = new Thread[n];
		for (int i = 0; i < n; i++) {
			final Process process = processList[i];
			final int aux = i;
			threads[i] = new Thread(() -> {
				try {				
					switch (aux) {
						case 0:
							process.broadcastMessage();
							break;
						case 1:
							Thread.sleep(2000);
							process.broadcastMessage();
							break;
					}
				} catch (IOException | InterruptedException ex) {
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
		
		// Put things back
		System.out.flush();
		System.setOut(old);
		
		// Check output with file
		for (int i = 0; i < n; i++) {
			BufferedReader br = new BufferedReader(new FileReader("test/exercises/exercise1/Exercise1Proc_" + i + ".txt"));
			String line;
			List<String> messages = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				messages.add(line);
			}
			br.close();
			
			int indexMessage = 0;
			String lines[] = baos.toString().split("\\r?\\n");
			for (String lineMessage : lines) {
				if (lineMessage.contains("[" + i + "]")) {
					assertTrue(messages.get(indexMessage).equals(lineMessage));
					indexMessage++;
				}
			}
		}
	}
	
}

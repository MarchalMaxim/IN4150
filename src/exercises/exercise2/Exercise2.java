package exercises.exercise2;
import exercises.exercise1.CountDownLatchProc;
import exercises.exercise1.CountDownLatchProcRMI;
import java.rmi.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import exercises.exercise1.MessageRMI;
import exercises.exercise1.MessageType;

import java.util.LinkedList;

public class Exercise2 {

	// Registry URL
	public static final String HOST = "localhost";
	public static final Integer PORT = 1099;

	public static int numComponents;
	public static int componentId;

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
					int component = Integer.parseInt(args[1]);

					if (component >= num || component < -1) {
						System.out.println("Error: Proc needs to be [0, num_of_processes] or -1 for registry");
						return false;
					}
					numComponents = num;
					componentId = component;
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
			if (componentId == -1) {
				reg = LocateRegistry.createRegistry(PORT);

				try {
					// Create countdownlatch for sync
					reg.bind("SyncLatch", new CountDownLatchProc(numComponents));
				} catch (AlreadyBoundException | AccessException ex) {
					Logger.getLogger(Exercise2.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				reg = LocateRegistry.getRegistry(HOST, PORT);
			}
		} catch (RemoteException ex) {
			Logger.getLogger(Exercise2.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String args[]) throws RemoteException, AlreadyBoundException, InterruptedException {
		// Check params for proc ud
		if (!validateParams(args)) {
			System.out.println("Use: Exercise2 [num_of_processes] (index_of_process)");
			System.out.println("Example for creating the registry: Exercise2 5 -1");
			System.out.println("Example for creating a process: Exercise2 5 0");
			return;
		}

		// Setup
		setup();

		// If "central" process, just finish
		if (componentId == -1) {
			System.out.println("Central registry RUNNING");
			return;
		}

		try {
			// Check if process is already defined
			reg.lookup("Component-" + componentId);
			System.out.println("Error: process with that id already binded");
			reg.unbind("Component-" + componentId);
			System.out.println("Component successfuly unbinded. Try again");
			return;
		} catch (RemoteException | NotBoundException ex) {
			// All good, process is not yet registered
		}

		// Run each proccess
		final Component component;
		component = new Component(HOST, PORT, componentId, numComponents);
		reg.bind("Component-" + componentId, component);
		Thread thread = new Thread(() -> {
			try {
				// Wait for the rest of processes
				CountDownLatchProcRMI latch = (CountDownLatchProcRMI) reg.lookup("SyncLatch");
				latch.countDown();
				System.out.println("COMPONENT " + component.getId() + " WAITING FOR THE REST");
				latch.await();

				// Do something
				System.out.println("COMPONENT " + component.getId() + " RUNNING");

				if (componentId == 0) {
					// Proc 0 records message
					long waitTime = (long) new Random().nextDouble()*50;
					try {
						Thread.sleep(waitTime);
						component.recordGlobalState();
					} catch (InterruptedException | NotBoundException | RemoteException e) {
						// Do nothing
					}
				} else {
					// +/- 50 ms delay
					component.broadcast(new Message(componentId, MessageType.MESSAGE));
					long waitTime = (long) new Random().nextDouble() * 50;
					Thread.sleep(waitTime);
					// Broadcast a message
					component.broadcast(new Message(componentId, MessageType.MESSAGE));
				}

				// Reset countdown
				latch.reset();
			} catch (RemoteException | NotBoundException | InterruptedException ex) {
				Logger.getLogger(Exercise2.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		
		thread.start();
	
		// Wait for the thread to finish before pulling results
		thread.join();
		
		// Extract the channel record from the components
		// The global state is extracted on the "server side"
		Map<String, LinkedList<MessageRMI>> globalChannel = new HashMap<>();

		// Extract the channel information from component c
		while (true) {
			try {
				LinkedList<MessageRMI>[] tmpList = component.pullResults();
				String tmpKey;
				LinkedList<MessageRMI> tmpValue;
				for (int i = 0; i < tmpList.length; i++) {
					tmpKey = componentId + "-" + i;
					tmpValue = tmpList[i];
					globalChannel.put(tmpKey, tmpValue);
				}
				printState(globalChannel);
				break;
			} catch(Exception e) {
				// Component not done recording yet. Wait
			}
		}
	}
		
	public static void printState(Map<String, LinkedList<MessageRMI>> state) {
		/** Prints the contents of a recording.
		 * The recording is stored in a Map that maps a channel name to a linkedList of messages
		 */
		for (Map.Entry<String, LinkedList<MessageRMI>> entry: state.entrySet()) {
			Iterator<MessageRMI> it = entry.getValue().iterator();
			if (!entry.getValue().isEmpty()) {
				System.out.println("Printing contents of channel "+entry.getKey());
			} else {
				System.out.println("Channel "+entry.getKey()+" is empty.");
			}
			while (it.hasNext()) {
				try {
					MessageRMI mess =  it.next();
					System.out.println("\tType: "+mess.getType()+"\n"+"\tOrigin: "+mess.getOrigin());
				} catch (RemoteException e) {
					System.out.println(e);
					break;
				}
			}
		}
	}
}


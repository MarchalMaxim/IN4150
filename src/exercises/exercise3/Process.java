package exercises.exercise3;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that implements ProcessRMI.
 */
public abstract class Process extends UnicastRemoteObject implements ProcessRMI {
	
	protected final Integer id;
	
	protected final Integer totalProcesses;
	
	protected final Registry registry;
		
	// Check if we are killed
	protected volatile Boolean isKilled;
	
	// Level of the process - number of nodes it has captured
	protected Integer level;
	
	// Owner of the process
	protected Integer ownerId;
	
	// Father of the process
	protected Integer father;
	
	// Potential father of the process
	protected Integer potentialFather;
	
	// Untraversed links
	protected List<Integer> untraversed;
	
	// Queue for messages
	protected BlockingQueue<MessageRMI> queue;
	
	/**
	 * Constructor.
	 * @param host - IP address
	 * @param port - port of the process
	 * @param id - id of the process
	 * @param totalProcesses - total number of processes
	 * @throws RemoteException 
	 */
	public Process(String host, Integer port, Integer id, Integer totalProcesses) throws RemoteException {
		this.id = id;
		this.totalProcesses = totalProcesses;
		this.registry = LocateRegistry.getRegistry(host, port);
		this.level = 0;
		this.ownerId = 0;
		this.untraversed = new ArrayList<>();
		for (int i = 0; i < this.totalProcesses; i++) {
			this.untraversed.add(i);
		}
		this.father = null;
		this.potentialFather = null;
		this.isKilled = false;
		this.queue = new ArrayBlockingQueue<>(10);
	}
	
	/**
	 * Get the id of the process.
	 * @return id of the process
	 */
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Method to compare (level, id).
	 * @param message - message
	 * @return -1 if the message is bigger, 1 if our node is bigger, 0 if thy are equal
	 * @throws RemoteException 
	 */
	protected int compareContent(MessageRMI message) throws RemoteException {
		if (message.getContentLevel() < this.level) {
			return 1;
		} else if (message.getContentLevel() > this.level) {
			return -1;
		} else {
			// Levels are equal
			if (message.getContentId() < this.ownerId) return 1;
			else if (message.getContentId() > this.ownerId) return -1;
			else return 0;
		}
	}
	
	/**
	 * Send a message to another process.
	 * @param message - object of the message
	 * @param procId - id of the process
	 * @return thread from sending a message
	 * @throws RemoteException 
	 */
	protected Thread sendMessage(MessageRMI message, Integer procId) throws RemoteException {
		// Send message
		//Thread messageThread = new Thread(() -> {
			try {
				// Random delay
				int ms = new Random().nextInt((2000 - 500) + 1) + 500;
				Thread.sleep(ms);

				// Debug
				System.out.println("[" + this.id + "] Sending " + message.getType() + " to [" + procId + "]");
				
				// Send message
				ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + procId);
				process.onReceived(message);
			} catch (NotBoundException | RemoteException | InterruptedException ex) {
				Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
			}
		//});
		//messageThread.start();
		return null;
	}
	
	/**
	 * Start a process.
	 * @throws RemoteException - error
	 * @throws InterruptedException - error
	 */
	public abstract void start() throws RemoteException, InterruptedException;
	
	/**
	 * Event triggers when a message is received.
	 * @param message 
	 * @throws java.rmi.RemoteException 
	 * @throws java.lang.InterruptedException 
	 */
	@Override
	public abstract void onReceived(MessageRMI message) throws RemoteException, InterruptedException;
	
}

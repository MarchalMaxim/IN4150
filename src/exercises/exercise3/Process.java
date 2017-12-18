package exercises.exercise3;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that implements ProcessRMI.
 */
public class Process extends UnicastRemoteObject implements ProcessRMI {
	
	public final Integer id;
	
	public final Integer totalProcesses;
	
	private final Registry registry;
		
	// Check if we are killed
	private Boolean isKilled;
	
	// Level of the process - number of nodes it has captured
	private Integer level;
	
	// Owner of the process
	private Integer ownerId;
	
	// Father of the process
	private Integer father;
	
	// Potential father of the process
	private Integer potentialFather;
	
	// Untraversed links
	private List<Integer> untraversed;
	
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
	}
	
	/**
	 * Get the id of the process.
	 * @return id of the process
	 */
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Method to compare (level, id)
	 * @param message
	 * @return -1 if the message is bigger, 1 if our node is bigger, 0 if thy are equal
	 * @throws RemoteException 
	 */
	private int compareContent(MessageRMI message) throws RemoteException {
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
	
	@Override
	public void onReceived(MessageRMI message) throws RemoteException {
		// Debug
		System.out.println("[" + this.id + "] Received a " + message.getType() + " message");
		
		// Process message
		switch (message.getType()) {
			case CAPTURE:
				// Received a capture attempt from a node
				
				// Check if they are bigger
				switch (this.compareContent(message)) {
					case 1:
						// (level’,id’) < (level,owner_id)
						
						// Ignore message
						assert true;
						break;
					case -1:
						// (level’,id’) > (level,owner_id
						
						// Set potential father
						this.potentialFather = message.getSenderId();
						// Adopt higher value
						this.level = message.getContentLevel();
						this.ownerId = message.getSenderId();
						// Mark as captured
						if (this.father == null) {
							this.father = this.potentialFather;
						}

						// TODO: KILL or ACK
						break;
					default:
						// (level’,id’) = (level,owner_id

						// Set new father
						this.father = this.potentialFather;
						// Set ACK to new father
						try {
							MessageRMI ackMessage = new Message(this.id, MessageType.ACK, this.level, this.ownerId);
							ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + this.father);
							process.onReceived(ackMessage);
						} catch (NotBoundException ex) {
							Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
						}
						break;
				}
				break;
			case ACK:
				// Received an ACK message
				// So we are bigger than that process
				
				// Add it to the level
				this.level++;
				
				break;
			case KILL:
				// Received a KILL message
				// So we are no longer a candidate
				
				// TODO: do something
				
			default:
				System.out.println("Unknown message received");
		}
	}
	
	/**
	 * Send a message to another process
	 * @param message - object of the message
	 * @param procId - id of the process
	 * @return thread from sending a message
	 * @throws RemoteException 
	 */
	private Thread sendMessage(MessageRMI message, Integer procId) throws RemoteException {
		// Send message
		Thread messageThread = new Thread(() -> {
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
		});
		messageThread.start();
		return messageThread;
	}
	
	/**
	 * TODO: Start a candidate process.
	 * @throws RemoteException - something went wrong
	 */
	public void startAsCandidate() throws RemoteException {
		// Go through all untraverse links
		while (!this.untraversed.isEmpty()) {
				
			// Get next random link
			int nextIndex = (int) (Math.random() * (this.untraversed.size() - 1));
			Integer link = this.untraversed.get(nextIndex);
			
			// Attemp to capture
			MessageRMI captureMessage = new Message(this.id, MessageType.CAPTURE, this.level, this.id);
			this.sendMessage(captureMessage, link);
			
			// TODO: Wait to receive a response
			while (true) {
				Integer newLevel = 0;
				Integer newId = 0;

				if (newId.equals(this.id) && !this.isKilled) {
					this.level++;
					this.untraversed.remove(this.untraversed.indexOf(link));

					// Don't receive more message from this link
					break;
				} else if (false) {
					// Discard message
				} else {
					// Send ACK back
					this.isKilled = true;
				}
			}
		}
		
		// TODO: Wait until a message has arrived
		
		/*CountDownLatch counter = this.ackList.get(message.hashCode());
		try {
			counter.await();
		} catch (InterruptedException ex) {
			Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
		}*/
		
		// If we are not killed, we are elected
		if (!this.isKilled) {
			System.out.println("[" + this.id + "] IS ELECTED!!");
		}
		
	}

	/**
	 * Start an ordinary process..
	 */
	public void startAsOrdinary() {
		// Do nothing?
	}
	
}

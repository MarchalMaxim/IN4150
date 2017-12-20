package exercises.exercise3;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for ordinary processes.
 */
public class OrdinaryProcess extends Process {
	
	/**
	 * Constructor.
	 * @param host - host
	 * @param port - port
	 * @param id - id
	 * @param totalProcesses - number
	 * @throws RemoteException - something went wrong
	 */
	public OrdinaryProcess(String host, Integer port, Integer id, Integer totalProcesses) throws RemoteException {
		super(host, port, id, totalProcesses);
	}
	
	/**
	 * Start an ordinary process..
	 */
	@Override
	public void start() {
		// Do nothing?
	}
	
	@Override
	public synchronized void onReceived(MessageRMI message) throws RemoteException, InterruptedException {
		// Debug
		System.out.println("[" + this.id + "] Received a " + message.getType() + " message");
		
		// Process message
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
				MessageRMI newMessage;
				if (this.father == null) {
					this.father = this.potentialFather;
					// Send ACK to new father
					newMessage = new Message(this.id, MessageType.ACK, this.level, this.ownerId);
				} else {
					// Send KILL to current father
					newMessage = new Message(this.id, MessageType.KILL, this.level, this.ownerId);
				}
				
				try {
					ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + this.father);
					process.onReceived(newMessage);
				} catch (NotBoundException ex) {
					Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
				}
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
	}
	
}
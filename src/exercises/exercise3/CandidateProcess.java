package exercises.exercise3;

import java.rmi.RemoteException;

/**
 * Class for candidate process.
 */
public class CandidateProcess extends Process {

	/**
	 * Constructor.
	 * @param host - host
	 * @param port - port
	 * @param id - id
	 * @param totalProcesses - number
	 * @throws RemoteException - something went wrong
	 */
	public CandidateProcess(String host, Integer port, Integer id, Integer totalProcesses) throws RemoteException {
		super(host, port, id, totalProcesses);
	}

	/**
	 * Start a candidate process.
	 * @throws RemoteException - something went wrong
	 * @throws InterruptedException - something went wrong
	 */
	@Override
	public void start() throws RemoteException, InterruptedException {
		// Go through all untraverse links
		while (!this.untraversed.isEmpty()) {
			// Hack
			if (this.isKilled) break;
			
			// Get next random link
			int nextIndex = (int) (Math.random() * (this.untraversed.size() - 1));
			Integer link = this.untraversed.get(nextIndex);
			
			// Attemp to capture
			MessageRMI captureMessage = new Message(this.id, MessageType.CAPTURE, this.level, this.id);
			this.sendMessage(captureMessage, link);
		}
		
		// If we are not killed, we are elected
		if (!this.isKilled) {
			System.out.println("[" + this.id + "] IS ELECTED!!");
			
			// Show report
			System.out.println("-------------------------------");
			System.out.println("Num. captures: " + Exercise3Report.captureMessages);
			System.out.println("Num. kills: " + Exercise3Report.killedMessages);
			System.out.println("Num. acks: " + Exercise3Report.ackMessages);
			System.out.println("Max level: " + Exercise3Report.maxLevel);
			//System.out.println("Num. captures: ");
		}
		
	}
	
	@Override
	public void onReceived(MessageRMI message) throws RemoteException, InterruptedException {
		// Debug
		System.out.println("[" + this.id + "] Received a " + message.getType() + " message");
		
		// Add to queue
		//this.queue.add(message);
		
		// Get content
		Integer link = message.getSenderId();
		Integer newLevel = message.getContentLevel();
		Integer newId = message.getContentId();

		if (newId.equals(this.id) && !this.isKilled) {
			// Recieved an ACK message
			this.level++;
			this.untraversed.remove(link);
			
			// Report level
			Exercise3Report.maxLevel = Math.max(Exercise3Report.maxLevel, this.level);

			System.out.println("[" + this.id + "] CAPTURED process [" + message.getSenderId() + "]");
			
			// Don't receive more message from this link
		} else if (
				newLevel < this.level
				|| (newLevel.equals(this.level) && newId <= this.id)
			) {
			// Discard message
		} else {
			// Send ACK back
			MessageRMI ackMessage = new Message(this.id, MessageType.ACK, newLevel, newId);
			this.sendMessage(ackMessage, link);

			System.out.println("[" + this.id + "] was KILLED");
			
			// We just got killed
			this.isKilled = true;
		}
	}
	
}

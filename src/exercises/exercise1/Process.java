package exercises.exercise1;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process extends UnicastRemoteObject implements ProcessRMI {

	public final Integer id;
	
	public final Integer totalProcesses;
	
	private final Registry registry;
	
	// Scalar clock
	private Integer time;
	
	// Queue of messages received but not delivered
	private final PriorityQueue<MessageRMI> messageQueue;
	
	// Counter for the number of ACKS received for each message
	private final Map<Integer, CountDownLatch> ackList;
	
	public Process(String host, Integer port, Integer id, Integer totalProcesses) throws RemoteException {
		this.id = id;
		this.totalProcesses = totalProcesses;
		this.registry = LocateRegistry.getRegistry(host, port);
		this.time = 1;
		this.messageQueue = new PriorityQueue<>(100, (MessageRMI messageA, MessageRMI messageB) -> {
			try {
				if (messageA.getTime() < messageB.getTime()) {
					return -1;
				} else {
					return 1;
				}
			} catch (RemoteException ex) {
				Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
			}
			return 1;			
		});
		this.ackList = new HashMap<>();
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public Integer getTime() {
		return this.time;
	}
	
	@Override
	public void onReceived(MessageRMI message) throws RemoteException {
		switch (message.getType()) {
			case MESSAGE:
				// Do something with MESSAGE
				this.time++;
				this.time = Math.max(this.time, message.getTime());
				
				System.out.println("[" + this.id + "] Broadcast ACK");
				
				// Send ACK to all proccesses
				for (int i = 0; i < this.totalProcesses; i++) {
					try {
						MessageRMI ackMessage = new Message(this.id, MessageType.ACK, this.time, message.getContent(), message);
						ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + i);
						process.onReceived(ackMessage);
					} catch (NotBoundException ex) {
						Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				break;
			case ACK:
				// Ignore ACKs for other processes
				if (!message.getOriginalMessage().getOrigin().equals(this.id)) break;
				
				// Do something with ACK
				this.time = Math.max(this.time, message.getTime());
				
				// Add ACK
				CountDownLatch counter = this.ackList.get(message.getOriginalMessage().hashCode());
				counter.countDown();
				
				break;
			default:
				System.out.println("Unknown message received");
		}
	}
	
	private void broadcast(MessageRMI message) {
		// Add mesage to the queue
		this.messageQueue.add(message);
		
		// Initialize count down latch
		this.ackList.put(message.hashCode(), new CountDownLatch(this.totalProcesses - 1));
		
		System.out.println("[" + this.id + "] Broadcast message");
		
		// Send message to all processes (except self)
		for (int i = 0; i < this.totalProcesses; i++) {
			// Dont send message to self
			if (i == this.id) continue;
			
			final int aux = i;
			new Thread(() -> {
				try {
					ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + aux);
					process.onReceived(message);
				} catch (NotBoundException | RemoteException ex) {
					Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
				}
			}).start();
		}
		
		// Wait for all ACKs
		CountDownLatch counter = this.ackList.get(message.hashCode());
		try {
			counter.await();
		} catch (InterruptedException ex) {
			Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		// If we get here: all processes have sent an ACK
		
		// Remove mesage from the queue
		this.messageQueue.poll();
		
		// Clear acknowledgements
		this.ackList.remove(message.hashCode());
		
		System.out.println("[" + this.id + "] Message delivered");
	}
	
	public void broadcastMessage() throws RemoteException {
		MessageRMI message = new Message(this.id, MessageType.MESSAGE, this.time);
		this.broadcast(message);
	}
	
}

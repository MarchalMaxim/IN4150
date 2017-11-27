package exercises.exercise1;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process extends UnicastRemoteObject implements ProcessRMI {

	public final Integer id;
	
	public final Integer totalProcesses;
	
	private final Registry registry;
	
	public Integer time;
	
	public Queue<MessageRMI> messageQueue;
	
	public LinkedList<MessageRMI> ackList;
	
	public Process(String host, Integer port, Integer id, Integer totalProcesses) throws RemoteException {
		this.id = id;
		this.totalProcesses = totalProcesses;
		this.registry = LocateRegistry.getRegistry(host, port);
		this.time = 1;
		this.messageQueue = new LinkedList<>();
		this.ackList = new LinkedList<>();
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
						MessageRMI ackMessage = new Message(this.id, MessageType.ACK, this.time);
						ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + i);
						process.onReceived(ackMessage);
					} catch (NotBoundException ex) {
						Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				break;
			case ACK:
				if (message.getOrigin().equals(this.id)) break;
				
				// Do something with ACK
				this.time = Math.max(this.time, message.getTime());
				
				// Add ACK to list
				this.ackList.add(message);
				
				// All processes have sent an ACK
				//System.out.println(this.ackList.size());
				//System.out.println(this.totalProcesses);
				if (this.ackList.size() == (this.totalProcesses - 1)) {
					this.ackList.clear();
					this.messageQueue.remove();
					System.out.println("Done!");
				}
				
				break;
			default:
				System.out.println("Unknown message received");
		}
	}
	
	private void broadcast(MessageRMI message) {
		System.out.println("[" + this.id + "] Broadcast message");
		
		// Add mesage to queue
		this.messageQueue.add(message);
		
		// Send message to all processes
		for (int i = 0; i < this.totalProcesses; i++) {
			if (i == this.id) continue;
			
			try {
				ProcessRMI process = (ProcessRMI) this.registry.lookup("Process-" + i);
				process.onReceived(message);
			} catch (NotBoundException | RemoteException ex) {
				Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public void run() throws RemoteException {
		System.out.println("PROCESS " + this.id + " RUNNING");
		
		// Send a message
		MessageRMI message = new Message(this.id, MessageType.MESSAGE, this.time);
		this.broadcast(message);
		
	}
	
}

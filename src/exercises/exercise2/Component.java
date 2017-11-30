package exercises.exercise2;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exercises.exercise1.*;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
public class Component extends UnicastRemoteObject implements ComponentRMI {
	
	// Save the state of the a channel in a list. There's a list for every channel	
	private LinkedList<MessageRMI>[] channels;
	private Integer numMarkers;
	
	// Every component is aware of how many components there are in the network
	private Integer numComponents;
	
	// We define the state of the component as all the messages it has received
	private Queue<MessageRMI> state = new LinkedList<MessageRMI>();
	
	// Every component has a unique id
	private int id;
	
	// The boolean participated is meant to signify whether the component has participated in a global state recording
	public boolean participated;
	
	public int messageCount;
	
	// The registry of RMI objects that the components can access
	private Registry registry;
		
	public Component(String host, int port, int id, int numComponents)throws RemoteException {
		super(port);
		channels = new LinkedList[numComponents];
		this.id = id;
		for(int i = 0; i<numComponents; i++) {
			channels[i] = new LinkedList<MessageRMI>();
		}
		this.participated = false;
		this.registry = LocateRegistry.getRegistry(host, port);
		if(this.registry == null) {
			throw new RemoteException();
		}
		this.numComponents = numComponents;
		this.numMarkers = 0;
		messageCount = 0;
		
	}
		
	@Override 
	public void printId()throws RemoteException {
		System.out.println("printId() : "+this.id);
	}
	
	public void broadcast(MessageRMI message)throws NotBoundException, RemoteException {
		// Broadcast a message to all components
		String[] componentNames = registry.list();
		System.out.println("Component "+id+" broadcasting marker");
		for(int i = 0; i<numComponents; i++) {
			if(i==id) {
				continue;
			}
			ComponentRMI comp = (ComponentRMI) this.registry.lookup(componentNames[i]);
			comp.onReceive(message);
		}
	}
	
	public void recordGlobalState() throws RemoteException, NotBoundException{
		// Method that initializes a global state recording
		this.participated = true;
		MessageRMI marker;
		this.recordLocalState();
		// Broadcast a marker
		try {
			marker = new Message(this.id, MessageType.MARKER);
		}catch(RemoteException e) {
			System.out.println(e);
			this.participated = false;
			return;
		}
		broadcast(marker);		
	}
	
	public void recordLocalState() {
		// Copies the state of the component into the recorded variable
		// Sets the participated variable equal to true (as is shown in the lecture notes of
		// the Chandy-Lamport algorithm
		for(MessageRMI stateVar: state) {
			channels[id].add(stateVar);
		}
		participated = true;
	}
	
	public LinkedList<MessageRMI>[] presentRecord() throws Exception{
		boolean ready = (numMarkers == (numComponents-1));
		if(false==ready) {
			throw new Exception("Error, not ready because not all markers have been returned");
		}else {
			LinkedList<MessageRMI>[] toReturn = channels;
			return toReturn;
		}
	}
	
	public void sendNext() {
		// Send a message to another (semi random) process
		int to = new Integer(id).hashCode() % numComponents;
		Message message;
		try{
			message = new Message(this.id, MessageType.MESSAGE);
			message.setContent(messageCount++);
			ComponentRMI comp = (ComponentRMI) registry.lookup("Component-"+to);
			comp.onReceive(message);
		}catch(Exception e) {
			System.out.println("Could not deliver message");
		}		
	}
	
	@Override
	public void debugPrint() {
		System.out.println("Remote method call successful");
	}
	
	@Override
	public void onReceive(MessageRMI message) throws RemoteException{
		// Create a marker
		MessageType type;
		try{
			type = message.getType();
		}catch(Exception e) {
			System.out.println("Could not extract type");
			throw e;
		}
		state.add(message);
		
		// React to a received message
		if(type==null) {
			System.out.println("type is null!");
		}
		switch(type) {
		case ACK:
			System.out.println("Received message of type ACK");
			break;
		case MESSAGE:
			try {
				int markerOrigin = message.getOrigin();
				this.channels[markerOrigin].add(message); // Adds message to the buffer
			}catch(Exception e) {
				throw e;
			}
			break;
		case MARKER:
			numMarkers++;
			System.out.println("Component "+id+" received a marker");
			if(true == participated) {
				try{
					int markerOrigin = message.getOrigin();
					channels[markerOrigin].add(message);
				}catch(Exception e) {
					System.out.println(e.getMessage()); 
				}				
			}else {
				try{
					int markerOrigin = message.getOrigin();
					this.channels[markerOrigin] = new LinkedList<MessageRMI>();
					participated = true;
					MessageRMI marker = new Message(this.id, MessageType.MARKER);
					broadcast(marker);
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
			}
			break;
		}
	}
}

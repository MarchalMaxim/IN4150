package exercises.exercise2;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exercises.exercise1.*;
import java.util.Queue;
import java.util.LinkedList;

public class Component extends UnicastRemoteObject implements ComponentRMI {
	
	// Save the state of the a channel in a list. There's a list for every channel	
	private LinkedList<MessageRMI>[] channels;
	private Integer numMarkers;
	
	// Every component is aware of how many components there are in the network
	private Integer numComponents;
	
	// We define the state of the component as all the messages it has received
	private Queue<MessageRMI> state = new LinkedList<>();
	
	// Every component has a unique id
	private final int id;
	
	// The boolean participated is meant to signify whether the component has participated in a global state recording
	public boolean participated;
	
	public int messageCount;
	
	private boolean recordingDone;
	
	// The registry of RMI objects that the components can access
	private final Registry registry;
		
	public Component(String host, int port, int id, int numComponents) throws RemoteException {
		//super(port);
		channels = new LinkedList[numComponents];
		this.id = id;
		for(int i = 0; i<numComponents; i++) {
			channels[i] = new LinkedList<>();
		}
		this.participated = false;
		this.registry = LocateRegistry.getRegistry(host, port);
		if(this.registry == null) {
			throw new RemoteException();
		}
		this.numComponents = numComponents;
		this.numMarkers = 0;
		messageCount = 0;
		recordingDone = false;
		
	}
	
	public int getId() {
		return id;
	}
			
	@Override 
	public void printId()throws RemoteException {
		System.out.println("printId() : "+this.id);
	}
	
	public void broadcast(MessageRMI message)throws NotBoundException, RemoteException {
		// Broadcast a message to all components
		//String[] componentNames = registry.list();
		System.out.println("C" + id + " broadcasting "+message.getType());
		for(int i = 0; i < numComponents; i++) {
			if(i==id) {
				continue;
			}
			ComponentRMI comp = (ComponentRMI) this.registry.lookup("Component-" + i);
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
	
	public LinkedList<MessageRMI>[] pullResults() throws Exception{
		if (recordingDone == false) {
			throw new Exception("Please wait, component "+id+" not ready because only "+numMarkers+" markers have been returned");
		} else {
			numMarkers = 0;
			recordingDone = false;
			LinkedList<MessageRMI>[] toReturn = channels;
			channels = new LinkedList[numComponents];
			for (int i = 0; i < numComponents; i++) {
				// Reset the channels
				channels[i] = new LinkedList<>();
			}
			return toReturn;
		}
	}
	
	public void sendTo(int to) {
		// Send a message to component to
		Message message;
		try{
			message = new Message(this.id, MessageType.MESSAGE);
			message.setContent(messageCount++);
			ComponentRMI comp = (ComponentRMI) registry.lookup("Component-"+to);
			comp.onReceive(message);
			System.out.println("Component "+id+" sent message to component "+to);
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
		if(type!=MessageType.MARKER) {
			state.add(message);
		}
		System.out.println("C"+id+" received a "+type+" from C"+message.getOrigin());
		// React to a received message
		switch(type) {
		case ACK:
			break;
		case MESSAGE:
			try {
				int origin = message.getOrigin();
				if(true==participated) {
					// If the component is participating in a global state recording, add incoming message to the buffer
					this.channels[origin].add(message); 
				}
			}catch(Exception e) {
				throw e;
			}
			break;
		case MARKER:
			numMarkers++;
			if(false==participated){
				// Received a marker and not yet participated:
				// record your local state and broadcast marker
				try{
					int markerOrigin = message.getOrigin();
					this.channels[markerOrigin] = new LinkedList<MessageRMI>();
					recordLocalState();
					participated = true;
					MessageRMI marker = new Message(this.id, MessageType.MARKER);
					broadcast(marker);
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
			}
			if(numMarkers == numComponents-1) {
				System.out.println("C"+id+" received all markers, terminating.");
				recordingDone = true;
				numMarkers = 0;
				participated = false;
			}
			break;
		} 
	}
}

package exercises.exercise2;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exercises.exercise1.*;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
public class Component implements ComponentRMI {
	
	// Save the state of the a channel in a list. There's a list for every channel	
	private LinkedList<MessageRMI>[] channels;
	
	// We define the state of the component as all the messages it has received
	// TODO: maybe define something else as the state of a component
	private Queue<MessageRMI> state;
	
	// Every component has a unique id
	private Integer id;
	
	// The boolean participated is meant to signify whether the component has participated in a global state recording
	public boolean participated;
	
	// The registry of RMI objects that the component knows of
	private Registry registry;
	
	private LinkedList<MessageRMI>[] recorded;
	
	public Component(String host, Integer port, Integer id, Integer numComponents)throws RemoteException {
		for(int i = 0; i<numComponents; i++) {
			channels[i] = new LinkedList<MessageRMI>();
		}
		this.participated = false;
		this.state = new LinkedList<MessageRMI>();
		this.registry = LocateRegistry.getRegistry(host, port);
		
	}
	
	public void record_local_state() {
		// Copies the state of the component into the recorded variable
		// Sets the participated variable equal to true (as is shown in the lecture notes of
		// the Chandy-Lamport algorithm)
		for(MessageRMI mes: state) {
			recorded[id].add(mes);
		}
		participated = true;
	}
	
	public void sendMarkers() {
		// Sends markers along all channels by calling onReceive remotely
		int i = 0;
		while(true) {
			if(i==this.id) {
				continue;
			}
			try {
				MessageRMI marker = new Message(this.id, MessageType.MARKER);
				ComponentRMI comp = (ComponentRMI) this.registry.lookup("Component-"+i);
				comp.onReceive(marker);
			}catch(Exception e) {
				System.out.println("Could not send marker from component "+id+" to component "+i);
			}
			i++;
		}
	}
	
	
	public void onReceive(MessageRMI message) {
		MessageType type;
		try{
			type = message.getType();
		}catch(Exception e) {
			System.out.println(e);
			type = MessageType.MESSAGE;
		}
		switch(type) {
		case ACK:
			state.add(message);
			System.out.println("Received message of type ACK");
			break;
		case MESSAGE:
			state.add(message);
			try {
				Integer markerOrigin = message.getOrigin();
				channels[markerOrigin].add(message); // Adds message to the buffer
			}catch(Exception e) {
				System.out.println(e);
			}
			break;
		case MARKER:
			state.add(message);
			if(true == participated) {
				try{
					Integer markerOrigin = message.getOrigin();
					recorded[markerOrigin] = channels[markerOrigin];
					channels[markerOrigin] = new LinkedList<MessageRMI>();
				}catch(Exception e) {
					System.out.println(e);
				}
				
			}
			break;
		}
	}
	
	public void recordLocalState() {
		
	}
	
	public void sendMarker() {
		
	}
	
	
}

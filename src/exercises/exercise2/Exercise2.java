package exercises.exercise2;
import java.rmi.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import exercises.exercise1.MessageRMI;
import exercises.exercise1.MessageType;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
public class Exercise2 {
		// Registry URL
		public static final String HOST = "localhost";
		public static final Integer PORT = 1099;
		
		public static void main(String args[]) {
			// Create and install a security manager
			System.setProperty("java.security.policy", "./my.policy");
			if(args.length==0) {
				args = new String[]{"3"};
			}
			int numComponents = Integer.parseInt(args[0]);
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}
			
			// Make a registry so components can communicate
			Registry reg;
			try {
				reg = LocateRegistry.createRegistry(PORT);
			}catch(Exception e) {
				// If we can not make a registry, something is wrong and we should exit
				Logger.getLogger(Exercise2.class.getName()).log(Level.SEVERE, null, e);
				return;
			}
			
			// Make some components
			// and declare the components in the registry
			Component[] ComponentList = new Component[numComponents];
			for(int i = 0; i<numComponents; i++) {
				try {
					ComponentList[i] = new Component(HOST, PORT, i, numComponents);
					reg.bind("Component-"+i, ComponentList[i]);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		// Test the algorithm by running a pre-fab test case
		Thread[] threads = new Thread[numComponents];				
		threads[0] = new Thread (  
				()-> {
					try{
						ComponentList[0].recordGlobalState();
						long waitTime = (long) new Random().nextDouble()*50;
						Thread.sleep(waitTime);
						ComponentList[0].broadcast(new Message(0,MessageType.MESSAGE));
					}catch(Exception e) {
						System.out.println(e);
					}
				}
			);
		threads[1] = new Thread( 
					()->{
				try{
					ComponentList[1].broadcast(new Message(1,MessageType.MESSAGE));
					long waitTime = (long) new Random().nextDouble()*50;
					Thread.sleep(waitTime);
					ComponentList[1].sendTo(2);
				}catch(Exception e) {
					// 
				}
					}
				);
		threads[2] = new Thread(
				()-> {
					try{
						ComponentList[2].broadcast(new Message(2,MessageType.MESSAGE));
						long waitTime = (long) new Random().nextDouble()*50;
						Thread.sleep(waitTime);
						ComponentList[2].sendTo(1);
					}catch(Exception e) {
						//
					}
				}
			);
		
		
		for(int i = 0; i<numComponents; i++) {
			threads[i].start();
		}
		// Extract the channel record from the components
		// The global state is extracted on the "server side"
		Map<String, LinkedList<MessageRMI>> globalChannels = new HashMap<String, LinkedList<MessageRMI>>();
		boolean userNotified = false;
		for(int j = 0; j<numComponents; j++) {
			while(globalChannels.size()==0) {
				try{
					LinkedList<MessageRMI>[] tmpList = ComponentList[j].presentRecord();
					String tmpKey;
					LinkedList<MessageRMI> tmpValue;
					for(int i = 0; i<tmpList.length; i++) {
						tmpKey = j+"-"+i;
						tmpValue = tmpList[i];
						globalChannels.put(tmpKey, tmpValue);
					}
				}catch(Exception e) {
					if(userNotified==false) {
						System.out.println(e);
						userNotified = true;
					}
				}
			}
		// Print out the contents of the channel
		printState(globalChannels);
		// Reset the channel contents
		globalChannels = new HashMap<String, LinkedList<MessageRMI>>();
		}
		
	}
		
	public static void printState(Map<String, LinkedList<MessageRMI>> state) {
		/** Prints the contents of a recording.
		 * The recording is stored in a Map that maps a channel name to a linkedList of messages
		 */
		for(Map.Entry<String, LinkedList<MessageRMI>> entry: state.entrySet()) {
			Iterator<MessageRMI> it = entry.getValue().iterator();
			if(entry.getValue().size() != 0){
				System.out.println("Printing contents of channel "+entry.getKey());
			}else {
				System.out.println("Channel "+entry.getKey()+" is empty.");
			}
			while(it.hasNext()) {
				try{
					MessageRMI mess =  it.next();
					System.out.println("\tType: "+mess.getType()+"\n"+"\tOrigin: "+mess.getOrigin());
				}catch(Exception e) {
					System.out.println(e);
					break;
				}
			}
		}
	}
}


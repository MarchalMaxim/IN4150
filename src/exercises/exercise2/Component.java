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
public class Exercise2 {
		// Registry URL
		public static final String HOST = "localhost";
		public static final Integer PORT = 1099;
		
		public static void main(String args[]) {
			// Create and install a security manager
			System.setProperty("java.security.policy", "./my.policy");
			if(args.length==0) {
				args = new String[]{"5"};
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
			
			
		// Test a component by running it in a thread
		// Pass an anonymous function to it that lets the first component run a global state record
			/** TODO
			 * Make multiple threads and make the components send messages at random time intervals
			 */
		Thread thread = new Thread( () -> {
			try {
				ComponentList[0].recordGlobalState();
			}catch(Exception e) {
				System.out.println(e);
			}
		}
				);
		thread.start();
		try{
			thread.join();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		// Extract the channel record from the 
		Map<String, LinkedList<MessageRMI>> globalChannels = new HashMap<String, LinkedList<MessageRMI>>();
		Map<String, LinkedList<MessageRMI>> globalComponents = new HashMap<String, LinkedList<MessageRMI>>();
		while(globalComponents.size() == 0) {
			try{
				LinkedList<MessageRMI>[] tmpList = ComponentList[0].presentRecord();
				String tmpKey;
				LinkedList<MessageRMI> tmpValue;
				for(int i = 0; i<tmpList.length; i++) {
					if(i==0) {
						tmpKey = Integer.toString(i);
						tmpValue = tmpList[i];
						globalComponents.put(tmpKey, tmpValue);
					}
					tmpKey = "0-"+i;
					tmpValue = tmpList[i];
					globalChannels.put(tmpKey, tmpValue);
				}
			}catch(Exception e) {
				System.out.println("Global record not ready yet!");
			}
		}
		printState(globalChannels);
		
	}
		
	public static void printState(Map<String, LinkedList<MessageRMI>> state) {
		/** Prints the contents of a recording.
		 * The recording is stored in a Map that maps a channel name to a linkedList of messages
		 */
		for(Map.Entry<String, LinkedList<MessageRMI>> entry: state.entrySet()) {
			Iterator<MessageRMI> it = entry.getValue().iterator();
			System.out.println("Printing contents of key "+entry.getKey());
			while(it.hasNext()) {
				try{
					Message mess = (Message)it.next();
					System.out.println("Type: "+mess.getType());
					System.out.println("Content: "+mess.getContent());
				}catch(Exception e) {
					System.out.println();
				}
			}
		}
		}
	}


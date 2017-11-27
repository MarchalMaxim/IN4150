package exercises.exercise2;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exercise2 {
		// Registry URL
		public static final String HOST = "localhost";
		public static final Integer PORT = 1099;
		
		public static void main(int numComponents) {
			System.setProperty("java.security.policy", "./my.policy");
			
			// Create and install a security manager
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}
			
		// Make a registry so components can communicate
			Registry reg;
			try {
				reg = LocateRegistry.createRegistry(PORT);
			}catch(Exception e) {
				Logger.getLogger(Exercise2.class.getName()).log(Level.SEVERE, null, e);
			}
			
			// Make some components
			Component[] ComponentList = new Component[numComponents];
			for(Integer i =0; i<numComponents; i++) {
				try {
					ComponentList[i] = new Component(HOST, PORT, i, numComponents);
				}catch(Exception e) {
					
				}
			}
		}
}

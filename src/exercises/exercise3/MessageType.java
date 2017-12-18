package exercises.exercise3;

/**
 * Class to differ the different message types.
 */
public enum MessageType {

	CAPTURE(1),
	ACK(2),
	KILL(3),
	OK(4);
	
	private final int value;
	
	/**
	 * Constructor.
	 * @param value 
	 */
	MessageType(int value) {
		this.value = value;
	}
	
	/**
	 * Get the value of the message.
	 * @return  value
	 */
	public int getValue() {
		return value;
	}
	
}

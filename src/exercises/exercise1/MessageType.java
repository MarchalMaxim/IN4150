package exercises.exercise1;

public enum MessageType {

	MESSAGE(1),
	ACK(2),
	MARKER(3);
	
	private final int value;
	
	MessageType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
}

package messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class Response extends Message {
	public static final int ACK = 1;
	public static final int ECHO = 2;
	public static final int MESSAGE = 3;
	public static final int QUEUES = 4;
	public static final int ERROR = 5;
	public static final int EMPTY = 6;
	public static final int QUEUE_CREATED = 7;

	@Override
	public byte[] getBytes() throws IOException {
		return null;
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
	}
}

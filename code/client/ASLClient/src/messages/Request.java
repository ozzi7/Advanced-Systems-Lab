package messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;

public class Request extends Message {
	public static final int SEND_MESSAGE_TO_ANY = 1;
	public static final int SEND_MESSAGE_TO_RECEIVER = 2;
	public static final int QUERY_QUEUES = 3;
	public static final int PEEK_QUEUE_WITH_SENDER = 4;
	public static final int DELETE_QUEUE = 5;
	public static final int CREATE_QUEUE = 6;
	public static final int PEEK_QUEUE = 7;
	public static final int POP_QUEUE = 8;
	public static final int REQUEST_ECHO = 9;
	public static final int POP_QUEUE_WITH_SENDER = 10;

	public int receiverID;
	public int querySenderID;
	public int queueID;
	public String message;

	public Request() {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return null;
	}

	public Response executeQuery(Connection connection) {
		return null;
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
	}
}

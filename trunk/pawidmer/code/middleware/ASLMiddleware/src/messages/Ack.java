package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Ack extends Response {

	public static final int MESSAGE_SENT = 1;
	public static final int QUEUE_DELETED = 2;

	public int ackCode = -1;

	public Ack(int aAckCode, int aAssociatedClientID) {
		messageType = ACK;
		ackCode = aAckCode;
		associatedClientID = aAssociatedClientID;
	}

	public Ack(DataInputStream input) throws IOException {
		messageType = ACK;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
		ackCode = input.readInt();
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(16);
		out.writeInt(associatedClientID);
		out.writeInt(ackCode);
		out.flush();
	}
}

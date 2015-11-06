package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class QueueCreated extends Response {

	public int queueID = -1;

	public QueueCreated(int aQueueID, int aAssociatedClientID) {
		messageType = QUEUE_CREATED;
		queueID = aQueueID;
		associatedClientID = aAssociatedClientID;
	}

	public QueueCreated(DataInputStream input) throws IOException {
		messageType = QUEUE_CREATED;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
		queueID = input.readInt();
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(16);
		out.writeInt(associatedClientID);
		out.writeInt(queueID);
		out.flush();
	}
}

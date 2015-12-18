package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Queues extends Response {

	private ArrayList<Integer> queues = null;
	private String message = null;
	
	public Queues(int aAssociatedClientID, ArrayList<Integer> aQueues) {
		messageType = QUEUES;
		associatedClientID = aAssociatedClientID;
		queues = aQueues;
	}
	
	public Queues(DataInputStream input) throws IOException {
		messageType = QUEUES;

		messageLength = input.readInt();
		associatedClientID = input.readInt();

		queues = new ArrayList<Integer>();
		for (int i = 0; i < messageLength - 12; i = i + 4) {
			queues.add(input.readInt());
		}
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		if(queues != null) {
			out.writeInt(12 + queues.size() * 4);
			out.writeInt(associatedClientID);
			for (int i = 0; i < queues.size(); ++i) {
				out.writeInt(queues.get(i));
			}
		}
		else {
			out.writeInt(12);
			out.writeInt(associatedClientID);
		}
		out.flush();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

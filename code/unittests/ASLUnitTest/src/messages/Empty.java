package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Empty extends Response {

	public Empty(int aAssociatedClientID) {
		messageType = EMPTY;
		associatedClientID = aAssociatedClientID;
	}

	public Empty(DataInputStream input) throws IOException {
		messageType = EMPTY;
		associatedClientID = input.readInt();
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(12);
		out.writeInt(associatedClientID);
		out.flush();
	}
}

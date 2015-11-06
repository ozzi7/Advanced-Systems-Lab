package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Echo extends Response {

	private String message;
	
	public Echo(String aMessage, int aAssociatedClientID) {
		messageType = ECHO;
		setMessage(aMessage);
		associatedClientID = aAssociatedClientID;
	}

	public Echo(DataInputStream input) throws IOException {
		messageType = ECHO;

		messageLength = input.readInt();
		associatedClientID = input.readInt();

		byte[] data = new byte[messageLength - 12];
		input.readFully(data);
		message = new String(data, "UTF-8");
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(12 + message.length());
		out.writeInt(associatedClientID);
		out.writeBytes(getMessage());
		out.flush();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

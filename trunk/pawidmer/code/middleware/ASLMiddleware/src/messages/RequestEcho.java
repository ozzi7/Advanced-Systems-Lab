package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;

public class RequestEcho extends Request {

	public RequestEcho(int aAssociatedClientID, String aMessage) {
		messageType = REQUEST_ECHO;
		associatedClientID = aAssociatedClientID;
		setMessage(aMessage);
	}

	public RequestEcho(DataInputStream input) throws IOException {
		messageType = REQUEST_ECHO;
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
	@Override
	public Response executeQuery(Connection connection) {
		return new Echo(message,associatedClientID);
	}
}

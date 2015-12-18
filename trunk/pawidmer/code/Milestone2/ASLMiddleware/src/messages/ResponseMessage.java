package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResponseMessage extends Response {

	private String message;
	public int senderID; 
	
	public ResponseMessage(String aMessage, int aAssociatedClientID, int aSenderID) {
		messageType = MESSAGE;
		senderID = aSenderID;
		setMessage(aMessage);
		associatedClientID = aAssociatedClientID;
	}
	
	public ResponseMessage(DataInputStream input) throws IOException {
		messageType = MESSAGE;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
		senderID = input.readInt();
		
		if(messageLength > 16) {
			byte[] data = new byte[messageLength - 16];
			input.readFully(data);
			message = new String(data, "UTF-8");
		}
		else
			message = null;
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		if(message != null)
		{
			out.writeInt(16 + message.length());
			out.writeInt(associatedClientID);
			out.writeInt(senderID);
			out.writeBytes(getMessage());
		}
		else
		{
			out.writeInt(16);
			out.writeInt(associatedClientID);
			out.writeInt(senderID);
		}
		out.flush();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String aMessage) {
		this.message = aMessage;
	}
}

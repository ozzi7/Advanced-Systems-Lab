package messages;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Message {

	public int messageType = 0;
	public int associatedClientID = 0;
	public int messageLength = 0;

	public abstract byte[] getBytes() throws IOException;

	public abstract void send(DataOutputStream out) throws IOException;
}

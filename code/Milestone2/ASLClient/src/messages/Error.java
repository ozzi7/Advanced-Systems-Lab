package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Error extends Response {

	public static final int ERROR_QUEUE_ALREADY_EXISTS = 1;
	public static final int ERROR_NO_SUCH_QUEUE = 2;
	public static final int ERROR_QUEUE_NOT_EMPTY = 3;
	public static final int ERROR_AUTHENTICATION = 4;
	public static final int ERROR_QUEUE_EMPTY = 5;
	public static final int ERROR_GENERIC = 6;

	public int errorCode = -1;

	public Error(int aErrorCode, int aAssociatedClientID) {
		messageType = ERROR;
		errorCode = aErrorCode;
		associatedClientID = aAssociatedClientID;
	}

	public Error(DataInputStream input) throws IOException {
		messageType = ERROR;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
		errorCode = input.readInt();
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(16);
		out.writeInt(associatedClientID);
		out.writeInt(errorCode);
		out.flush();
	}
}

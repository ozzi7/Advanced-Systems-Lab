package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SendMessageToReceiver extends Request {

	public SendMessageToReceiver(int aReceiverID, int aQueueID, int clientID,
			String aMessage) {
		messageType = SEND_MESSAGE_TO_RECEIVER;
		receiverID = aReceiverID;
		queueID = aQueueID;
		associatedClientID = clientID;
		setMessage(aMessage);
	}
	
	public SendMessageToReceiver(DataInputStream input) throws IOException {
		messageType = SEND_MESSAGE_TO_RECEIVER;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
		receiverID = input.readInt();
		queueID = input.readInt();

		byte[] data = new byte[messageLength - 20];
		input.readFully(data);
		message = new String(data,"UTF-8");
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(20 + message.length());
		out.writeInt(associatedClientID);
		out.writeInt(receiverID);
		out.writeInt(queueID);
		out.writeBytes(getMessage());
		out.flush();
	}

	@Override
	public Response executeQuery(Connection connection) {
		try {
			String sqlString = "select send_message_to_receiver(?,?,?,?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(1,associatedClientID);
			preparedStmt.setInt(2, receiverID);
			preparedStmt.setString(3, message);
			preparedStmt.setInt(4,queueID);
			preparedStmt.execute();
			return new Ack(Ack.MESSAGE_SENT,associatedClientID);
		} catch (SQLException e) {
			if(e.getMessage().contains("queue_id")) {
				return new Error(Error.ERROR_NO_SUCH_QUEUE, associatedClientID);
			}
			else if(e.getMessage().contains("sender_id")) {
				return new Error(Error.ERROR_AUTHENTICATION, associatedClientID);
			}
			else {
				return new Error(Error.ERROR_GENERIC,associatedClientID);
			}
		}
	}
}

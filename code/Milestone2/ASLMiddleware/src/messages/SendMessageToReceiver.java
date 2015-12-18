package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
			String sqlString = "select * from send_message_to_receiver(?,?,?,?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(1,associatedClientID);
			preparedStmt.setInt(2, receiverID);
			preparedStmt.setString(3, message);
			preparedStmt.setInt(4,queueID);
			ResultSet rs = preparedStmt.executeQuery();
			connection.commit();
			if(rs.next()) {
				if(rs.getString("error_text").equals(new String("ERROR_NO_SUCH_QUEUE"))) {
					return new Error(Error.ERROR_NO_SUCH_QUEUE,associatedClientID);
				}
				else if(rs.getString("error_text").equals(new String("SUCCESS"))) {
					return new Ack(Ack.MESSAGE_SENT,associatedClientID);
				}
				else {
					return new Error(Error.ERROR_AUTHENTICATION,associatedClientID);
				}
			}
			else {
				return new Error(Error.ERROR_GENERIC,associatedClientID);
			}
		} catch (SQLException e) {
			return new Error(Error.ERROR_GENERIC,associatedClientID);
		}
	}
}

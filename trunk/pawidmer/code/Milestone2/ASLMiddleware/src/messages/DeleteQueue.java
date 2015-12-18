package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteQueue extends Request {

	public DeleteQueue(int aQueueID, int aAssociatedClientID) {
		queueID = aQueueID;
		messageType = DELETE_QUEUE;
		associatedClientID = aAssociatedClientID;
	}

	public DeleteQueue(DataInputStream input) throws IOException {
		messageType = DELETE_QUEUE;
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
	
	@Override
	public Response executeQuery(Connection connection) {
		try {
			String sqlString = "select * from delete_queue(?,?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(1,queueID);
			preparedStmt.setInt(2,associatedClientID);
			ResultSet rs = preparedStmt.executeQuery();
			connection.commit();
			if(rs.next()) {
				if(rs.getString("error_text").equals(new String("ERROR_AUTHENTICATION"))) {
					return new Error(Error.ERROR_AUTHENTICATION,associatedClientID);
				}
				else if(rs.getString("error_text").equals(new String("ERROR_NO_SUCH_QUEUE"))) {
					return new Error(Error.ERROR_NO_SUCH_QUEUE,associatedClientID);
				}
				else {
					return new Ack(Ack.QUEUE_DELETED,associatedClientID);
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

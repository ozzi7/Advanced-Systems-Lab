package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PeekQueue extends Request {

	public PeekQueue(int aQueueID, int aAssociatedClientID) {
		queueID = aQueueID;
		messageType = PEEK_QUEUE;
		associatedClientID = aAssociatedClientID;
	}

	public PeekQueue(DataInputStream input) throws IOException {
		messageType = PEEK_QUEUE;
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
			String sqlString = "select * from peek_queue(?,?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(2,associatedClientID);
			preparedStmt.setInt(1,queueID);
			ResultSet rs = preparedStmt.executeQuery();
			
			if(rs.next()) {
				if(rs.getString("error_text").equals(new String("ERROR_QUEUE_EMPTY"))) {
					return new Error(Error.ERROR_QUEUE_EMPTY,associatedClientID);
				}
				else if(rs.getString("error_text").equals(new String("ERROR_NO_SUCH_QUEUE"))) {
					return new Error(Error.ERROR_NO_SUCH_QUEUE,associatedClientID);
				}
				else if(rs.getString("error_text").equals(new String("ERROR_NO_MESSAGE"))) {
					return new Empty(associatedClientID);
				}
				else {
					return new ResponseMessage(rs.getString("message_text"),associatedClientID,rs.getInt("sender_id"));
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

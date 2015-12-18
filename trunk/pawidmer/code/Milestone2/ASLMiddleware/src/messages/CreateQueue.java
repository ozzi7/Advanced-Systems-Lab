package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateQueue extends Request {

	public CreateQueue(int aAssociatedClientID) {
		messageType = CREATE_QUEUE;
		associatedClientID = aAssociatedClientID;
	}

	public CreateQueue(DataInputStream input) throws IOException {
		messageType = CREATE_QUEUE;
		messageLength = input.readInt();
		associatedClientID = input.readInt();
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(messageType);
		out.writeInt(12);
		out.writeInt(associatedClientID);
		out.flush();
	}

	@Override
	public Response executeQuery(Connection connection) {
		try {
			String sqlString = "select * from create_queue(?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(1,associatedClientID);
			
			ResultSet rs = preparedStmt.executeQuery();
			connection.commit();
			if(rs.next()) {
				if(rs.getString("error_text").equals(new String("ERROR_AUTHENTICATION"))) {
					return new Error(Error.ERROR_AUTHENTICATION,associatedClientID);
				}
				else if(rs.getString("error_text").equals(new String("SUCCESS"))) {
					return new QueueCreated(rs.getInt("queue_id"),associatedClientID);
				}
				else {
					return new Error(Error.ERROR_GENERIC,associatedClientID);
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

package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryQueues extends Request {

	public QueryQueues(int aAssociatedClientID) {
		messageType = QUERY_QUEUES;
		associatedClientID = aAssociatedClientID;
	}

	public QueryQueues(DataInputStream input) throws IOException {
		messageType = QUERY_QUEUES;
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
			String sqlString = "select query_queues(?)";
			PreparedStatement preparedStmt = connection.prepareStatement(sqlString);
			preparedStmt.setInt(1,associatedClientID);
			ResultSet rs;
			rs = preparedStmt.executeQuery();
			
			ArrayList<Integer> queues = new ArrayList<Integer>();
			if(rs != null) {
				while (rs.next()) {
					queues.add(rs.getInt("queues_id"));
				}
			}
			return new Queues(associatedClientID, queues);
		} catch (SQLException e) {
			return new Error(Error.ERROR_GENERIC,associatedClientID);
		}
	}
}

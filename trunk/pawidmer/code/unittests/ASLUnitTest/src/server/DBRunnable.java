package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import logger.ErrorLogger;
import logger.EventLogger;
import messages.Request;
import messages.Response;

public class DBRunnable implements Runnable {

	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;
	private EventLogger eventLogger;
	private ErrorLogger errorLogger;
	
	private Connection DBconnection = null;
	private String dbIp = "127.0.0.1";
	private String dbUser = "postgres";
	private String dbPort = "5432";
	private String dbPassword = "wurst";

	public DBRunnable(LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		initLogger();
		connectToDB();
	}

	public DBRunnable(LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues,
			String aDbIp, String aDbPort, String aDbUser, String aDbPassword) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		dbIp = aDbIp;
		dbPort = aDbPort;
		dbUser = aDbUser;
		dbPassword = aDbPassword;
		initLogger();
		connectToDB();
	}

	@Override
	public void run() {		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Request request = requestQueue.take();
				eventLogger.log("Found request");
				Response response = request.executeQuery(DBconnection);
				responseQueues.get(request.associatedClientID-1).put(response);
				eventLogger.log("Received reply");
			} catch (InterruptedException | IOException e) {
				eventLogger.saveToDisk();
			}
		}
		try {
			DBconnection.close();
			eventLogger.saveToDisk();
		} catch (SQLException e) {
			errorLogger.log(e);
		}
	}

	private void connectToDB() {
		try {
			Class.forName("org.postgresql.Driver");
			DBconnection = DriverManager.getConnection("jdbc:postgresql://"
					+ dbIp + ":" + dbPort + "/ASL", dbUser, dbPassword);
			DBconnection.setAutoCommit(true);

		} catch (SQLException | ClassNotFoundException e) {
				errorLogger.log(e);
			return;
		}
	}
	private void initLogger() {
		try {
			eventLogger= new EventLogger("DBThreads","Event_"+Thread.currentThread().getName());
			errorLogger= new ErrorLogger("DBThreads","Error_" +Thread.currentThread().getName());
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}
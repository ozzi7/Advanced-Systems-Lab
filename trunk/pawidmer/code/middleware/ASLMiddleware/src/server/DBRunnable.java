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
import middlewareSettings.Settings;

public class DBRunnable implements Runnable {

	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;
	private EventLogger eventLogger;
	private EventLogger responseLogger;
	private ErrorLogger errorLogger;
	private Connection DBconnection = null;
	private String dbIp = "127.0.0.1";
	private String dbUser = "postgres";
	private String dbPort = "5432";
	private String dbPassword = "wurst";
	private int dbID;
	
	public DBRunnable(int aDbThreadID,LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		dbID = aDbThreadID;
		initLogger();
		connectToDB();
	}

	public DBRunnable(int aDbThreadID,LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues,
			String aDbIp, String aDbPort, String aDbUser, String aDbPassword) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		dbIp = aDbIp;
		dbPort = aDbPort;
		dbUser = aDbUser;
		dbPassword = aDbPassword;
		dbID = aDbThreadID;
		initLogger();
		connectToDB();
	}

	@Override
	public void run() {		
		long startTime = 0;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Request request = requestQueue.take();
				eventLogger.log("Found client request");
				startTime = System.nanoTime();
				Response response = request.executeQuery(DBconnection);
				responseQueues.get(request.associatedClientID-1).put(response);
				responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf((System.nanoTime() -startTime)));
				eventLogger.log("Received DB response");
			} catch (InterruptedException e) {
				errorLogger.log("Interrupted");
				return;
			}
			catch (IOException e) {
				errorLogger.log("IO Exception");
				return;
			}
		}
		try {
			DBconnection.close();
			return;
		} catch (SQLException e) {
			errorLogger.log(e);
			return;
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
			eventLogger= new EventLogger("DBThreads","Event_"+Settings.GLOBAL_FILE_NAME+"_Thread"+dbID);
			errorLogger= new ErrorLogger("DBThreads","Error_"+Settings.GLOBAL_FILE_NAME+"_Thread"+dbID);
			responseLogger = new EventLogger("DBThreads","Response_"+Settings.GLOBAL_FILE_NAME+"_Thread"+dbID);
			responseLogger.logging = Settings.RESPONSE_LOGGING;
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}
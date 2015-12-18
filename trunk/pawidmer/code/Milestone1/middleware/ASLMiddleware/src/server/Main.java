package server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import benchmark.MessageGenerator;


import messages.Request;
import messages.Response;
import middlewareSettings.Settings;

public class Main {

	public static void main(String[] argv) {

		if(argv.length != 11)
	    {
	        System.out.println("Proper usage: Database IP, Middleware listening port, # clients, # DB connections, server run time [ms]," +
	        "Enable error logging [1 = true], Enable event logging [1 = true], Enable response logging [1 = true], Enable throughput logging [1 = true], " +
	        "Enable Message Generator [0 = off, 1,2,3,4 MessageGen Method], startClientID");
	        System.exit(0);
	    }
		String dbIP = argv[0];
		int mwPort = Integer.parseInt(argv[1]);
		Settings.NOF_CLIENTS = Integer.parseInt(argv[2]);
		Settings.NOF_DBTHREADS = Integer.parseInt(argv[3]);
		int ttl = Integer.parseInt(argv[4]);
		Settings.ERROR_LOGGING = (1 == Integer.parseInt(argv[5]));
		Settings.EVENT_LOGGING  = (1 == Integer.parseInt(argv[6]));
		Settings.RESPONSE_LOGGING = (1 == Integer.parseInt(argv[7]));
		Settings.THROUGHPUT_LOGGING  = (1 == Integer.parseInt(argv[8]));
		boolean useMessageGenerator = (0!=Integer.parseInt(argv[9]));
		int messageGenMethod = Integer.parseInt(argv[9]);
		int firstClientID = Integer.parseInt(argv[10]);
		Settings.GLOBAL_FILE_NAME = "StartingID" + firstClientID+ "Type" + messageGenMethod + "_Cons"+Settings.NOF_DBTHREADS;
				
		ArrayList<Thread> dbThreads = new ArrayList<Thread>(Settings.NOF_DBTHREADS);
		Thread serverThread;
		Thread messageGenThread = null;
		
		/*Create one request queue and a response queue for each client */
		LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>(1000);
		ArrayList<LinkedBlockingQueue<Response>> responseQueues = new ArrayList<LinkedBlockingQueue<Response>>();
		
		if(useMessageGenerator == false)
			for (int i = 0; i < firstClientID -1 + Settings.NOF_CLIENTS; ++i) {
				responseQueues.add(new LinkedBlockingQueue<Response>(1));
			}
		else
			responseQueues.add(new LinkedBlockingQueue<Response>(200));

		/* Create a fixed number of database threads handling interaction with the database*/
		for (int i = 0; i < Settings.NOF_DBTHREADS; i++) {
			DBRunnable dbRunnable = new DBRunnable(i,requestQueue, responseQueues,dbIP,"5432","postgres","wurst");
			dbThreads.add(new Thread(dbRunnable));
			dbThreads.get(i).start();
		}
		/* Create a server managing new client connections*/
		Server server = new Server(requestQueue, responseQueues,mwPort);
		serverThread = new Thread(server);
		serverThread.start();

		/* Fill request queue without using clients */
		if(useMessageGenerator) {
			responseQueues.add(new LinkedBlockingQueue<Response>(1));
			MessageGenerator msgGen = new MessageGenerator(messageGenMethod,requestQueue,responseQueues.get(0));
			messageGenThread = new Thread(msgGen);
			messageGenThread.start();
		}
		
		/* Destroy threads after some time*/
		try {
			Thread.sleep(ttl);
			if(useMessageGenerator) {
				messageGenThread.interrupt();
				System.out.println("Stopped message generator");
			}
			Thread.sleep(1000);
			for (int i = 0; i < Settings.NOF_DBTHREADS; i++) {
				dbThreads.get(i).interrupt();
			}
			System.out.println("Stopped db threads");
			serverThread.interrupt();
			System.out.println("Stopped server");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("Main interrupted");
		}
        System.exit(0);
	}
}
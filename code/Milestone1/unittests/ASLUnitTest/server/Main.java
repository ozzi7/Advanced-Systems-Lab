package server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import settings.Settings;

import messages.Request;
import messages.Response;

public class Main {

	public static void main(String[] argv) {

		ArrayList<Thread> dbThreads = new ArrayList<Thread>(Settings.NOF_DBTHREADS);
		ArrayList<Thread> clientThreads = new ArrayList<Thread>(Settings.NOF_CLIENTS);
		Thread serverThread;
		
		/*Create one request queue and a response queue for each client */
		LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>(Settings.NOF_CLIENTS);
		ArrayList<LinkedBlockingQueue<Response>> responseQueues = new ArrayList<LinkedBlockingQueue<Response>>();

		for (int i = 0; i < Settings.NOF_CLIENTS; ++i) {
			responseQueues.add(new LinkedBlockingQueue<Response>(1));
		}

		/* Create a fixed number of database threads handling interaction with the database*/
		for (int i = 0; i < Settings.NOF_DBTHREADS; i++) {
			DBRunnable dbRunnable = new DBRunnable(requestQueue, responseQueues);
			dbThreads.add(new Thread(dbRunnable));
			dbThreads.get(i).start();
		}
		/* Create a server managing new client connections*/
		Server server = new Server(requestQueue, responseQueues,3351);
		serverThread = new Thread(server);
		serverThread.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Create client threads */
		for (int i = 0; i < Settings.NOF_CLIENTS; i++) {
			ClientRunnable clientRunnable = new ClientRunnable(i+1, Settings.NOF_CLIENTS, "127.0.0.1", 3351);
			clientThreads.add(new Thread(clientRunnable));
			clientThreads.get(i).start();
		}
		// MessageGenerator msgGen = new MessageGenerator(requestQueue);
		// new Thread(msgGen).start();
		
		/* Destroy threads after some time*/
		try {
			Thread.sleep(4000);
			for (int i = 0; i < Settings.NOF_CLIENTS; i++) {
				clientThreads.get(i).interrupt();
			}
			Thread.sleep(500);
			for (int i = 0; i < Settings.NOF_DBTHREADS; i++) {
				dbThreads.get(i).interrupt();
			}
			serverThread.interrupt();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
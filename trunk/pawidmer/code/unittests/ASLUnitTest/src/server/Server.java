package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import logger.ErrorLogger;
import logger.EventLogger;
import messages.Request;
import messages.Response;

public class Server implements Runnable {

	private ServerSocket serverSocket;
	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;
	private int port;
	private EventLogger eventLogger;
	private ErrorLogger errorLogger;
	
	public Server(LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues, int aPort) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		port = aPort;
	}

	@Override
	public void run() {
		try {
			eventLogger= new EventLogger("Server","Event_"+Thread.currentThread().getName());
			errorLogger= new ErrorLogger("Server","Error_"+Thread.currentThread().getName());
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException ioe) {
				errorLogger.log(ioe);
				System.exit(-1);
			}
			while (!Thread.interrupted()) {
				eventLogger.log("Server accepting new connections");
				Socket clientSocket;
				try {
					clientSocket = serverSocket.accept();
					eventLogger.log("Server accepted client connection");
					new Thread(new ClientServiceRunnable(clientSocket, requestQueue,
							responseQueues)).start();
				} catch (IOException e) {
					errorLogger.log(e);
					eventLogger.saveToDisk();
				}
			}
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}
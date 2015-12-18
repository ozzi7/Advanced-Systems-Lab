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
import middlewareSettings.Settings;

public class Server implements Runnable {

	private ServerSocket serverSocket;
	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;
	private int port;
	private ErrorLogger errorLogger;
	private EventLogger eventLogger;
	
	public Server(LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues, int aPort) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		port = aPort;
		initLogger();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			ArrayList<Socket> clientSockets = new ArrayList<Socket>(Settings.NOF_CLIENTS);
			for(int i = 0; i < Settings.NOF_CLIENTS; ++i) {
				Socket clientSocket = serverSocket.accept();
				clientSockets.add(clientSocket);
				eventLogger.log("Client connected");
			}
			for(int i = 0; i < Settings.NOF_CLIENTS; ++i) {
				new Thread(new ClientServiceRunnable(clientSockets.get(i), requestQueue,
						responseQueues)).start();
			}
		} catch (IOException ioe) {
			errorLogger.log(ioe);
			System.exit(-1);
		}

	}

	private void initLogger() {
		try {
			errorLogger= new ErrorLogger("ServerThread","Error_"+Thread.currentThread().getName());
			eventLogger= new EventLogger("ServerThread","Event_"+Thread.currentThread().getName());
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}
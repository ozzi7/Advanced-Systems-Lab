package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import messages.Request;
import messages.Response;

public class Server implements Runnable {

	private ServerSocket serverSocket;
	private boolean running = true;
	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;
	private int port;

	public Server(LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues, int aPort) {
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
		port = aPort;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			System.out.println("Could not create server socket.");
			System.exit(-1);
		}
		while (!Thread.interrupted()) {
			System.out.println("Server accepting new connections");
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				new Thread(new ClientServiceRunnable(clientSocket, requestQueue,
						responseQueues)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
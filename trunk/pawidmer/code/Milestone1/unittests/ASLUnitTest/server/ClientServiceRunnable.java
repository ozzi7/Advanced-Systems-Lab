package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import messages.CreateQueue;
import messages.DeleteQueue;
import messages.PeekQueue;
import messages.PeekQueueWithSender;
import messages.PopQueue;
import messages.PopQueueWithSender;
import messages.QueryQueues;
import messages.Request;
import messages.RequestEcho;
import messages.Response;
import messages.SendMessageToAny;
import messages.SendMessageToReceiver;

public class ClientServiceRunnable implements Runnable {

	private Socket clientServiceSocket;
	private LinkedBlockingQueue<Request> requestQueue = null;
	private ArrayList<LinkedBlockingQueue<Response>> responseQueues = null;

	public ClientServiceRunnable(Socket aClientServiceSocket,
			LinkedBlockingQueue<Request> aRequestQueue,
			ArrayList<LinkedBlockingQueue<Response>> aResponseQueues) {
		clientServiceSocket = aClientServiceSocket;
		requestQueue = aRequestQueue;
		responseQueues = aResponseQueues;
	}

	@Override
	public void run() {
		DataInputStream dataInFromClient = null;
		InputStream inFromClient = null;
		DataOutputStream output = null;

		try {
			inFromClient = clientServiceSocket.getInputStream();
			dataInFromClient = new DataInputStream(inFromClient);
			output = new DataOutputStream(clientServiceSocket.getOutputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		System.out.println("ClientServiceThread listening");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				int messageType = dataInFromClient.readInt();

				/* Parse message, create Request */
				System.out.println("ClientServiceThread received request type "+messageType );
				Request clientRequest = null;
				switch (messageType) {
				case Request.SEND_MESSAGE_TO_ANY:
					clientRequest = new SendMessageToAny(dataInFromClient);
					break;
				case Request.SEND_MESSAGE_TO_RECEIVER:
					clientRequest = new SendMessageToReceiver(dataInFromClient);
					break;
				case Request.QUERY_QUEUES:
					clientRequest = new QueryQueues(dataInFromClient);
					break;
				case Request.PEEK_QUEUE_WITH_SENDER:
					clientRequest = new PeekQueueWithSender(dataInFromClient);
					break;
				case Request.DELETE_QUEUE:
					clientRequest = new DeleteQueue(dataInFromClient);
					break;
				case Request.CREATE_QUEUE:
					clientRequest = new CreateQueue(dataInFromClient);
					break;
				case Request.PEEK_QUEUE:
					clientRequest = new PeekQueue(dataInFromClient);
					break;
				case Request.POP_QUEUE:
					clientRequest = new PopQueue(dataInFromClient);
					break;
				case Request.REQUEST_ECHO:
					clientRequest = new RequestEcho(dataInFromClient);
					break;
				case Request.POP_QUEUE_WITH_SENDER:
					clientRequest = new PopQueueWithSender(dataInFromClient);
					break;
				}

				/* Insert request into requestQueue */
				requestQueue.add(clientRequest);

				/* Wait for response */
				Response response = responseQueues.get(
						clientRequest.associatedClientID-1).take();

				/* Send response to client */
				response.send(output);

			} catch (IOException e1) {
				System.out.println("Client left");
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			dataInFromClient.close();
			inFromClient.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}

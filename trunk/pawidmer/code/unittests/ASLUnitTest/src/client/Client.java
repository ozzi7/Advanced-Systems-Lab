package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import dataGenerator.DataGenerator;

import logger.ErrorLogger;
import logger.EventLogger;
import messages.Ack;
import messages.Echo;
import messages.Empty;
import messages.Error;
import messages.ResponseMessage;
import messages.QueueCreated;
import messages.Queues;
import messages.Request;
import messages.Response;
import messages.SendMessageToReceiver;

public class Client {

	private Socket clientSocket;
	private int clientID;
	private int nofClients;
	private DataGenerator dataGen;
	private EventLogger eventLogger;
	private ErrorLogger errorLogger;

	public Client(int aClientID, int aNofClients, String ip, int port) {
		try {
			clientSocket = new Socket(ip, port);
			clientID = aClientID;
			dataGen = new DataGenerator();
			nofClients = aNofClients;

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			eventLogger= new EventLogger("ClientThreads",Thread.currentThread().getName());
			errorLogger= new ErrorLogger("ClientThreads",Thread.currentThread().getName());
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
	
	public Response Send(Request request) {
		DataInputStream dataInFromServer = null;
		InputStream inFromServer = null;
		DataOutputStream output = null;
			
		try {
			inFromServer = clientSocket.getInputStream();
			dataInFromServer = new DataInputStream(inFromServer);
			output = new DataOutputStream(clientSocket.getOutputStream());
			
			/* Send request */
			eventLogger.log("Connected to server");
			
			request.send(output);
			eventLogger.log("Sent request");
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();
			eventLogger.log("Received reply");
			Response clientResponse = null;
			switch (messageType) {
			case Response.ACK:
				clientResponse = new Ack(dataInFromServer);
				break;
			case Response.MESSAGE:
				clientResponse = new ResponseMessage(dataInFromServer);
				break;
			case Response.ECHO:
				clientResponse = new Echo(dataInFromServer);
				break;
			case Response.ERROR:
				clientResponse = new Error(dataInFromServer);
				break;
			case Response.QUEUES:
				clientResponse = new Queues(dataInFromServer);
				break;
			case Response.QUEUE_CREATED:
				clientResponse = new QueueCreated(dataInFromServer);
				break;
			case Response.EMPTY:
				clientResponse = new Empty(dataInFromServer);
				break;
			}
			return clientResponse;

		} catch (IOException e) {
			errorLogger.log(e);
		}		
		try {
			output.close();
			inFromServer.close();
			dataInFromServer.close();
		} catch (IOException e) {
			errorLogger.log(e);
		}
		return null;
	}
}
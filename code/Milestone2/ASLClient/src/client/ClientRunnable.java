package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import clientSettings.Settings;


import dataGenerator.DataGenerator;

import logger.ErrorLogger;
import logger.EventLogger;
import messages.Ack;
import messages.Echo;
import messages.Empty;
import messages.Error;
import messages.PeekQueue;
import messages.PopQueue;
import messages.QueryQueues;
import messages.RequestEcho;
import messages.ResponseMessage;
import messages.QueueCreated;
import messages.Queues;
import messages.Request;
import messages.Response;
import messages.SendMessageToAny;
import messages.SendMessageToReceiver;

public class ClientRunnable implements Runnable {

	private Socket clientSocket = null;
	private int clientID;
	private int nofClients;
	private DataGenerator dataGen;
	private EventLogger eventLogger;
	private EventLogger throughputLogger;
	private EventLogger responseLogger;
	private ErrorLogger errorLogger;
	private String ip;
	private int port;
	private int testType;
	private double thinkTime;
	
	DataInputStream dataInFromServer = null;
	InputStream inFromServer = null;
	DataOutputStream output = null;
	
	public ClientRunnable(int aClientID, int aNofClients, String aIp, int aPort, int aTestType, double aThinkTime) {	
		clientID = aClientID;
		dataGen = new DataGenerator();
		nofClients = aNofClients;
		ip = aIp;
		port = aPort;
		testType = aTestType;
		thinkTime = aThinkTime;
		initLogger();
		
		try {
			Thread.sleep(dataGen.getRandomInt(1, 300));
			clientSocket = new Socket(ip, port);
			clientSocket.setTcpNoDelay(true); 
			
			output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
			Thread.sleep(100);			
			inFromServer = clientSocket.getInputStream();
			dataInFromServer = new DataInputStream(new BufferedInputStream(inFromServer));
			eventLogger.log("Connected to server");
			
		} catch (UnknownHostException e) {
			errorLogger.log("Unknown host");
		} catch (IOException e) {
			errorLogger.log("Client could not connect");
			System.out.println("Client "+clientID + " could not connect");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {	
		try {
			switch(testType) {
		case 1:
			sendEchoRequests();
			break;
		case 2:
			sendOnlyValidReceiver2000Char();
			break;
		case 3:
			sendMixedLoad();
			break;
		case 4:
			sendOnlyValidReceiverXChar();
			break;
		case 5:
			stabilityTest();
			break;
			}
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			output.close();
			inFromServer.close();
			dataInFromServer.close();
		} catch (IOException e) {
			errorLogger.log(e);
		}
	}
	
	/*Used for unit tests to send only 1 message and close connection*/
	public Response Send(Request request) {
		try {
			/* Send request */
			request.send(output);
			eventLogger.log("Sent request");
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();	
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
			eventLogger.log("Received reply");
			
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
	/* Used for throughput tests between middleware and clients without involving the database */
	void sendEchoRequests() throws IOException, InterruptedException {
		long startTimeResponse = System.nanoTime();
		long startTimeT= System.nanoTime();
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		long thinkTimeStart = System.nanoTime();
		while(!Thread.interrupted()) {
			
			/* Send request */
			Request request = new RequestEcho(clientID,dataGen.getRandomString(200));
			startTimeResponse = System.nanoTime();
			request.send(output);
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();

			/* Parse response */
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
			}
			responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf(System.nanoTime()-startTimeResponse));
			
			/*Measure throughput*/
			receivedMessages++;
			if(System.nanoTime() - startTimeT >= 1000000000) {
				throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
				startTimeT = System.nanoTime();
				prevReceivedMessages = receivedMessages;
			}
			/*Thread.sleep(Math.max((long)(((thinkTimeStart + receivedMessages*(thinkTime*1000000)) - System.nanoTime())/1000000),0));*/
			Thread.sleep((long)thinkTime);
		}
	}
	void sendOnlyValidReceiver2000Char() throws IOException, InterruptedException {
		long startTimeResponse = System.nanoTime();
		long startTimeT= System.nanoTime();
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		while(!Thread.interrupted()) {
			
			/* Send request */
			Request request = new SendMessageToReceiver(dataGen.getRandomInt(
					1, nofClients), dataGen.getRandomInt(0, nofClients-1), clientID,
					dataGen.getRandomString(2000));
			startTimeResponse = System.nanoTime();
			request.send(output);

			/* Wait for response */
			int messageType = dataInFromServer.readInt();
			
			/* Parse response */
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
			}
			responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf(System.nanoTime()-startTimeResponse));
			
			/*Measure throughput*/
			receivedMessages++;
			if(System.nanoTime() - startTimeT >= 1000000000) {
				throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
				startTimeT = System.nanoTime();
				prevReceivedMessages = receivedMessages;
			}
			Thread.sleep((long)thinkTime);
		}
	}
	public void sendMixedLoad() throws IOException, InterruptedException
	{		
		long startTimeResponse = System.nanoTime();
		long startTimeT= System.nanoTime();
		long thinkTimeStart = System.nanoTime();
		int receivedMessages = 0;
		int prevReceivedMessages = 0;	
		while(!Thread.interrupted()) {

			/* Send request */
			int random = dataGen.getRandomInt(1,10);
			startTimeResponse = System.nanoTime();
			
			if(random < 5){
				/*Send a message*/
				Request request = new SendMessageToAny(dataGen.getRandomString(200),clientID,dataGen.getRandomInt(1, 100));
				
				request.send(output);
				eventLogger.log("Sent request send");
			}
			else if(random >= 5 && random <= 8) {
				/*Pop a message*/
				Request request = new PopQueue(dataGen.getRandomInt(1, 100),clientID);
				request.send(output);
				eventLogger.log("Sent request pop");
			}
			else if(random > 8) {
				/*Peek a message*/
				Request request = new PeekQueue(dataGen.getRandomInt(1, 100),clientID);
				request.send(output);
				eventLogger.log("Sent request peek");
			}
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();
			eventLogger.log("Received response "+messageType);
			
			/* Parse response */
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
			case Response.EMPTY:
				clientResponse = new Empty(dataInFromServer);
				break;
			case Response.QUEUE_CREATED:
				clientResponse = new QueueCreated(dataInFromServer);
				break;
			}
			if(clientID == 1)
			responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf(System.nanoTime()-startTimeResponse));

			
			/*Measure throughput*/
			receivedMessages++;
			if(System.nanoTime() - startTimeT >= 1000000000) {
				if(clientID == 1)
				throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
				startTimeT = System.nanoTime();
				prevReceivedMessages = receivedMessages;
			}
			Thread.sleep((long)thinkTime);
		}
	}
	void sendOnlyValidReceiverXChar() throws IOException, InterruptedException {
		long startTimeResponse = System.nanoTime();
		long startTimeT= System.nanoTime();
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		while(!Thread.interrupted()) {
			
			/* Send request */
			Request request = new SendMessageToReceiver(dataGen.getRandomInt(
					1, nofClients), dataGen.getRandomInt(1, nofClients-1), clientID,
					dataGen.getRandomString(2000));
			startTimeResponse = System.nanoTime();
			request.send(output);
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();
			
			/* Parse response */
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
			}
			responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf(System.nanoTime()-startTimeResponse));
			
			/*Measure throughput*/
			receivedMessages++;
			if(System.nanoTime() - startTimeT >= 1000000000) {
				throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
				startTimeT = System.nanoTime();
				prevReceivedMessages = receivedMessages;
			}
			Thread.sleep((long)thinkTime);
		}
	}
	public void stabilityTest() throws IOException, InterruptedException
	{		
		long startTimeResponse = System.nanoTime();
		long startTimeT= System.nanoTime();
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		while(!Thread.interrupted()) {
			
			/* Send request */
			int random = dataGen.getRandomInt(1,100);
			startTimeResponse = System.nanoTime();
			
			if(random < 51){
				/*Send a message*/
				Request request = new SendMessageToAny(dataGen.getRandomString(20),clientID,dataGen.getRandomInt(1, 100));
				request.send(output);
				eventLogger.log("Sent request send");
			}
			else if(random < 95) {
				/*Peek a message*/
				Request request = new PeekQueue(dataGen.getRandomInt(1, 100),clientID);
				request.send(output);
				eventLogger.log("Sent request peek");
			}
			else if(random < 97) {
				/*Query queues message*/
				Request request = new QueryQueues(clientID);
				request.send(output);
				eventLogger.log("Sent request query queues");
			}
			else {
				/*Pop queue message*/
				Request request = new PopQueue(dataGen.getRandomInt(1, 100),clientID);
				request.send(output);
				eventLogger.log("Sent request pop");
			}
			
			/* Wait for response */
			int messageType = dataInFromServer.readInt();
			eventLogger.log("Received response "+messageType);	
			
			/* Parse response */
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
				System.out.println("response queues");
				clientResponse = new Queues(dataInFromServer);
				break;
			}
			responseLogger.logNoTimestamp((System.nanoTime()/1000000000) + " " +String.valueOf(System.nanoTime()-startTimeResponse));

			
			/*Measure throughput*/
			receivedMessages++;
			if(System.nanoTime() - startTimeT >= 1000000000) {
				throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
				startTimeT = System.nanoTime();
				prevReceivedMessages = receivedMessages;
			}
			Thread.sleep((long)thinkTime);
		}
	}
	void initLogger() {
		try {
			if(clientID == 1){
			throughputLogger= new EventLogger("ClientThreads","Throughput_"+clientID);
			responseLogger= new EventLogger("ClientThreads","Response_"+clientID);
			errorLogger= new ErrorLogger("ClientThreads","Error_"+clientID);
			eventLogger = new EventLogger("ClientThreads","Event"+clientID);
			responseLogger.logging = Settings.RESPONSE_LOGGING;
			throughputLogger.logging = Settings.THROUGHPUT_LOGGING;}
			
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}
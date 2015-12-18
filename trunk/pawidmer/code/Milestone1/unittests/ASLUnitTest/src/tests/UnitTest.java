package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import messages.CreateQueue;
import messages.DeleteQueue;
import messages.PeekQueue;
import messages.PeekQueueWithSender;
import messages.PopQueue;
import messages.PopQueueWithSender;
import messages.Request;
import messages.RequestEcho;
import messages.Response;
import messages.Ack;
import messages.Echo;
import messages.Error;
import messages.ResponseMessage;
import messages.Empty;
import messages.QueueCreated;
import messages.SendMessageToAny;
import messages.SendMessageToReceiver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import server.DBRunnable;
import server.Server;
import settings.Settings;
import client.Client;
import dataGenerator.DataGenerator;

/*
 * The test assumes 100 (1-100) clients as well as 100 (1-100) queues initially.
 */

public class UnitTest {

	private String host = "127.0.0.1";
	private int port = 3379;
	private static boolean setUpIsDone = false;
	private static Thread dbThread = null;
	private static Thread serverThread = null;
	
	public static void main(String[] args) throws Exception {                    
	       JUnitCore.main(
	         "tests.UnitTest");            
	}
	
	@Before
	public void setUp() throws Exception {
		if(!setUpIsDone) {
			setUpIsDone = true;
			LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>(Settings.REQUEST_QUEUE_SIZE);
			ArrayList<LinkedBlockingQueue<Response>> responseQueues = new ArrayList<LinkedBlockingQueue<Response>>();
			for (int i = 0; i < 200; ++i) {
				responseQueues.add(new LinkedBlockingQueue<Response>(1));
			}
			DBRunnable dbRunnable = new DBRunnable(requestQueue, responseQueues);
			dbThread = new Thread(dbRunnable);
			dbThread.start();

			Server server = new Server(requestQueue, responseQueues, port);
			serverThread = new Thread(server);
			serverThread.start();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		dbThread.interrupt();
		serverThread.interrupt();
	}
	
	@Test
	public void testPopQueue_ResponseMessage() throws IOException {	
		
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		int receiverID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to self on new queue */
		request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Pop the new message*/
		request = new PopQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}
	@Test
	public void testPopQueue_ErrorQueueEmpty() throws IOException {	
		
		int clientID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);

		int queueID = ((QueueCreated)response1).queueID;
				
		/*Try to pop a message*/
		request = new PopQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_QUEUE_EMPTY);
	}
	@Test
	public void testPopQueue_NoMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to someone else on new queue */
		request = new SendMessageToReceiver(2,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Try to pop a message*/
		request = new PopQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Empty);
	}
	@Test
	public void testPopQueue_ErrorNoSuchQueue() throws IOException {	
		int clientID = 1;
		int receiverID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
			
		/*Try to pop a message*/
		request = new PopQueue(queueID+1,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testPopQueueWithSender_ResponseMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		int receiverID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to self on new queue */
		request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Pop the new message*/
		request = new PopQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}
	@Test
	public void testPopQueueWithSender_ErrorQueueEmpty() throws IOException {	
		int clientID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
				
		/*Try to pop a message*/
		request = new PopQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_QUEUE_EMPTY);
	}
	@Test
	public void testPopQueueWithSender_NoMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to someone else on new queue */
		request = new SendMessageToReceiver(2,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Try to pop a message*/
		request = new PopQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Empty);
	}
	@Test
	public void testPopQueueWithSender_ErrorNoSuchQueue() throws IOException {	
		int clientID = 1;
		int receiverID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
			
		/*Try to pop a message*/
		request = new PopQueueWithSender(queueID+1,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testSendMessageToAny() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue*/
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message */
		request = new SendMessageToAny(message,clientID,queueID);
		Response response2 = client.Send(request);
		assertTrue(response2 instanceof Ack);
		assertTrue(((Ack)response2).ackCode == Ack.MESSAGE_SENT);
	}
	@Test
	public void testSendMessageToAny_AuthenticationError() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(101, 200);
		int queueID = 2;
		Client client = new Client(clientID, 100, host, port);
		String message = dataGen.getRandomString(2000);

		Request request = new SendMessageToAny(message,clientID,queueID);
		Response response = client.Send(request);
		assertTrue(response instanceof Error);
		assertTrue(((Error)response).errorCode == Error.ERROR_AUTHENTICATION);
	}
	
	@Test
	public void testSendMessageToAny_NoSuchQueueError() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		String message = dataGen.getRandomString(2000);
		Client client = new Client(clientID, 100, host, port);		
		
		/*Create new queue*/
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;

		request = new SendMessageToAny(message,clientID,queueID+1);
		Response response = client.Send(request);
		assertTrue(response instanceof Error);
		assertTrue(((Error)response).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	
	@Test
	public void testSendMessageToReceiver() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		int receiverID = dataGen.getRandomInt(1, 100);
		int queueID = 2;
		String message = dataGen.getRandomString(2000);
		
		Client client = new Client(clientID, 100, host, port);
		Request request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response = client.Send(request);
		assertTrue(response instanceof Ack);
		assertTrue(((Ack)response).ackCode == Ack.MESSAGE_SENT);
	}
	
	@Test
	public void testSendMessageToReceiver_AuthenticationError() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(101, 200);
		int receiverID = dataGen.getRandomInt(1, 100);
		int queueID = 2;
		String message = dataGen.getRandomString(2000);
		
		Client client = new Client(clientID, 100, host, port);
		Request request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response = client.Send(request);
		assertTrue(response instanceof Error);
		assertTrue(((Error)response).errorCode == Error.ERROR_AUTHENTICATION);
	}
	
	@Test
	public void testSendMessageToReceiver_NoSuchQueueError() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		int receiverID = dataGen.getRandomInt(1, 100);
		String message = dataGen.getRandomString(2000);
		Client client = new Client(clientID, 100, host, port);		
		
		/*Create new queue*/
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		Request request2 = new SendMessageToReceiver(receiverID,queueID+1,clientID,message);
		Response response = client.Send(request2);
		assertTrue(response instanceof Error);
		assertTrue(((Error)response).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testRequestEcho() throws IOException {
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		String message = dataGen.getRandomString(2000);
		
		Client client = new Client(clientID, 100, host, port);
		Request request = new RequestEcho(clientID,message);
		Response response = client.Send(request);
		assertTrue(response instanceof Echo);
		assertTrue(((Echo)response).getMessage().equals(request.getMessage()));
	}	
	@Test
	public void testPeekQueue_NoMessage() throws IOException {		
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to someone else on new queue */
		request = new SendMessageToReceiver(2,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Try to get message*/
		request = new PeekQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Empty);
	}
	@Test
	public void testPeekQueue_ResponseMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		int receiverID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to self on new queue */
		request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Try to get message*/
		request = new PeekQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}	

	@Test
	public void testPeekQueue_ErrorQueueEmpty() throws IOException {	
		int clientID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
				
		/*Try to pop a message*/
		request = new PeekQueue(queueID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_QUEUE_EMPTY);
	}
	@Test
	public void testPeekQueue_ErrorNoSuchQueue() throws IOException {	
		int clientID = 1;
		int receiverID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
			
		/*Try to pop a message*/
		request = new PeekQueue(queueID+1,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testPeekQueueWithSender_ResponseMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		int receiverID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to self on new queue */
		request = new SendMessageToReceiver(receiverID,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Pop the new message*/
		request = new PeekQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}
	@Test
	public void testPeekQueueWithSender_ErrorQueueEmpty() throws IOException {	
		int clientID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
				
		/*Try to pop a message*/
		request = new PeekQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_QUEUE_EMPTY);
	}
	@Test
	public void testPeekQueueWithSender_NoMessage() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to someone else on new queue */
		request = new SendMessageToReceiver(2,queueID,clientID,message);
		Response response2 = client.Send(request);	
		
		/*Try to pop a message*/
		request = new PeekQueueWithSender(queueID,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Empty);
	}
	@Test
	public void testPeekQueueWithSender_ErrorNoSuchQueue() throws IOException {	
		int clientID = 1;
		int receiverID = 1;
			
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
			
		/*Try to pop a message*/
		request = new PeekQueueWithSender(queueID+1,clientID,clientID);
		Response response3 = client.Send(request);
		
		assertTrue(response3 instanceof Error);
		assertTrue(((Error)response3).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testCreateQueue() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		
		Client client = new Client(clientID, 100, host, port);
		Request request = new CreateQueue(clientID);
		Response response = client.Send(request);
		assertTrue(response instanceof QueueCreated);
	}
	@Test
	public void testCreateQueue_AuthenticationError() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(101, 200);
		
		Client client = new Client(clientID, 100, host, port);
		Request request = new CreateQueue(clientID);
		Response response = client.Send(request);
		assertTrue(response instanceof Error);
		assertTrue(((Error)response).errorCode == Error.ERROR_AUTHENTICATION);
	}
	@Test
	public void testDeleteQueue() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		
		/* Create queue*/
		Client client = new Client(clientID, 100, host, port);
		Request request = new CreateQueue(clientID);
		Response response = client.Send(request);
		int queueID = ((QueueCreated)response).queueID;
		
		/* Delete queue*/
		request = new DeleteQueue(queueID,clientID);
		Response response2 = client.Send(request);
		assertTrue(response2 instanceof Ack);
		assertTrue(((Ack)response2).ackCode == Ack.QUEUE_DELETED);
	}
	@Test
	public void testDeleteQueue_NoSuchQueue() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(1, 100);
		
		/* Create queue*/
		Client client = new Client(clientID, 100, host, port);
		Request request = new CreateQueue(clientID);
		Response response = client.Send(request);
		int queueID = ((QueueCreated)response).queueID;
		
		request = new DeleteQueue(queueID+1,clientID);
		Response response2 = client.Send(request);
		assertTrue(response2 instanceof Error);
		assertTrue(((Error)response2).errorCode == Error.ERROR_NO_SUCH_QUEUE);
	}
	@Test
	public void testDeleteQueue_ErrorAuthentication() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = dataGen.getRandomInt(101, 200);
		int queueID = dataGen.getRandomInt(101, 200);
		Client client = new Client(clientID, 100, host, port);
		
		Request request = new DeleteQueue(queueID,clientID);
		Response response2 = client.Send(request);
		assertTrue(response2 instanceof Error);
		assertTrue(((Error)response2).errorCode == Error.ERROR_AUTHENTICATION);
	}
	@Test
	public void testPeekQueue_ResponseMessageNoReceiver() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to any on new queue */
		request = new SendMessageToAny(message,clientID,queueID);
		Response response2 = client.Send(request);	
		
		/*Try to get message*/
		request = new PeekQueue(queueID,clientID);
		Response response3 = client.Send(request);

		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}
	@Test
	public void testPopQueue_ResponseMessageNoReceiver() throws IOException {	
		DataGenerator dataGen = new DataGenerator();
		int clientID = 1;
		
		String message = dataGen.getRandomString(2000);		
		Client client = new Client(clientID, 100, host, port);
		
		/*Create new queue and receive new queueID */
		Request request = new CreateQueue(clientID);
		Response response1 = client.Send(request);
		int queueID = ((QueueCreated)response1).queueID;
		
		/*Send a message to any on new queue */
		request = new SendMessageToAny(message,clientID,queueID);
		Response response2 = client.Send(request);	
		
		/*Try to get message*/
		request = new PopQueue(queueID,clientID);
		Response response3 = client.Send(request);

		assertTrue(response3 instanceof ResponseMessage);
		assertTrue(((ResponseMessage)response3).getMessage().equals(message));
		assertTrue(((ResponseMessage)response3).senderID == clientID);
	}
}
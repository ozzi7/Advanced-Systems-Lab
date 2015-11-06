package benchmark;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import dataGenerator.DataGenerator;

import logger.EventLogger;
import messages.PeekQueue;
import messages.PopQueue;
import messages.Request;
import messages.Response;
import messages.SendMessageToAny;
import messages.SendMessageToReceiver;
import middlewareSettings.Settings;

public class MessageGenerator implements Runnable {

	private LinkedBlockingQueue<Request> lbq = null;
	private LinkedBlockingQueue<Response> responseQueue = null;
	private EventLogger errorLogger = null;
	private EventLogger throughputLogger = null;
	private DataGenerator dataGen;
	private int messageGenMethod = 1;
	
	public MessageGenerator(int aMessageGenMethod,LinkedBlockingQueue<Request> lbq, LinkedBlockingQueue<Response> aResponseQueues) {
		this.setBlockingQueue(lbq);
		messageGenMethod = aMessageGenMethod;
		responseQueue = aResponseQueues;
		dataGen = new DataGenerator();
		initLogger();
	}

	@Override
	public void run() {
		switch(messageGenMethod) {
		case 1:
			sendMessageValidReceiver200Char();
			break;
		case 2:
			sendMessageValidReceiver2000Char();
			break;
		case 3:
			sendPeekQueue();
			break;
		case 4:
			sendMixedLoad();
			break;
		}
	}

	public void setBlockingQueue(LinkedBlockingQueue<Request> lbq) {
		this.lbq = lbq;
	}
	public void sendMessageValidReceiver200Char()
	{
		long startTime = 0;
		long responseTime = 0;
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		try {
			/*Fill request queue with initial messages so the queue is never empty*/
			int sentMessages = 0;
			receivedMessages = 0;    
			startTime = System.nanoTime();
			
			while (sentMessages < 100) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						2, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(200));
				lbq.put(request);
				sentMessages++;
			}
			
			/*Reinsert a new request once a reply was received keeping the queues constant size */
			while(!Thread.currentThread().isInterrupted()) {
				if(responseQueue.poll() != null) {					
					receivedMessages++;
					Request request = new SendMessageToReceiver(dataGen.getRandomInt(
							2, 100), dataGen.getRandomInt(1, 100), 1,
							dataGen.getRandomString(200));
					lbq.put(request);
				}
				if(System.nanoTime() - startTime >= 1000000000) {
					throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
					startTime = System.nanoTime();
					prevReceivedMessages = receivedMessages;
				}
			}
		} catch (InterruptedException | IOException e) {
			try {
				errorLogger.log("Terminated by exception");
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
	}
	public void sendMessageValidReceiver2000Char() {
		long responseTime = 0;
		long startTime = 0;
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		try {
			/*Fill request queue with initial messages so the queue is never empty*/
			int sentMessages = 0;
			receivedMessages = 0;    
			startTime = System.nanoTime();
			
			while (sentMessages < 100) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						2, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(2000));
				lbq.put(request);
				sentMessages++;
			}
			
			/*Reinsert a new request once a reply was received keeping the queues constant size */
			while(true) {
				if(responseQueue.poll() != null) {					
					receivedMessages++;
					Request request = new SendMessageToReceiver(dataGen.getRandomInt(
							2, 100), dataGen.getRandomInt(1, 100), 1,
							dataGen.getRandomString(200));
					lbq.put(request);
				}
				if(System.nanoTime() - startTime >= 1000000000) {
					throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
					startTime = System.nanoTime();
					prevReceivedMessages = receivedMessages;
				}
			}
		} catch (InterruptedException | IOException e) {
			try {
				errorLogger.log("Exception");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void sendPeekQueue()
	{
		long responseTime = 0;
		long startTime = 0;
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		try {
			/*Send messages so not all queues are empty */
			int sentMessages = 0;
			receivedMessages = 0;    
			
			while (sentMessages < 50) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						1, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(200));
				lbq.put(request);
				sentMessages++;
			}
			while (sentMessages < 10000) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						1, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(200));
				lbq.put(request);
				sentMessages++;
				responseQueue.take();
			}
			
			/*Wait for requests to be consumed */
			while(lbq.isEmpty() == false) {responseQueue.poll();}
			Thread.sleep(100); 	
			while(responseQueue.poll() != null) {}
			
			/*Fill request queue with initial messages so the queue is never empty*/
			sentMessages = 0;
			receivedMessages = 0;    
			
			while (sentMessages < 100) {
				Request request = new PeekQueue(dataGen.getRandomInt(1, 100),1);
				lbq.put(request);
				sentMessages++;
			}
			startTime = System.nanoTime();
			
			/*Reinsert a new request once a reply was received keeping the queues constant size */
			while(true) {
				if(responseQueue.poll() != null) {				
					receivedMessages++;
					Request request = new PeekQueue(dataGen.getRandomInt(1, 100),1);
					lbq.put(request);
				}
				if(System.nanoTime() - startTime >= 1000000000) {
					throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
					startTime = System.nanoTime();
					prevReceivedMessages = receivedMessages;
				}
			}
		} catch (InterruptedException | IOException e) {
			try {
				errorLogger.log("Exception");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	/**
	 * Creates a random sequence of 40% send, 20% peek and 40% pop requests
	 */
	public void sendMixedLoad()
	{
		long responseTime = 0;
		long startTime = 0;
		int receivedMessages = 0;
		int prevReceivedMessages = 0;
		try {
			/*Send messages so not all queues are empty */
			int sentMessages = 0;
			receivedMessages = 0;    
			
			while (sentMessages < 50) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						1, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(200));
				lbq.put(request);
				sentMessages++;
			}
			while (sentMessages < 10000) {
				Request request = new SendMessageToReceiver(dataGen.getRandomInt(
						1, 100), dataGen.getRandomInt(1, 100), 1,
						dataGen.getRandomString(200));
				lbq.put(request);
				sentMessages++;
				responseQueue.take();
			}
			
			/*Wait for requests to be consumed */
			while(lbq.isEmpty() == false) {responseQueue.poll();}
			Thread.sleep(100); 	
			while(responseQueue.poll() != null) {}
			
			/*Fill request queue with initial messages so the queue is never empty*/
			sentMessages = 0;
			receivedMessages = 0;    
			
			while (sentMessages < 100) {
				Request request = new PeekQueue(dataGen.getRandomInt(1, 100),1);
				lbq.put(request);
				sentMessages++;
			}
			startTime = System.nanoTime();
			
			
			/*Reinsert a new request once a reply was received keeping the queues constant size */
			while(true) {				
				if(responseQueue.poll() != null) {
					receivedMessages++;
					
					int random = dataGen.getRandomInt(1,10);
					
					if(random < 5){
						/*Send a message*/
						Request request = new SendMessageToAny(dataGen.getRandomString(200),1, dataGen.getRandomInt(1, 100));
						lbq.put(request);
					}
					else if(random >= 5 && random <= 8) {
						/*Pop a message*/
						Request request = new PopQueue(dataGen.getRandomInt(1, 100),1);
						lbq.put(request);
					}
					else if(random > 8) {
						/*Peek a message*/
						Request request = new PeekQueue(dataGen.getRandomInt(1, 100),1);
						lbq.put(request);
					}
				}
				if(System.nanoTime() - startTime >= 1000000000) {
					throughputLogger.logNoTimestamp(String.valueOf((receivedMessages -prevReceivedMessages)));
					startTime = System.nanoTime();
					prevReceivedMessages = receivedMessages;
				}
			}

		} catch (InterruptedException | IOException e) {
			try {
				errorLogger.log("Exception");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	private void initLogger() {
		try {
			throughputLogger= new EventLogger("DBPerformance","Throughput_"+Settings.GLOBAL_FILE_NAME);
			errorLogger= new EventLogger("DBPerformance","Error_" +Settings.GLOBAL_FILE_NAME);
			throughputLogger.logging = Settings.THROUGHPUT_LOGGING;
		} catch (IOException e1) {
			System.out.println("Logger initialization error!");
		}
	}
}

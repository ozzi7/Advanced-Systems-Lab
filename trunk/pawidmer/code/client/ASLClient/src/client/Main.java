package client;

import clientSettings.Settings;

public class Main {

	public static void main(String[] argv) {
		
		if(argv.length != 10) {
	        System.out.println("Proper usage: Middleware IP, Middleware port, # clients, clientID, client run time [ms]" +
	        "Enable error logging [1 = true], Enable event logging [1 = true], Enable response logging [1 = true]," +
	        " Enable throughput logging [1 = true], Test type [1,2,3,4,5]");
	        System.exit(0);
	    }
		
		String mwHost = argv[0];
		int mwPort = Integer.parseInt(argv[1]);
		int nofClients = Integer.parseInt(argv[2]); /*Needed to know which clients can receive messages */
		int clientID = Integer.parseInt(argv[3]);
		Settings.RUNTIME = Integer.parseInt(argv[4]);
		Settings.ERROR_LOGGING = (1 == Integer.parseInt(argv[5]));
		Settings.EVENT_LOGGING  = (1 == Integer.parseInt(argv[6]));
		Settings.RESPONSE_LOGGING = (1 == Integer.parseInt(argv[7]));
		Settings.THROUGHPUT_LOGGING  = (1 == Integer.parseInt(argv[8]));
		int testType = Integer.parseInt(argv[9]);
		Thread clientThread = null;
		
		try {
			Thread.sleep(Settings.CLIENT_WAIT_FOR_SERVER_START);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ClientRunnable clientRunnable = new ClientRunnable(clientID, nofClients, mwHost, mwPort,testType);
		clientThread = new Thread(clientRunnable);
		clientThread.start();

		/* Destroy client thread after specified time*/
		try {
			Thread.sleep(Settings.RUNTIME);
			clientThread.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
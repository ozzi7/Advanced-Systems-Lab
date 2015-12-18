package client;

import java.util.ArrayList;

import clientSettings.Settings;

public class Main {

	public static void main(String[] argv) {
		
		if(argv.length != 11) {
	        System.out.println("Proper usage: Middleware IP, Middleware port, # clients, startingClientID, client run time [ms], " +
	        "Enable error logging [1 = true], Enable event logging [1 = true], Enable response logging [1 = true]," +
	        " Enable throughput logging [1 = true], Test type [1,2,3,4,5], thinktime [in ms, 0 = disabled]");
	        System.exit(0);
	    }
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		String mwHost = argv[0];
		int mwPort = Integer.parseInt(argv[1]);
		int nofClients = Integer.parseInt(argv[2]); /*Needed to know which clients can receive messages */
		int startingClientID = Integer.parseInt(argv[3]);
		Settings.RUNTIME = Integer.parseInt(argv[4]);
		Settings.ERROR_LOGGING = (1 == Integer.parseInt(argv[5]));
		Settings.EVENT_LOGGING  = (1 == Integer.parseInt(argv[6]));
		Settings.RESPONSE_LOGGING = (1 == Integer.parseInt(argv[7]));
		Settings.THROUGHPUT_LOGGING  = (1 == Integer.parseInt(argv[8]));
		int testType = Integer.parseInt(argv[9]);
		double thinkTime = Double.parseDouble(argv[10]);
		
		try {
			Thread.sleep(Settings.CLIENT_WAIT_FOR_SERVER_START);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i = startingClientID; i < startingClientID+nofClients; ++i) {
			ClientRunnable clientRunnable = new ClientRunnable(i, nofClients, mwHost, mwPort,testType, thinkTime);
			threads.add(new Thread(clientRunnable));
			threads.get(threads.size()-1).start();
		}

		/* Destroy client threads after specified time*/
		try {
			Thread.sleep(Settings.RUNTIME);
			for(int i = 0; i < threads.size(); ++i) {
				threads.get(i).interrupt();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
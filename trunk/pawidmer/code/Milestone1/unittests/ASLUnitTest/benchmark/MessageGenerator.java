package benchmark;

import java.util.concurrent.LinkedBlockingQueue;

import messages.CreateQueue;
import messages.Request;

public class MessageGenerator implements Runnable {

	private LinkedBlockingQueue<Request> lbq = null;

	public MessageGenerator(LinkedBlockingQueue<Request> lbq) {
		this.setBlockingQueue(lbq);
	}

	@Override
	public void run() {
		try {
			int count = 4;
			while (count < 100) {
				Thread.sleep(10);
				lbq.put(new CreateQueue(0));
				count++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setBlockingQueue(LinkedBlockingQueue<Request> lbq) {
		this.lbq = lbq;
	}
}

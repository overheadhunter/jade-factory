package factory.visualization.unity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.visualization.BlockingVisualizationCallback;
import factory.visualization.Visualizing;

public class UnityVisualizer implements Visualizing {
	
	private final Logger LOG = LoggerFactory.getLogger(UnityVisualizer.class);
	private final Executor executor = Executors.newCachedThreadPool();
	private final List<String> receivedMessages = new Vector<>();
	private final Lock receiveLock = new ReentrantLock();
	private final Condition receiveCondition = receiveLock.newCondition();
	private Socket serverSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	public UnityVisualizer() {
		try {
			serverSocket = new Socket("localhost", 4711);
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			executor.execute(new UnityMessageReader());
			out.println("Rockin");
		} catch (IOException e) {
			throw new IllegalStateException("Start Unity first!", e);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			serverSocket.close();
		} catch(IOException e) {
			// bazinga!
		}
		super.finalize();
	}
	
	@Override
	public void orderArrived(String orderId) {
		out.println(orderId + " arrived");
	}
	
	@Override
	public void orderShipped(String orderId) {
		out.println(orderId + " shipped");
	}

	@Override
	public void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue) {
		// ignore
	}

	@Override
	public void stationStartsWorking(String stationId, String orderId) {
		out.println(stationId + " started working on " + orderId);
	}

	@Override
	public void stationStopsWorking(String stationId, String orderId) {
		out.println(stationId + " stopped working");
	}

	@Override
	public void youBotWillMoveTo(String youBotId, String stationId, String orderId, BlockingVisualizationCallback callbackWhenDone) {
		if (orderId != null) {
			out.println(orderId + " is on " + youBotId);
		}
		out.println(youBotId + " moves to " + stationId);
		executor.execute(new WaitForArrivalThing(youBotId, () -> {
			if (orderId != null) {
				out.println(orderId + " is at " + stationId);
			}
			callbackWhenDone.done();
		}));
	}
	
	private class WaitForArrivalThing implements Runnable {
		
		final String youBotId;
		final Runnable callbackWhenDone;
		
		private WaitForArrivalThing(String youBotId, Runnable callbackWhenDone) {
			this.youBotId = youBotId;
			this.callbackWhenDone = callbackWhenDone;
		}

		@Override
		public void run() {
			receiveLock.lock();
			try {
				while (!hasYouBotArrived()) {
					receiveCondition.await();
				}
				callbackWhenDone.run();
			} catch (InterruptedException e) {
				// bazinga
			} finally {
				receiveLock.unlock();
			}
		}
		
		private boolean hasYouBotArrived() {
			for (Iterator<String> iterator = receivedMessages.iterator(); iterator.hasNext();) {
				final String message = iterator.next();
				if (message.startsWith(youBotId + " arrived")) {
					iterator.remove();
					return true;
				}
			}
			return false;
		}
		
	}
	
	private class UnityMessageReader implements Runnable {

		@Override
		public void run() {
			try {
				String message;
				while ((message = in.readLine()) != null) {
					messageArrived(message);
				}
			} catch (IOException e) {
				LOG.error("Communication error.", e);
			}
		}
		
		private void messageArrived(String message) {
			receiveLock.lock();
			try {
				receivedMessages.add(message);
				receiveCondition.signalAll();
			} finally {
				receiveLock.unlock();
			}
		}
		
	}

}

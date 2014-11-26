package factory.visualization;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BlockingVisualizationCallback {
	
	private final Condition condition;
	private final AtomicBoolean done;
	private final Lock lock;
	
	BlockingVisualizationCallback(Lock lock) {
		this.lock = lock;
		this.condition = lock.newCondition();
		this.done = new AtomicBoolean(false);
	}
	
	public void done() {
		synchronized (lock) {
			done.set(true);
			condition.signal();
		}
	}
	
	public void waitUntilDone() throws InterruptedException {
		while (!done.get()) {
			synchronized (lock) {
				condition.await();
			}
		}
	}

}

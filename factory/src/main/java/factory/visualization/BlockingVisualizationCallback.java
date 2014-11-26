package factory.visualization;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingVisualizationCallback {
	
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final AtomicBoolean done = new AtomicBoolean(false);
	
	public void done() {
		lock.lock();
		try {
			done.set(true);
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public void waitUntilDone() throws InterruptedException {
		lock.lock();
		try {
			while (!done.get()) {
				condition.await();
			}
		} finally {
			lock.unlock();
		}
	}

}

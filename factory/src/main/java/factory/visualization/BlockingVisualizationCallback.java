package factory.visualization;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BlockingVisualizationCallback {
	
	private final Condition condition;
	private final AtomicBoolean done;
	
	BlockingVisualizationCallback(Lock lock) {
		this.condition = lock.newCondition();
		this.done = new AtomicBoolean(false);
	}
	
	public void done() {
		done.set(true);
		condition.signal();
	}
	
	public void waitUntilDone() throws InterruptedException {
		while (!done.get()) {
			condition.wait();
		}
	}

}

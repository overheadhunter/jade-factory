package factory.order;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Order implements Serializable {
	
	private static final long serialVersionUID = -3417265163977595139L;
	private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	private final int orderNumber;
	
	public Order() {
		this.orderNumber = COUNTER.getAndIncrement();
	}
	
	@Override
	public String toString() {
		return String.format("[Order %d]", orderNumber);
	}

}

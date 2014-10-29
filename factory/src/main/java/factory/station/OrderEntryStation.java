package factory.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.behaviours.CyclicBehaviour;
import factory.order.Order;


public class OrderEntryStation extends AbstractStation {
	
	private static final long serialVersionUID = -4667270809378640277L;
	private static final Logger LOG = LoggerFactory.getLogger(OrderEntryStation.class);
	
	@Override
	protected void setup() {
		super.setup();
		
		this.addBehaviour(new OrderCreatingBehaviour());
	}

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.ORDER_CREATING;
	}

	@Override
	protected String getStationName() {
		return getAID().getName();
	}
	
	/**
	 * Assembles the next order in queue.
	 */
	private class OrderCreatingBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;

		@Override
		public void action() {
			try {
				final Order order = new Order();
				putFinishedOrder(order);
				LOG.info("New order " + order);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOG.error("Assembly failed.", e);
			}
		}
		
	}

}

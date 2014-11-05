package factory.station;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.Order;


public class OrderEntryStation extends AbstractStation {
	
	private static final long serialVersionUID = -4667270809378640277L;
	private static final Logger LOG = LoggerFactory.getLogger(OrderEntryStation.class);
	
	@Override
	protected void setup() {
		super.setup();
		
		this.addBehaviour(new OrderCreatingBehaviour(this, 7500));
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
	private class OrderCreatingBehaviour extends TickerBehaviour {
		
		public OrderCreatingBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		private static final long serialVersionUID = 4362396144651504823L;

		@Override
		public void onTick() {
			try {
				final Order order = new Order();
				putFinishedOrder(order);
				LOG.info("New order " + order);
			} catch (InterruptedException e) {
				LOG.error("Failed to enqueue new order.", e);
			}
		}
		
	}

}

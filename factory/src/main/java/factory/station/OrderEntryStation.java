package factory.station;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

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
		return getAID().getLocalName();
	}
	
	/**
	 * Assembles the next order in queue.
	 */
	private class OrderCreatingBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;
		
		public OrderCreatingBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		public void onTick() {
			try {
				final Order order = new Order();
				final AgentController ac = getContainerController().acceptNewAgent(order.toString(), order);
				ac.start();
				putFinishedOrder(order);
				LOG.info("New order " + order);
			} catch (InterruptedException e) {
				LOG.error("Failed to enqueue new order.", e);
			} catch (StaleProxyException e) {
				LOG.error("Failed to create new order.", e);
			}
		}
		
	}

}

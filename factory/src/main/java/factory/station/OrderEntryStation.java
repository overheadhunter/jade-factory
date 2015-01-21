package factory.station;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.Order;
import factory.order.OrderAgent;
import factory.visualization.VisualizationAdapter;


public class OrderEntryStation extends AbstractStation {
	
	private static final long serialVersionUID = -4667270809378640277L;
	private static final Logger LOG = LoggerFactory.getLogger(OrderEntryStation.class);
	private static final AtomicInteger ORDER_COUNTER = new AtomicInteger(1);
	
	@Override
	protected void setup() {
		super.setup();
		
		this.addBehaviour(new OrderCreatingBehaviour(this, 5000));
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
	 * Adds new orders to the output queue.
	 */
	private class OrderCreatingBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;
		
		
		public OrderCreatingBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		public void onTick() {
			try {
				final String orderName = "Order_" + ORDER_COUNTER.getAndIncrement();
				final Object[] args = {getAID()};
				final AgentController ac = getContainerController().createNewAgent(orderName, OrderAgent.class.getName(), args);
				ac.start();
				final Order order = ac.getO2AInterface(Order.class);
				outQueue.put(order.getAID());
				VisualizationAdapter.visualizeOrderArrival(order.getAID().getLocalName());
				VisualizationAdapter.visualizeStationQueueChange(OrderEntryStation.this.getLocalName(), inQueue.size(), outQueue.size());
			} catch (InterruptedException e) {
				LOG.error("Failed to enqueue new order.", e);
			} catch (StaleProxyException e) {
				LOG.error("Failed to create new order.", e);
			}
		}
		
	}

}

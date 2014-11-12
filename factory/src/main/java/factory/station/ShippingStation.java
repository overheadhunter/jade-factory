package factory.station;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.Order;

public class ShippingStation extends AbstractStation {

	private static final long serialVersionUID = -8376989242142226380L;
	private static final Logger LOG = LoggerFactory.getLogger(ShippingStation.class);

	@Override
	protected void setup() {
		super.setup();

		this.addBehaviour(new ShippingBehaviour(this, 1000));
	}

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.SHIPPING;
	}

	@Override
	protected String getStationName() {
		return getAID().getLocalName();
	}

	/**
	 * Ships the next order in queue.
	 */
	private class ShippingBehaviour extends TickerBehaviour {

		private static final long serialVersionUID = 4362396144651504823L;

		public ShippingBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		public void onTick() {
			final Order order = inQueue.poll();
			if (order != null) {
				LOG.info("Shipping order " + order);
			}
		}

	}

}

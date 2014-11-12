package factory.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import factory.order.Order;

abstract class AbstractAssemblyStation extends AbstractStation {
	
	private static final long serialVersionUID = -2934606720355101360L;
	private static final ThreadedBehaviourFactory THREADED_BEHAVIOUR_FACTORY = new ThreadedBehaviourFactory();
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAssemblyStation.class);
	
	@Override
	protected void setup() {
		super.setup();
		
		// add behaviours
		this.addBehaviour(THREADED_BEHAVIOUR_FACTORY.wrap(new AssemblyBehaviour()));
	}
	
	protected abstract void assemble(Order order) throws InterruptedException;
	
	/**
	 * Assembles the next order in queue.
	 */
	private class AssemblyBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;

		@Override
		public void action() {
			try {
				final Order order = takeNextOrder();
				assemble(order);
				putFinishedOrder(order);
			} catch (InterruptedException e) {
				LOG.error("Assembly failed.", e);
			}
		}
		
	}

}

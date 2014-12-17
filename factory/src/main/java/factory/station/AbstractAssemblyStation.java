package factory.station;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ControllerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.Order;
import factory.visualization.VisualizationAdapter;

abstract class AbstractAssemblyStation extends AbstractStation {
	
	private static final long serialVersionUID = -2934606720355101360L;
	private static final ThreadedBehaviourFactory THREADED_BEHAVIOUR_FACTORY = new ThreadedBehaviourFactory();
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAssemblyStation.class);
	
	@Override
	protected void setup() {
		super.setup();
		
		// add behaviours
		this.addBehaviour(new CheckInQueueBehaviour(this, 2000));
	}
	
	protected void assemble(Order order) throws InterruptedException {
		VisualizationAdapter.visualizeStationStartsWorking(this.getLocalName(), order.getAID().getLocalName());
		Thread.sleep(5000);
		order.assemble(getServiceType());
		VisualizationAdapter.visualizeStationStopsWorking(this.getLocalName(), order.getAID().getLocalName());
	}
	
	/**
	 * Check length of in-queue.
	 */
	private class CheckInQueueBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = -7036548104596688712L;

		public CheckInQueueBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			if (inQueue.peek() != null) {
				myAgent.addBehaviour(THREADED_BEHAVIOUR_FACTORY.wrap(new AssembleBehaviour()));
			}
		}
		
	}
	
	/**
	 * Assembles the next order.
	 */
	private class AssembleBehaviour extends OneShotBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;

		@Override
		public void action() {
			try {
				final AID orderAid = inQueue.take();
				VisualizationAdapter.visualizeStationQueueChange(AbstractAssemblyStation.this.getLocalName(), inQueue.size(), outQueue.size());
				
				final Order order = getContainerController().getAgent(orderAid.getName(), true).getO2AInterface(Order.class);
				assemble(order);

				outQueue.put(orderAid);
				VisualizationAdapter.visualizeStationQueueChange(AbstractAssemblyStation.this.getLocalName(), inQueue.size(), outQueue.size());
			} catch (InterruptedException | ControllerException e) {
				LOG.error("Assembly failed.", e);
			}
		}
		
	}

}

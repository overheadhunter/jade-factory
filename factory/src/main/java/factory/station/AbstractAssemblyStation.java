package factory.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import factory.common.Constants;
import factory.order.Order;

abstract class AbstractAssemblyStation extends AbstractStation {
	
	private static final long serialVersionUID = -2934606720355101360L;
	private static final ThreadedBehaviourFactory THREADED_BEHAVIOUR_FACTORY = new ThreadedBehaviourFactory();
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAssemblyStation.class);
	
	@Override
	protected void setup() {
		super.setup();
		
		// add behaviours
		this.addBehaviour(THREADED_BEHAVIOUR_FACTORY.wrap(new AssembleBehaviour(this)));
	}
	
	protected void assemble(Order order) throws InterruptedException {
		Thread.sleep(500);
	}
	
	/**
	 * Assembles the next order in queue.
	 */
	private class AssembleBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 4362396144651504823L;
		
		public AssembleBehaviour(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			try {
				final Order order = inQueue.take();
				
				final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setConversationId(Constants.CONV_ID_ASSEMBLE);
				msg.addReceiver(order.getAID());
				msg.setContent(getServiceType().name());
				myAgent.send(msg);
				
				assemble(order);

				outQueue.put(order);
			} catch (InterruptedException e) {
				LOG.error("Assembly failed.", e);
			}
		}
		
	}

}

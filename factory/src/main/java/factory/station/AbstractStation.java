package factory.station;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.order.Order;

abstract class AbstractStation extends Agent {
	
	private static final long serialVersionUID = -504573009972336872L;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractStation.class);
	
	private final BlockingQueue<Order> inQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<Order> outQueue = new LinkedBlockingQueue<>();

	@Override
	protected void setup() {
		super.setup();
		
		// register service:
		final DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		final ServiceDescription sd = new ServiceDescription();
		sd.setType(getServiceType().name());
		sd.setName(getStationName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			LOG.info("Registered station " + getStationName());
		} catch (FIPAException fe) {
			LOG.error("Error during agent registration.", fe);
		}
		
		// add behaviours:
		this.addBehaviour(new PickupOfferingBehaviour());
		this.addBehaviour(new EnqueueingBehaviour());
	}
	
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			LOG.error("Error during agent deregistration.", fe);
		}
		super.takeDown();
	}
	
	protected final Order takeNextOrder() throws InterruptedException {
		return inQueue.take();
	}
	
	protected final void putFinishedOrder(Order order) throws InterruptedException {
		outQueue.put(order);
	}
	
	protected abstract ServiceType getServiceType();
	
	protected abstract String getStationName();
	
	/**
	 * Responds to pickup CFPs from youBots, if outgoing queue is not empty.
	 */
	private class PickupOfferingBehaviour extends CyclicBehaviour implements Constants {
		
		private static final long serialVersionUID = -5699647452534534818L;

		@Override
		public void action() {
			final MessageTemplate mtMatchingConversationId = MessageTemplate.MatchConversationId(CONV_ID_PICKUP);
			final ACLMessage request = myAgent.receive(mtMatchingConversationId);
			if (request != null && request.getPerformative() == ACLMessage.CFP) {
				this.makeProposal(request);
			} else if (request != null && request.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				this.handOverItem(request);
			}
		}
		
		private void makeProposal(ACLMessage request) {
			final ACLMessage reply = request.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent(Integer.toString(outQueue.size()));
			myAgent.send(reply);
		}
		
		private void handOverItem(ACLMessage request) {
			try {
				final ACLMessage reply = request.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContentObject(outQueue.take());
				myAgent.send(reply);
			} catch (IOException | InterruptedException e) {
				LOG.error("Failed to hand over order.", e);
				final ACLMessage reply = request.createReply();
				reply.setPerformative(ACLMessage.FAILURE);
				myAgent.send(reply);
			}
		}
	}
	
	/**
	 * Accepts one order from a youBot.
	 */
	private class EnqueueingBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = -6855574067206356572L;

		@Override
		public void action() {
			final MessageTemplate mt = MessageTemplate.MatchConversationId("enqueue");
			final ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				final Order order = unwrapPayload(msg);
				final ACLMessage reply = msg.createReply();
				if (order != null && inQueue.offer(order)) {
					reply.setPerformative(ACLMessage.INFORM);
					LOG.info("Enqueued order " + order + " in station " + getStationName());
				} else {
					reply.setPerformative(ACLMessage.FAILURE);
					LOG.warn("Failed to enqueue order " + order + " in station " + getStationName());
				}
				myAgent.send(reply);
			}
		}
		
		private Order unwrapPayload(ACLMessage msg) {
			try {
				final Serializable payload = msg.getContentObject();
				if (payload instanceof Order) {
					return (Order) payload;
				}
			} catch (UnreadableException e) {
				LOG.error("Assembly failed.", e);
			}
			return null;
		}
		
	}

}

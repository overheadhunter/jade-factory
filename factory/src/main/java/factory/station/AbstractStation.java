package factory.station;

import jade.core.AID;
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
		this.addBehaviour(new DequeueingBehaviour());
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
	 * Informs the youBot about finished orders.
	 */
	private class DequeueingBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = -5699647452534534818L;

		@Override
		public void action() {
			try {
				final DFAgentDescription[] transportAgents = queryTransportAgents();
				
				final ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.setContentObject(outQueue.take());
				cfp.setConversationId("transportation-todo-externalize-conversation-ids");
				cfp.setReplyWith("cfp" + System.currentTimeMillis());
				for (DFAgentDescription dfAgentDescription : transportAgents) {
					cfp.addReceiver(dfAgentDescription.getName());
				}
				myAgent.send(cfp);
				
				final MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(cfp.getConversationId()), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				ACLMessage reply = myAgent.receive(mt);
				
				// TODO ...
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private DFAgentDescription[] queryTransportAgents() throws FIPAException {
			final DFAgentDescription template = new DFAgentDescription();
			final ServiceDescription sd = new ServiceDescription();
			sd.setType("transportation-todo-type");
			template.addServices(sd);
			return DFService.search(myAgent, template);
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

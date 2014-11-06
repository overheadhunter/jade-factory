package factory.station;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.common.MessageUtil;
import factory.common.ResponseCreationException;
import factory.order.Order;
import factory.station.ProposingBehaviour.Proposing;

abstract class AbstractStation extends Agent implements Proposing {

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
			LOG.info("Registered station " + getStationName() + " of type " + getServiceType().name());
		} catch (FIPAException fe) {
			LOG.error("Error during agent registration.", fe);
		}

		// add behaviours:
		this.addBehaviour(new ProposingBehaviour(Constants.CONV_ID_PICKUP, this));
		this.addBehaviour(new EnqueueingBehaviour());
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
			LOG.info("Deregistered station " + getStationName());
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

	@Override
	public ACLMessage createResponseForCfp(String conversationId, ACLMessage request) {
		if (Constants.CONV_ID_PICKUP.equals(conversationId) && !outQueue.isEmpty()) {
			final ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.PROPOSE);
			response.setContent(Integer.toString(outQueue.size()));
			return response;
		} else {
			return null;
		}
	}

	@Override
	public ACLMessage createResponseForProposal(String conversationId, ACLMessage request) throws ResponseCreationException {
		if (Constants.CONV_ID_PICKUP.equals(conversationId)) {
			try {
				final Order order = outQueue.poll();
				LOG.info(getStationName() + " hands over item " + order);
				final ACLMessage response = request.createReply();
				response.setPerformative(ACLMessage.INFORM);
				response.setContentObject(order);
				return response;
			} catch (IOException e) {
				throw new ResponseCreationException("Failed to hand over order.", e);
			}
		} else {
			return null;
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
				final Order order = MessageUtil.unwrapPayload(msg);
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

	}

}

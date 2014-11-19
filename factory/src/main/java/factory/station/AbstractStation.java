package factory.station;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.common.MessageUtil;
import factory.common.ResponseCreationException;
import factory.station.ProposingBehaviour.Proposing;
import factory.visualization.VisualizationAdapter;

abstract class AbstractStation extends Agent implements Proposing {

	private static final long serialVersionUID = -504573009972336872L;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractStation.class);

	protected final BlockingQueue<AID> inQueue = new LinkedBlockingQueue<>();
	protected final BlockingQueue<AID> outQueue = new LinkedBlockingQueue<>();

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
		this.addBehaviour(new ProposingBehaviour(Constants.CONV_ID_DROPOFF, this));
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

	protected abstract ServiceType getServiceType();

	protected abstract String getStationName();

	@Override
	public ACLMessage createResponseForCfp(String conversationId, ACLMessage request) {
		LOG.trace("{} received CFP", getStationName());
		if (Constants.CONV_ID_PICKUP.equals(conversationId) && !outQueue.isEmpty()) {
			final ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.PROPOSE);
			response.setContent(Integer.toString(outQueue.size()));
			return response;
		} else if(Constants.CONV_ID_DROPOFF.equals(conversationId)) {
			final ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.PROPOSE);
			response.setContent(Integer.toString(inQueue.size()));
			return response;
		} else {
			return null;
		}
	}

	@Override
	public ACLMessage createResponseForAcceptedProposal(String conversationId, ACLMessage request) throws ResponseCreationException {
		if (Constants.CONV_ID_PICKUP.equals(conversationId)) {
			try {
				final AID order = outQueue.poll();
				VisualizationAdapter.visualizeStationQueueChange(this.getLocalName(), inQueue.size(), outQueue.size());
				final ACLMessage response = request.createReply();
				response.setPerformative(ACLMessage.INFORM);
				response.setContentObject(order);
				return response;
			} catch (IOException e) {
				throw new ResponseCreationException("Failed to hand over order.", e);
			}
		} else if (Constants.CONV_ID_DROPOFF.equals(conversationId)) {
			try {
				final AID order = MessageUtil.unwrapPayload(request, AID.class);
				inQueue.put(order);
				VisualizationAdapter.visualizeStationQueueChange(this.getLocalName(), inQueue.size(), outQueue.size());
			} catch (InterruptedException e) {
				throw new ResponseCreationException("Failed to receive order.", e);
			}
			// we don't need to respond...
			return null;
		} else {
			return null;
		}
	}

}

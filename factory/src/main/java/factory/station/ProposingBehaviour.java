package factory.station;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.ResponseCreationException;

public class ProposingBehaviour extends CyclicBehaviour {
	
	private static final long serialVersionUID = 2201978103845444321L;
	private static final Logger LOG = LoggerFactory.getLogger(ProposingBehaviour.class);
	
	private final String conversationId;
	private final Proposing proposing;
	
	public <T extends Agent & Proposing> ProposingBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.proposing = agent;
	}

	@Override
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchConversationId(conversationId);
		final ACLMessage request = myAgent.receive(mt);
		if (request == null) {
			block();
		} else {
			switch (request.getPerformative()) {
			case ACLMessage.CFP:
				respondToCfp(request);
				return;
			case ACLMessage.ACCEPT_PROPOSAL:
				respondToAcceptProposal(request);
				return;
			default:
				return;
			}
		}
	}
	
	private void respondToCfp(ACLMessage request) {
		try {
			ACLMessage response = proposing.createResponseForCfp(conversationId, request);
			if (response != null) {
				myAgent.send(response);
			}
		} catch (ResponseCreationException e) {
			LOG.warn(e.getMessage(), e);
			ACLMessage response = e.createFailureResponse(request);
			myAgent.send(response);
		}
	}
	
	private void respondToAcceptProposal(ACLMessage request) {
		try {
			ACLMessage response = proposing.createResponseForProposal(conversationId, request);
			if (response != null) {
				myAgent.send(response);
			}
		} catch (ResponseCreationException e) {
			LOG.warn(e.getMessage(), e);
			ACLMessage response = e.createFailureResponse(request);
			myAgent.send(response);
		}
	}
	
	public interface Proposing {
		
		/**
		 * @return response object or <code>null</code>, if not responding to request.
		 */
		ACLMessage createResponseForCfp(String conversationId, ACLMessage request) throws ResponseCreationException;
		
		/**
		 * @return response object or <code>null</code>, if not responding to request.
		 */
		ACLMessage createResponseForProposal(String conversationId, ACLMessage request) throws ResponseCreationException;
		
	}

}

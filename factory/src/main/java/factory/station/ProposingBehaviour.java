package factory.station;

import java.util.concurrent.atomic.AtomicBoolean;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.ResponseCreationException;

/**
 * Cycling behaviour listening to CFPs and creating responses.
 */
public class ProposingBehaviour extends CyclicBehaviour {
	
	private static final long serialVersionUID = 2201978103845444321L;
	private static final Logger LOG = LoggerFactory.getLogger(ProposingBehaviour.class);
	
	private final String conversationId;
	private final Proposing proposing;
	private final AtomicBoolean negotiating;
	
	public <T extends Agent & Proposing> ProposingBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.proposing = agent;
		this.negotiating = new AtomicBoolean();
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
				if(negotiating.compareAndSet(false, true)) {
					respondToCfp(request);
				}
				return;
			case ACLMessage.ACCEPT_PROPOSAL:
				respondToAcceptProposal(request);
				negotiating.lazySet(false);
				return;
			case ACLMessage.REJECT_PROPOSAL:
				negotiating.lazySet(false);
				return;
			default:
				return;
			}
		}
	}
	
	private void respondToCfp(ACLMessage request) {
		try {
			final ACLMessage response = proposing.createResponseForCfp(conversationId, request);
			if (response != null) {
				myAgent.send(response);
			} else {
				negotiating.set(false);
			}
		} catch (ResponseCreationException e) {
			LOG.warn(e.getMessage(), e);
			ACLMessage response = e.createFailureResponse(request);
			myAgent.send(response);
		}
	}
	
	private void respondToAcceptProposal(ACLMessage request) {
		try {
			ACLMessage response = proposing.createResponseForAcceptedProposal(conversationId, request);
			if (response != null) {
				myAgent.send(response);
			}
		} catch (ResponseCreationException e) {
			LOG.warn(e.getMessage(), e);
			ACLMessage response = e.createFailureResponse(request);
			myAgent.send(response);
		}
	}
	
	/**
	 * Delegate interface for all decisions to be made by the agent responding to a CFP.
	 */
	public interface Proposing {
		
		/**
		 * @return response object or <code>null</code>, if not responding to request.
		 */
		ACLMessage createResponseForCfp(String conversationId, ACLMessage request) throws ResponseCreationException;
		
		/**
		 * @return response object or <code>null</code>, if not responding to request.
		 */
		ACLMessage createResponseForAcceptedProposal(String conversationId, ACLMessage request) throws ResponseCreationException;
		
	}

}

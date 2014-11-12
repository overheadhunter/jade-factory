package factory.youbot;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.ResponseCreationException;

/**
 * One shot CFP.<br/>
 * Calls for proposals and waits until a given deadline is reached, then accepts the best proposal (if any).
 * 
 * @see CallingForProposal
 */
public class CallingForProposalBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = -8705478409838715143L;
	private static final Logger LOG = LoggerFactory.getLogger(CallingForProposalBehaviour.class);
	
	private final String conversationId;
	private final String messageId;
	private final CallingForProposal caller;
	
	public <T extends Agent & CallingForProposal> CallingForProposalBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.messageId = UUID.randomUUID().toString();
		this.caller = agent;
	}

	@Override
	public void action() {
		try {
			final Date proposalDeadline = caller.getProposalDeadline(conversationId);
			final Collection<DFAgentDescription> stationAgents = getCfpRecipients();
			final ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.setConversationId(conversationId);
			cfp.setReplyWith(messageId);
			cfp.setReplyByDate(proposalDeadline);
			for (final DFAgentDescription agent : stationAgents) {
				cfp.addReceiver(agent.getName());
			}
			caller.configureCfp(conversationId, cfp);
			myAgent.send(cfp);
			myAgent.addBehaviour(new CollectProposalsBehaviour(proposalDeadline));
		} catch (FIPAException e) {
			LOG.error("Unable to contact stations.", e);
		}
	}
	
	private Collection<DFAgentDescription> getCfpRecipients() throws FIPAException {
		final Collection<DFAgentDescription> result = new HashSet<>();
		for (final String serviceType : caller.servicesToCall(conversationId)) {
			final DFAgentDescription template = new DFAgentDescription();
			final ServiceDescription sd = new ServiceDescription();
			sd.setType(serviceType);
			template.addServices(sd);
			result.addAll(Arrays.asList(DFService.search(myAgent, template)));
		}
		return result;
	}
	
	private void acceptProposal(ACLMessage request) {
		try {
			final ACLMessage response = caller.createAcceptResponseForProposal(conversationId, request);
			if (response != null) {
				response.setReplyWith(messageId);
				myAgent.send(response);
				myAgent.addBehaviour(new ReceiveResponseToAcceptedProposalBehaviour());
			}
		} catch (ResponseCreationException e) {
			LOG.warn(e.getMessage(), e);
			final ACLMessage response = e.createFailureResponse(request);
			myAgent.send(response);
		}
	}
	
	private void rejectProposals(Collection<ACLMessage> proposals) {
		for (final ACLMessage proposal : proposals) {
			final ACLMessage response = proposal.createReply();
			response.setConversationId(conversationId);
			response.setPerformative(ACLMessage.REJECT_PROPOSAL);
			myAgent.send(response);
		}
	}
	
	/**
	 * Evaluates all proposals received before a given deadline.
	 */
	private class CollectProposalsBehaviour extends WakerBehaviour {

		private static final long serialVersionUID = 1474879150796261851L;
		
		public CollectProposalsBehaviour(Date wakeupDate) {
			super(CallingForProposalBehaviour.this.myAgent, wakeupDate);
		}
		
		@Override
		protected void onWake() {
			final MessageTemplate mtMatchingMsgId = MessageTemplate.MatchInReplyTo(messageId);
			final MessageTemplate mtMatchingPerformative = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			final MessageTemplate mt = MessageTemplate.and(mtMatchingMsgId, mtMatchingPerformative);
			final Collection<ACLMessage> proposals = new HashSet<>();
			ACLMessage proposal;
			while ((proposal = myAgent.receive(mt)) != null) {
				proposals.add(proposal);
			}
			final ACLMessage bestProposal = caller.chooseProposal(conversationId, proposals);
			if (bestProposal != null) {
				acceptProposal(bestProposal);
				final Collection<ACLMessage> others = proposals.stream().filter(p -> !p.equals(bestProposal)).collect(Collectors.toList());
				rejectProposals(others);
			}
		}
		
	}
	
	/**
	 * If a proposal is accepted, wait for a last statement of the winner.
	 */
	private class ReceiveResponseToAcceptedProposalBehaviour extends OneShotBehaviour {
		
		private static final long serialVersionUID = -6189118814234620118L;
		
		@Override
		public void action() {
			final MessageTemplate mt = MessageTemplate.MatchInReplyTo(messageId);
			final long timeout = caller.waitForResponseForAcceptedProposal(conversationId);
			final ACLMessage response;
			if (timeout > 0) {
				response = myAgent.blockingReceive(mt, timeout);
			} else {
				response = myAgent.receive(mt);
			}
			if (response != null) {
				caller.didReceiveResponseForAcceptedProposal(conversationId, response);
			}
		}
		
	}
	
	/**
	 * Delegate interface for all decisions to be made by the agent, who creates a CFP.
	 */
	public interface CallingForProposal {
		
		/**
		 * @return Collection of service type names
		 */
		Collection<String> servicesToCall(String conversationId);
		
		/**
		 * @return Latest date by when proposals must be submitted
		 */
		Date getProposalDeadline(String conversationId);
		
		/**
		 * @param cfp to be configured before it gets sent.
		 */
		void configureCfp(String conversationId, final ACLMessage cfp);
		
		/**
		 * @return The best proposal
		 */
		ACLMessage chooseProposal(String conversationId, Collection<ACLMessage> proposals);
		
		/**
		 * @param bestProposal chosen by {@link #chooseProposal(Collection)}
		 * @return response object or <code>null</code>, if not responding to proposal.
		 */
		ACLMessage createAcceptResponseForProposal(String conversationId, ACLMessage bestProposal) throws ResponseCreationException;
		
		/**
		 * @return positive number of milliseconds to wait for a response or <code>-1</code> if no response is needed.
		 */
		long waitForResponseForAcceptedProposal(String conversationId);
		
		/**
		 * Informs the caller about the incoming response.
		 */
		void didReceiveResponseForAcceptedProposal(String conversationId, ACLMessage response);
		
	}

}

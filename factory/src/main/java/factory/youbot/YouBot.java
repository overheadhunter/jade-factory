package factory.youbot;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.common.MessageUtil;
import factory.common.ResponseCreationException;
import factory.order.Order;
import factory.station.ServiceType;
import factory.youbot.CallingForProposalBehaviour.CallingForProposal;

public class YouBot extends Agent implements Constants, CallingForProposal {

	private static final long serialVersionUID = 5093362251079611218L;
	private static final Logger LOG = LoggerFactory.getLogger(YouBot.class);
	
	private Order currentPayload = null;

	@Override
	protected void setup() {
		LOG.info("Registered YouBot " + getAID().getName());
		addBehaviour(new AskForWorkBehaviour(this, 5000));
	}
	
	@Override
	public Collection<String> servicesToCall(String conversationId) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			return EnumSet.allOf(ServiceType.class).stream().map(st -> st.name()).collect(Collectors.toSet());
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Date getProposalDeadline(String conversationId) {
		final Calendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, 1);
		return cal.getTime();
	}
	
	@Override
	public void configureCfp(String conversationId, ACLMessage cfp) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			cfp.setContent("I need something to do...");
		}
	}
	
	@Override
	public ACLMessage chooseProposal(String conversationId, Collection<ACLMessage> proposals) {
		LOG.debug("{} got {} proposals.", getAID().getName(), proposals.size());
		if (CONV_ID_PICKUP.equals(conversationId)) {
			return choosePickupProposalWithLongestQueue(proposals);
		} else {
			return null;
		}
	}
	
	private ACLMessage choosePickupProposalWithLongestQueue(Collection<ACLMessage> proposals) {
		ACLMessage bestProposal = null;
		int maxQueueLength = 0;
		for (final ACLMessage proposal : proposals) {
			try {
				final int queueLength = Integer.parseInt(proposal.getContent());
				if (queueLength > maxQueueLength) {
					maxQueueLength = queueLength;
					bestProposal = proposal;
				}
			} catch (NumberFormatException e) {
				LOG.warn("Unexpected message content: " + proposal.getContent() + ". Expected a number.");
			}
		}
		return bestProposal;
	}

	@Override
	public ACLMessage createResponseForProposal(String conversationId, ACLMessage bestProposal) throws ResponseCreationException {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			final ACLMessage response = bestProposal.createReply();
			response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			return response;
		} else {
			return null;
		}
	}

	@Override
	public long waitForResponseForAcceptedProposal(String conversationId) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			return 500;
		} else {
			return -1;
		}
	}

	@Override
	public void didReceiveResponseForAcceptedProposal(String conversationId, ACLMessage response) {
		currentPayload = MessageUtil.unwrapPayload(response);
		LOG.info(getAID().getName() + " received order " + currentPayload);
	}
	
	/**
	 * Asks for work regularily, if idle.
	 */
	private class AskForWorkBehaviour extends TickerBehaviour {

		private static final long serialVersionUID = -6570598243487555781L;

		public AskForWorkBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		protected void onTick() {
			if (currentPayload == null) {
				myAgent.addBehaviour(new CallingForProposalBehaviour(Constants.CONV_ID_PICKUP, YouBot.this));
			}
		}

	}
	
}
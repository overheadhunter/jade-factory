package factory.youbot;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.common.MessageUtil;
import factory.common.ResponseCreationException;
import factory.order.Order;
import factory.station.ServiceType;
import factory.visualization.VisualizationAdapter;
import factory.youbot.CallingForProposalBehaviour.CallingForProposal;

public class YouBot extends Agent implements Constants, CallingForProposal {

	private static final long serialVersionUID = 5093362251079611218L;
	private static final Logger LOG = LoggerFactory.getLogger(YouBot.class);

	private Order currentPayload = null;
	private Set<ServiceType> nextStepsForCurrentPayload = Collections.emptySet();

	@Override
	protected void setup() {
		LOG.info("Registered YouBot " + getAID().getName());
		
		addBehaviour(new AskForWorkBehaviour(this, 1000));
	}

	@Override
	public Collection<String> servicesToCall(String conversationId) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			return EnumSet.allOf(ServiceType.class).stream().map(st -> st.name()).collect(Collectors.toSet());
		} else if (CONV_ID_DROPOFF.equals(conversationId)) {
			return nextStepsForCurrentPayload.stream().map(st -> st.name()).collect(Collectors.toSet());
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Date getProposalDeadline(String conversationId) {
		final Calendar cal = new GregorianCalendar();
		cal.add(Calendar.MILLISECOND, 500);
		return cal.getTime();
	}

	@Override
	public void configureCfp(String conversationId, ACLMessage cfp) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			cfp.setContent("I need something to do...");
		} else if (CONV_ID_DROPOFF.equals(conversationId)) {
			cfp.setContent("I want to drop of my order...");
		}
	}

	@Override
	public ACLMessage chooseProposal(String conversationId, Collection<ACLMessage> proposals) {
		LOG.trace("{} got {} proposals.", getAID().getLocalName(), proposals.size());
		final ACLMessage bestProposal;
		if (CONV_ID_PICKUP.equals(conversationId)) {
			bestProposal = choosePickupProposalWithLongestQueue(proposals);
		} else if (CONV_ID_DROPOFF.equals(conversationId)) {
			bestProposal = chooseDropoffProposalWithShortestQueue(proposals);
		} else {
			bestProposal = null;
		}
		if (bestProposal != null) {
			final String orderId = (currentPayload == null) ? null : currentPayload.getAID().getLocalName();
			VisualizationAdapter.visualizeYouBotMovement(this.getLocalName(), bestProposal.getSender().getLocalName(), orderId);
		}
		return bestProposal;
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
				LOG.warn("Unexpected message content: {}. Expected a number.", proposal.getContent());
			}
		}
		return bestProposal;
	}

	private ACLMessage chooseDropoffProposalWithShortestQueue(Collection<ACLMessage> proposals) {
		ACLMessage bestProposal = null;
		int maxQueueLength = Integer.MAX_VALUE;
		for (final ACLMessage proposal : proposals) {
			try {
				final int queueLength = Integer.parseInt(proposal.getContent());
				if (queueLength < maxQueueLength) {
					maxQueueLength = queueLength;
					bestProposal = proposal;
				}
			} catch (NumberFormatException e) {
				LOG.warn("Unexpected message content: {}. Expected a number.", proposal.getContent());
			}
		}
		return bestProposal;
	}

	@Override
	public ACLMessage createAcceptResponseForProposal(String conversationId, ACLMessage bestProposal) throws ResponseCreationException {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			final ACLMessage response = bestProposal.createReply();
			response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			return response;
		} else if (CONV_ID_DROPOFF.equals(conversationId)) {
			try {
				final ACLMessage response = bestProposal.createReply();
				response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				response.setContentObject(currentPayload.getAID());
				currentPayload = null;
				nextStepsForCurrentPayload = Collections.emptySet();
				return response;
			} catch (IOException e) {
				throw new ResponseCreationException("Couldn't accept proposal", e);
			}
		} else {
			return null;
		}
	}

	@Override
	public long waitForResponseForAcceptedProposal(String conversationId) {
		if (CONV_ID_PICKUP.equals(conversationId)) {
			return 100;
		} else {
			return -1;
		}
	}

	@Override
	public void didReceiveResponseForAcceptedProposal(String conversationId, ACLMessage response) {
		if (CONV_ID_PICKUP.equals(conversationId) && currentPayload == null) {
			try {
				final AID aid = MessageUtil.unwrapPayload(response, AID.class);
				if (aid != null) {
					currentPayload = getContainerController().getAgent(aid.getName(), true).getO2AInterface(Order.class);
					nextStepsForCurrentPayload = currentPayload.getNextRequiredAssemblySteps();
					addBehaviour(new CallingForProposalBehaviour(CONV_ID_DROPOFF, this));
				}
			} catch (ControllerException e) {
				LOG.error("Could not analyze order.", e);
			}
		}
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
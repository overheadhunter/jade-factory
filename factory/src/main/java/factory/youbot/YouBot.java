package factory.youbot;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.common.MessageUtil;
import factory.order.Order;
import factory.station.ServiceType;

public class YouBot extends Agent implements Constants {

	private static final long serialVersionUID = 5093362251079611218L;
	private static final Logger LOG = LoggerFactory.getLogger(YouBot.class);
	
	private volatile boolean busy = false;
	private Order currentPayload = null;
	private String askForWorkMsgId;
	private final Collection<ACLMessage> workOffers = new HashSet<>();

	@Override
	protected void setup() {
		LOG.info("Registered YouBot " + getAID().getName());
		addBehaviour(new AskForWorkBehaviour(this, 5000));
		addBehaviour(new AcceptWorkBehaviour());
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
			if (busy) {
				return;
			} else if (workOffers.isEmpty()) {
				LOG.info(getAID().getName() + " asking for work.");
				askForWork();
			} else if (!workOffers.isEmpty()) {
				LOG.info(getAID().getName() + " choosing proposed work.");
				chooseUsefulWork();
			}
		}

		private void askForWork() {
			try {
				final Collection<DFAgentDescription> stationAgents = getAllStations();
				askForWorkMsgId = UUID.randomUUID().toString();
				final ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.setContent("need something to do..");
				cfp.setConversationId(CONV_ID_PICKUP);
				cfp.setReplyWith(askForWorkMsgId);
				for (final DFAgentDescription agent : stationAgents) {
					cfp.addReceiver(agent.getName());
				}
				myAgent.send(cfp);
			} catch (FIPAException e) {
				LOG.error("Unable to contact stations.", e);
			}
		}

		private Collection<DFAgentDescription> getAllStations() throws FIPAException {
			final Collection<DFAgentDescription> result = new HashSet<>();
			for (final ServiceType serviceType : ServiceType.values()) {
				final DFAgentDescription template = new DFAgentDescription();
				final ServiceDescription sd = new ServiceDescription();
				sd.setType(serviceType.name());
				template.addServices(sd);
				result.addAll(Arrays.asList(DFService.search(myAgent, template)));
			}
			return result;
		}
		
		private void chooseUsefulWork() {
			final ACLMessage proposal = getMostUrgentWorkOffer();
			if (proposal != null) {
				final ACLMessage accept = proposal.createReply();
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				accept.setReplyWith(askForWorkMsgId);
				myAgent.send(accept);
			}
		}
		
		private ACLMessage getMostUrgentWorkOffer() {
			ACLMessage mostUrgentProposal = null;
			int maxQueueLength = 0;
			for (final ACLMessage proposal : workOffers) {
				try {
					final int queueLength = Integer.parseInt(proposal.getContent());
					if (queueLength > maxQueueLength) {
						maxQueueLength = queueLength;
						mostUrgentProposal = proposal;
					}
				} catch (NumberFormatException e) {
					LOG.warn("Unexpected message content: " + proposal.getContent() + ". Expected a number.");
				}
			}
			return mostUrgentProposal;
		}

	}
	
	/**
	 * Collects replies to a "ask-for-work-request".
	 */
	private class AcceptWorkBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = -6189118814234620118L;

		@Override
		public void action() {
			if (askForWorkMsgId == null) {
				return;
			}
			
			final MessageTemplate mt = MessageTemplate.MatchInReplyTo(askForWorkMsgId);
			final ACLMessage proposal = myAgent. receive(mt);
			if (proposal != null) {
				if (proposal.getPerformative() == ACLMessage.PROPOSE) {
					workOffers.add(proposal);
				} else if (proposal.getPerformative() == ACLMessage.INFORM) {
					workOffers.clear();
					busy = true;
					currentPayload = MessageUtil.unwrapPayload(proposal, Order.class);
					LOG.info(getAID().getName() + " received order " + currentPayload);
				}
			}
		}
		
	}
	
}
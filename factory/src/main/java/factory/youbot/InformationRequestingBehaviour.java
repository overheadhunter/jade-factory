package factory.youbot;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.UUID;

public class InformationRequestingBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = -2378421398803188042L;
	private final String conversationId;
	private final String messageId;
	private final InformationRequesting requestor;
	
	public <T extends Agent & InformationRequesting> InformationRequestingBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.messageId = UUID.randomUUID().toString();
		this.requestor = agent;
	}

	@Override
	public void action() {
		final ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
		cfp.setConversationId(conversationId);
		cfp.setReplyWith(messageId);
		cfp.addReceiver(requestor.getInformationProvidingAgent());
		myAgent.send(cfp);
		
		final MessageTemplate mtMatchingMsgId = MessageTemplate.MatchInReplyTo(messageId);
		final MessageTemplate mtMatchingPerformative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final MessageTemplate mt = MessageTemplate.and(mtMatchingMsgId, mtMatchingPerformative);
		final ACLMessage response = myAgent.blockingReceive(mt);
		requestor.didReceiveInformationResponse(response);
	}
	
	/**
	 * Delegate interface for all decisions to be made by the agent, who creates a information request.
	 */
	public interface InformationRequesting {
		
		AID getInformationProvidingAgent();
		
		void didReceiveInformationResponse(ACLMessage response);
		
	}

}

package factory.order;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InformingBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -2352017252122891701L;
	private static final Logger LOG = LoggerFactory.getLogger(InformingBehaviour.class);

	private final String conversationId;
	private final Informing informing;

	public <T extends Agent & Informing> InformingBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.informing = agent;
	}

	@Override
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchConversationId(conversationId);
		final ACLMessage request = myAgent.receive(mt);
		if (request == null) {
			block();
		} else {
			switch (request.getPerformative()) {
			case ACLMessage.REQUEST:
				answerWithNextPossibleAssemblySteps(request);
				return;
			default:
				return;
			}
		}
	}

	private void answerWithNextPossibleAssemblySteps(ACLMessage request) {
		try {
			final ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.INFORM);
			informing.configureInformResponse(response);
			myAgent.send(response);
		} catch (IOException e) {
			LOG.error("Error during response generation.", e);
		}
	}

	public interface Informing {

		void configureInformResponse(ACLMessage response) throws IOException;

	}

}

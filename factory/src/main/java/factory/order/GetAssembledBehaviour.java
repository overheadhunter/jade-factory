package factory.order;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GetAssembledBehaviour extends CyclicBehaviour {
	
	private static final long serialVersionUID = -6756000544925644319L;
	
	private final String conversationId;
	private final GettingAssembled toBeAssembled;

	public <T extends Agent & GettingAssembled> GetAssembledBehaviour(String conversationId, T agent) {
		super(agent);
		this.conversationId = conversationId;
		this.toBeAssembled = agent;
	}

	@Override
	public void action() {
		final MessageTemplate mtMatchingConversationId = MessageTemplate.MatchConversationId(conversationId);
		final MessageTemplate mtMatchingPerformative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final MessageTemplate mt = MessageTemplate.and(mtMatchingConversationId, mtMatchingPerformative);
		final ACLMessage request = myAgent.receive(mt);
		if (request == null) {
			block();
		} else {
			toBeAssembled.assemble(request);
		}
	}

	public interface GettingAssembled {

		void assemble(ACLMessage request);

	}

}

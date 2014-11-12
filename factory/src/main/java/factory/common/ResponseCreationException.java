package factory.common;

import jade.lang.acl.ACLMessage;

public class ResponseCreationException extends Exception {

	private static final long serialVersionUID = -3881627526307337779L;
	
	public ResponseCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ACLMessage createFailureResponse(ACLMessage request) {
		final ACLMessage response = request.createReply();
		response.setPerformative(ACLMessage.FAILURE);
		response.setContent(getMessage());
		return response;
	}
	
}
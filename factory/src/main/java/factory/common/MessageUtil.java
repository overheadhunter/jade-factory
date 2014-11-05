package factory.common;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.Order;

public final class MessageUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);
	
	private MessageUtil() {
		throw new IllegalStateException("not instantiable.");
	}
	
	public static Order unwrapPayload(ACLMessage msg) {
		try {
			final Serializable payload = msg.getContentObject();
			if (payload instanceof Order) {
				return (Order) payload;
			}
		} catch (UnreadableException e) {
			LOG.error("Unwrapping message payload failed.", e);
		}
		return null;
	}

}

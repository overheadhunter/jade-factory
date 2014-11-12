package factory.common;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);
	
	private MessageUtil() {
		throw new IllegalStateException("not instantiable.");
	}
	
	public static <T extends Serializable> T unwrapPayload(ACLMessage msg, Class<T> type) {
		try {
			final Serializable payload = msg.getContentObject();
			if (payload == null) {
				return null;
			} else if (type.isAssignableFrom(payload.getClass())) {
				return type.cast(payload);
			}
		} catch (UnreadableException e) {
			LOG.error("Unwrapping message payload failed.", e);
		}
		return null;
	}

}

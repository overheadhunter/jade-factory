package factory.order;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.assembly.raw.Action;
import factory.assembly.raw.Assembly;
import factory.station.ServiceType;

public final class TaskFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskFactory.class);
	
	private TaskFactory() {
		throw new IllegalStateException("not instantiable");
	}
	
	public static Task createTaskTree(InputStream assemblyInstructions) throws InvalidOrderException {
		final Assembly assembly = parseInstructions(assemblyInstructions);
		return createTaskFromRawTask(assembly.getTask());
	}
	
	private static Task createTaskFromRawTask(final factory.assembly.raw.Task rawTask) throws InvalidOrderException {
		final ServiceType action = getServiceTypeForAction(rawTask.getAction());
		final Set<Task> rawSubtasks = new HashSet<>();
		for (final factory.assembly.raw.Task subtask : rawTask.getSubtasks().getTask()) {
			rawSubtasks.add(createTaskFromRawTask(subtask));
		}
		return new Task(action, rawSubtasks);
	}
	
	private static ServiceType getServiceTypeForAction(final Action action) throws InvalidOrderException {
		try {
			return ServiceType.valueOf(action.name());
		} catch (IllegalArgumentException e) {
			throw new InvalidOrderException("Unsupported action: " + action.name());
		}
	}
	
	private static Assembly parseInstructions(InputStream in) throws InvalidOrderException {
		try {
			final XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(in);
			final JAXBContext ctx = JAXBContext.newInstance("factory.assembly.raw");
			final Unmarshaller unmarshaller = ctx.createUnmarshaller();
			return unmarshaller.unmarshal(xsr, Assembly.class).getValue();
		} catch (JAXBException | XMLStreamException e) {
			LOG.error("Failed to parse assembly instructions.", e);
			throw new InvalidOrderException(e);
		}
	}
	
	public static class InvalidOrderException extends Exception {
		
		private static final long serialVersionUID = -8803026400123053594L;
		
		private InvalidOrderException(Throwable cause) {
			super(cause);
		}

		public InvalidOrderException(String message) {
			super(message);
		}
		
	}

}

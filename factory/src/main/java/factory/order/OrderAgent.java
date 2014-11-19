package factory.order;

import jade.core.Agent;

import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.order.TaskFactory.InvalidOrderException;
import factory.station.ServiceType;

public class OrderAgent extends Agent implements Order, Serializable {
	
	private static final long serialVersionUID = -3417265163977595139L;
	private static final Logger LOG = LoggerFactory.getLogger(OrderAgent.class);
	
	private Task rootTask;
	
	public OrderAgent() {
		registerO2AInterface(Order.class, this);
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		try {
			final InputStream in = OrderAgent.class.getResourceAsStream("/default-assembly.xml");
			rootTask = TaskFactory.createTaskTree(in);
		} catch (InvalidOrderException e) {
			LOG.error("Invalid assembly instructions.", e);
			this.doDelete();
		}
		
	}
	
	@Override
	public String toString() {
		if (rootTask == null) {
			return String.format("[%s %d]", getLocalName());
		} else {
			return String.format("[%s - %.2f done]", getLocalName(), rootTask.getProgress());
		}
	}

	@Override
	public void assemble(ServiceType action) {
		final Task task = rootTask.getNextAssemblyStep(action);
		if (task != null) {
			task.setFinished(true);
			LOG.debug("{} - assembled {}", action, this);
		}
	}

	@Override
	public EnumSet<ServiceType> getNextRequiredAssemblySteps() {
		if (rootTask.isFinished()) {
			return EnumSet.of(ServiceType.SHIPPING);
		} else {
			return rootTask.getPossibleNextAssemblySteps();
		}
	}

}

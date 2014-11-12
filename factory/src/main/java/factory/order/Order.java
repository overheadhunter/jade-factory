package factory.order;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.common.Constants;
import factory.order.GetAssembledBehaviour.GettingAssembled;
import factory.order.InformingBehaviour.Informing;
import factory.order.TaskFactory.InvalidOrderException;
import factory.station.ServiceType;

public class Order extends Agent implements Serializable, Informing, GettingAssembled {
	
	private static final long serialVersionUID = -3417265163977595139L;
	private static final AtomicInteger COUNTER = new AtomicInteger(1);
	private static final Logger LOG = LoggerFactory.getLogger(Order.class);
	
	private final int orderNumber;
	private Task rootTask;
	
	public Order() {
		this.orderNumber = COUNTER.getAndIncrement();
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		try {
			final InputStream in = Order.class.getResourceAsStream("/default-assembly.xml");
			rootTask = TaskFactory.createTaskTree(in);
		} catch (InvalidOrderException e) {
			LOG.error("Invalid assembly instructions.", e);
			this.doDelete();
		}
		
		addBehaviour(new InformingBehaviour(Constants.CONV_ID_QUERY_NEXT_ASSEMBLY_STEPS, this));
		addBehaviour(new GetAssembledBehaviour(Constants.CONV_ID_ASSEMBLE, this));
	}
	
	@Override
	public String toString() {
		if (rootTask == null) {
			return String.format("[Order %d]", orderNumber);
		} else {
			return String.format("[Order %d - %.2f done]", orderNumber, rootTask.getProgress());
		}
	}

	@Override
	public void configureInformResponse(ACLMessage response) throws IOException {
		if (rootTask.isFinished()) {
			response.setContentObject(EnumSet.of(ServiceType.SHIPPING));
		} else {
			response.setContentObject(rootTask.getPossibleNextAssemblySteps());
		}
	}

	@Override
	public void assemble(ACLMessage request) {
		final String actionName = request.getContent();
		try {
			final ServiceType action = ServiceType.valueOf(actionName);
			final Task task = rootTask.getNextAssemblyStep(action);
			if (task != null) {
				task.setFinished(true);
				LOG.debug("{} - assembled {}", actionName, this);
			}
		} catch(IllegalArgumentException e) {
			LOG.error("Unsupported assembly step {}", actionName);
		}
	}

}

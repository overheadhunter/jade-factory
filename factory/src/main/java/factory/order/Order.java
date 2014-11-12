package factory.order;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import factory.common.Constants;
import factory.order.InformingBehaviour.Informing;
import factory.station.ServiceType;

public class Order extends Agent implements Serializable, Informing {
	
	private static final long serialVersionUID = -3417265163977595139L;
	private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	private final int orderNumber;
	
	public Order() {
		this.orderNumber = COUNTER.getAndIncrement();
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		addBehaviour(new InformingBehaviour(Constants.CONV_ID_QUERY_NEXT_ASSEMBLY_STEPS, this));
	}
	
	@Override
	public String toString() {
		return String.format("[Order %d]", orderNumber);
	}

	@Override
	public void configureInformResponse(ACLMessage response) throws IOException {
		response.setContentObject(EnumSet.of(ServiceType.SCREWING, ServiceType.PRESSFITTING));
	}

}

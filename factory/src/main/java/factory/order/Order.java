package factory.order;

import java.util.EnumSet;

import factory.station.ServiceType;
import jade.core.AID;

public interface Order {
	
	AID getAID();
	void assemble(ServiceType action);
	EnumSet<ServiceType> getNextRequiredAssemblySteps();

}

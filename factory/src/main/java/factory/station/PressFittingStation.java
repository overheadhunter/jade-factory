package factory.station;

import factory.order.Order;

public class PressFittingStation extends AbstractAssemblyStation {
	
	private static final long serialVersionUID = 1744214026475059005L;

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.PRESSFITTING;
	}

	@Override
	protected String getStationName() {
		return getAID().getLocalName();
	}

	@Override
	protected void assemble(Order order) throws InterruptedException {
		Thread.sleep(10000);
	}

}

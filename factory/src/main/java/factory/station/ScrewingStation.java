package factory.station;

public class ScrewingStation extends AbstractAssemblyStation {

	private static final long serialVersionUID = -2181721592346982182L;

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.SCREWING;
	}

	@Override
	protected String getStationName() {
		return getAID().getLocalName();
	}

}

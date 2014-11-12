package factory.station;

public class PressFittingStation extends AbstractAssemblyStation {

	private static final long serialVersionUID = -1793509949459648133L;

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.PRESSFITTING;
	}

	@Override
	protected String getStationName() {
		return getAID().getLocalName();
	}

}

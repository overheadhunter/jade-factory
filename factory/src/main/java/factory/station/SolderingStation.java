package factory.station;

public class SolderingStation extends AbstractAssemblyStation {

	private static final long serialVersionUID = -3777347838299090006L;

	@Override
	protected ServiceType getServiceType() {
		return ServiceType.SOLDERING;
	}

	@Override
	protected String getStationName() {
		return getAID().getLocalName();
	}

}

package factory.visualization;

import factory.visualization.log.LoggingVisualizer;

public final class VisualizationAdapter {
	
	private VisualizationAdapter() {
		throw new IllegalStateException("Not instantiable.");
	}
	
	private static Visualizing visualizer;
	
	public static void visualizeOrderArrival(String orderId) {
		visualizer.orderArrived(orderId);
	}
	
	public static void visualizeOrderShipping(String orderId) {
		visualizer.orderShipped(orderId);
	}
	
	public static void visualizeStationQueueChange(String stationId, Integer inQueue, Integer outQueue) {
		visualizer.stationQueueDidChange(stationId, inQueue, outQueue);
	}
	
	public static void visualizeStationStartsWorking(String stationId, String orderId) {
		visualizer.stationStartsWorking(stationId, orderId);
	}
	
	public static void visualizeStationStopsWorking(String stationId, String orderId) {
		visualizer.stationStopsWorking(stationId, orderId);
	}
	
	public static void visualizeYouBotMovement(String youBotId, String stationId, String orderId) {
		try {
			final BlockingVisualizationCallback callback = new BlockingVisualizationCallback();
			visualizer.youBotWillMoveTo(youBotId, stationId, orderId, callback);
			callback.waitUntilDone();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Visualizing getVisualizer() {
		return visualizer;
	}

	public static void setVisualizer(Visualizing visualizer) {
		if (visualizer == null) {
			// default to log output.
			visualizer = new LoggingVisualizer();
		}
		VisualizationAdapter.visualizer = visualizer;
	}

}

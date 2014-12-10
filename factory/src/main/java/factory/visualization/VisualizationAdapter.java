package factory.visualization;

import factory.visualization.log.LoggingVisualizer;

public final class VisualizationAdapter {
	
	private VisualizationAdapter() {
		throw new IllegalStateException("Not instantiable.");
	}
	
	private static Visualizing visualizer = new LoggingVisualizer();
	
	public static void visualizeStationQueueChange(String stationId, Integer inQueue, Integer outQueue) {
		visualizer.stationQueueDidChange(stationId, inQueue, outQueue);
	}
	
	public static void visualizeStationStartsWorking(String stationId) {
		visualizer.stationStartsWorking(stationId);
	}
	
	public static void visualizeStationStopsWorking(String stationId) {
		visualizer.stationStopsWorking(stationId);
	}
	
	public static void visualizeYouBotMovement(String youBotId, String stationId, boolean withPayload) {
		try {
			final BlockingVisualizationCallback callback = new BlockingVisualizationCallback();
			visualizer.youBotWillMoveTo(youBotId, stationId, withPayload, callback);
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

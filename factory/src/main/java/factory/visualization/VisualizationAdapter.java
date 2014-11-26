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
	
	public static void visualizeYouBotMovement(String youBotId, String stationId) {
		try {
			final BlockingVisualizationCallback callback = new BlockingVisualizationCallback();
			visualizer.youBotWillMoveTo(youBotId, stationId, callback);
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

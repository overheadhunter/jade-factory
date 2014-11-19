package factory.visualization;


public interface Visualizing {
	
	void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue);
	
	void youBotWillMoveTo(String youBotId, String stationId, BlockingVisualizationCallback callbackWhenDone);

}

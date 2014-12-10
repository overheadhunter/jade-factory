package factory.visualization;


public interface Visualizing {
	
	void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue);
	
	void stationStartsWorking(String stationId);
	
	void stationStopsWorking(String stationId);
	
	void youBotWillMoveTo(String youBotId, String stationId, boolean withPayload, BlockingVisualizationCallback callbackWhenDone);

}

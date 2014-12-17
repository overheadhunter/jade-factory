package factory.visualization;


public interface Visualizing {
	
	void orderArrived(String orderId);
	
	void orderShipped(String orderId);
	
	void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue);
	
	void stationStartsWorking(String stationId, String orderId);
	
	void stationStopsWorking(String stationId, String orderId);
	
	void youBotWillMoveTo(String youBotId, String stationId, String orderId, BlockingVisualizationCallback callbackWhenDone);

}

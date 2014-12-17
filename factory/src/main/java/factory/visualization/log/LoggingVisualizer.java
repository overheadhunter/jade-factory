package factory.visualization.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.visualization.BlockingVisualizationCallback;
import factory.visualization.Visualizing;

public class LoggingVisualizer implements Visualizing {
	
	private static final Logger LOG = LoggerFactory.getLogger(LoggingVisualizer.class);
	
	@Override
	public void orderArrived(String orderId) {
		LOG.info("{} arrived", orderId);
	}
	
	@Override
	public void orderShipped(String orderId) {
		LOG.info("{} shipped", orderId);		
	}


	@Override
	public void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue) {
		LOG.info("Queue of station {} changed. IN: {} OUT: {}", stationId, inQueue, outQueue);
	}

	@Override
	public void youBotWillMoveTo(String youBotId, String stationId, String orderId, BlockingVisualizationCallback callbackWhenDone) {
		if (orderId != null) {
			LOG.info("Youbot {} carries sth. to station {}", youBotId, stationId);
		} else {
			LOG.info("Youbot {} moves to station {}", youBotId, stationId);
		}
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// ignore
		}
		callbackWhenDone.done();
	}

	@Override
	public void stationStartsWorking(String stationId, String orderId) {
		LOG.info("Station {} is now working.", stationId);
	}

	@Override
	public void stationStopsWorking(String stationId, String orderId) {
		LOG.info("Station {} stopped working.", stationId);
	}

}

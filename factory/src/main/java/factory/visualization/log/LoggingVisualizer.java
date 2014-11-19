package factory.visualization.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.visualization.BlockingVisualizationCallback;
import factory.visualization.Visualizing;

public class LoggingVisualizer implements Visualizing {
	
	private static final Logger LOG = LoggerFactory.getLogger(LoggingVisualizer.class);

	@Override
	public void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue) {
		LOG.info("Queue of station {} changed. IN: {} OUT: {}", stationId, inQueue, outQueue);
	}

	@Override
	public void youBotWillMoveTo(String youBotId, String stationId, BlockingVisualizationCallback callbackWhenDone) {
		LOG.info("Youbot {} moves to station {}", youBotId, stationId);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// ignore
		}
		callbackWhenDone.done();
	}

}

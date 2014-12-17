package factory.visualization.gui;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import factory.station.OrderEntryStation;
import factory.station.PressFittingStation;
import factory.station.ScrewingStation;
import factory.station.ShippingStation;
import factory.station.SolderingStation;
import factory.visualization.BlockingVisualizationCallback;
import factory.visualization.VisualizationAdapter;
import factory.visualization.Visualizing;
import factory.youbot.YouBot;

public class SceneController implements Visualizing, Initializable {

	private static final Object[] NO_ARGS = {};
	private static final Logger LOG = LoggerFactory.getLogger(SceneController.class);
	private final ContainerController jadeContainer;
	private final AtomicInteger youBotCounter = new AtomicInteger();
	private final AtomicInteger pressfittingCounter = new AtomicInteger();
	private final AtomicInteger screwingCounter = new AtomicInteger();
	private final AtomicInteger solderingCounter = new AtomicInteger();
	private final Map<String, AgentVisualization> stations = new HashMap<>();
	private final Map<String, AgentVisualization> youBots = new HashMap<>();

	@FXML
	private Canvas canvas;

	public SceneController() {
		final Profile myProfile = new ProfileImpl();
		jadeContainer = jade.core.Runtime.instance().createMainContainer(myProfile);
		VisualizationAdapter.setVisualizer(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			final AgentController entry = jadeContainer.createNewAgent("entry", OrderEntryStation.class.getName(), NO_ARGS);
			final AgentController exit = jadeContainer.createNewAgent("exit", ShippingStation.class.getName(), NO_ARGS);
			entry.start();
			exit.start();
			stations.put("entry", new StationVisualization(entry, 0.0, 250.0));
			stations.put("exit", new StationVisualization(exit, 500.0, 250.0));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}

	@FXML
	public void addYouBot(ActionEvent event) {
		try {
			final String agentName = "youBot_" + youBotCounter.getAndIncrement();
			final AgentController ctrl = jadeContainer.createNewAgent(agentName, YouBot.class.getName(), NO_ARGS);
			ctrl.start();
			youBots.put(agentName, new YouBotVisualization(ctrl));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}

	@FXML
	public void addSolderingStation(ActionEvent event) {
		try {
			final String agentName = "soldering_" + solderingCounter.getAndIncrement();
			final AgentController ctrl = jadeContainer.createNewAgent(agentName, SolderingStation.class.getName(), NO_ARGS);
			ctrl.start();
			stations.put(agentName, new StationVisualization(ctrl, 50.0 * solderingCounter.get(), 20.0));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}
	
	@FXML
	public void addScrewingStation(ActionEvent event) {
		try {
			final String agentName = "screwing_" + screwingCounter.getAndIncrement();
			final AgentController ctrl = jadeContainer.createNewAgent(agentName, ScrewingStation.class.getName(), NO_ARGS);
			ctrl.start();
			stations.put(agentName, new StationVisualization(ctrl, 250 + 50.0 * screwingCounter.get(), 20.0));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}
	
	@FXML
	public void addPressfittingStation(ActionEvent event) {
		try {
			final String agentName = "pressfitting_" + pressfittingCounter.getAndIncrement();
			final AgentController ctrl = jadeContainer.createNewAgent(agentName, PressFittingStation.class.getName(), NO_ARGS);
			ctrl.start();
			stations.put(agentName, new StationVisualization(ctrl, 50.0 * pressfittingCounter.get(), 480.0));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}
	
	@Override
	public void orderArrived(String orderId) {
		// ignore
	}
	
	@Override
	public void orderShipped(String orderId) {
		// ignore		
	}

	@Override
	public void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue) {
		final AgentVisualization station = stations.get(stationId);
		station.setInQueueLength(inQueue);
		station.setOutQueueLength(outQueue);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				drawAll();
			}
		});
	}
	
	@Override
	public void stationStartsWorking(String stationId, String orderId) {
		final StationVisualization station = (StationVisualization) stations.get(stationId);
		station.setCurrentlyDoingStuff(true);
	}

	@Override
	public void stationStopsWorking(String stationId, String orderId) {
		final StationVisualization station = (StationVisualization) stations.get(stationId);
		station.setCurrentlyDoingStuff(false);
	}

	@Override
	public void youBotWillMoveTo(String youBotId, String stationId, String orderId, BlockingVisualizationCallback callbackWhenDone) {
		final YouBotVisualization youBot = (YouBotVisualization) youBots.get(youBotId);
		youBot.setPayload(orderId != null);
		final AgentVisualization station = stations.get(stationId);
		final double distance = Math.sqrt(Math.abs(youBot.getPosX() - station.getPosX()) + Math.abs(youBot.getPosY() - station.getPosY()));
		final DoubleProperty x = new SimpleDoubleProperty(youBot.getPosX());
		final DoubleProperty y = new SimpleDoubleProperty(youBot.getPosY());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final Timeline tl = new Timeline(new KeyFrame(Duration.millis(50.0*distance), new KeyValue(x, station.getPosX()), new KeyValue(y, station.getPosY())));
				final AnimationTimer timer = new AnimationTimer() {
					@Override
					public void handle(long now) {
						youBot.setPosX(x.doubleValue());
						youBot.setPosY(y.doubleValue());
						drawAll();
					}
				};
				tl.setOnFinished(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						callbackWhenDone.done();
						timer.stop();
						youBot.setPayload(false);
					}
				});
				timer.start();
				tl.play();
			}
		});
	}

	private void drawAll() {
		canvas.getGraphicsContext2D().clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
		for (AgentVisualization agent : stations.values()) {
			agent.draw(canvas.getGraphicsContext2D());
		}
		for (AgentVisualization agent : youBots.values()) {
			agent.draw(canvas.getGraphicsContext2D());
		}
	}

}

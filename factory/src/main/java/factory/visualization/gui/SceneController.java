package factory.visualization.gui;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;

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
	private final AtomicInteger youBotCounter = new AtomicInteger(1);
	private final AtomicInteger pressfittingCounter = new AtomicInteger(1);
	private final AtomicInteger screwingCounter = new AtomicInteger(1);
	private final AtomicInteger solderingCounter = new AtomicInteger(1);
	private final Map<String, AgentVisualization> agents = new HashMap<>();

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
			agents.put("entry", new StationVisualization(entry, 0.0, 250.0));
			agents.put("exit", new StationVisualization(exit, 500.0, 250.0));
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
			agents.put(agentName, new YouBotVisualization(ctrl));
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
			agents.put(agentName, new StationVisualization(ctrl, 50.0 * screwingCounter.get(), 480.0));
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
			agents.put(agentName, new StationVisualization(ctrl, 250 + 50.0 * screwingCounter.get(), 20.0));
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
			agents.put(agentName, new StationVisualization(ctrl, 50.0 * solderingCounter.get(), 20.0));
			drawAll();
		} catch (StaleProxyException e) {
			LOG.error("Could not create new agent", e);
		}
	}

	@Override
	public void stationQueueDidChange(String stationId, Integer inQueue, Integer outQueue) {
		LOG.info("Queue of station {} changed. IN: {} OUT: {}", stationId, inQueue, outQueue);
		final AgentVisualization station = agents.get(stationId);
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
	public void youBotWillMoveTo(String youBotId, String stationId, BlockingVisualizationCallback callbackWhenDone) {
		LOG.info("Youbot {} moves to station {}", youBotId, stationId);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// who cares
		}
		callbackWhenDone.done();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				drawAll();
			}
		});
	}

	private void drawAll() {
		canvas.getGraphicsContext2D().clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
		for (Entry<String, AgentVisualization> entry : agents.entrySet()) {
			entry.getValue().draw(canvas.getGraphicsContext2D());
		}
	}

}

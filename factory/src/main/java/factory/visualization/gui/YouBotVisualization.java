package factory.visualization.gui;

import jade.wrapper.AgentController;
import javafx.scene.canvas.GraphicsContext;

public class YouBotVisualization extends AgentVisualization {

	YouBotVisualization(AgentController agent) {
		super(agent);
	}

	@Override
	public void draw(GraphicsContext ctx) {
		ctx.fillOval(getPosX()-10.0, getPosY()-10.0, 20.0, 20.0);
	}

}

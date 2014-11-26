package factory.visualization.gui;

import jade.wrapper.AgentController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class YouBotVisualization extends AgentVisualization {
	
	private final Paint fill;

	YouBotVisualization(AgentController agent) {
		super(agent);
		this.fill = new Color(Math.random(), Math.random(), Math.random(), 1.0);
	}

	@Override
	public void draw(GraphicsContext ctx) {
		ctx.setFill(fill);
		ctx.fillOval(getPosX()-10.0, getPosY()-10.0, 20.0, 20.0);
	}

}

package factory.visualization.gui;

import jade.wrapper.AgentController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class StationVisualization extends AgentVisualization {

	StationVisualization(AgentController agent, double posX, double posY) {
		super(agent, posX, posY);
	}

	@Override
	public void draw(GraphicsContext ctx) {
		ctx.setFill(Color.BLACK);
		ctx.fillRect(getPosX()-20.0, getPosY()-20.0, 40.0, 40.0);
		ctx.setFill(Color.GREEN);
		ctx.fillRect(getPosX()-25.0, getPosY()+20.0 - getInQueueLength()*5.0, 10.0, getInQueueLength()*5.0);
		ctx.setFill(Color.RED);
		ctx.fillRect(getPosX()+15.0, getPosY()+20.0 - getOutQueueLength()*5.0, 10.0, getOutQueueLength()*5.0);
	}

}

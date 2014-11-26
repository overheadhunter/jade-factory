package factory.visualization.gui;

import javafx.scene.canvas.GraphicsContext;
import jade.wrapper.AgentController;

public abstract class AgentVisualization {
	
	private final AgentController agent;
	private double posX;
	private double posY;
	private int inQueueLength;
	private int outQueueLength;
	
	AgentVisualization(AgentController agent) {
		this(agent, Math.random() * 500.0, Math.random() * 500.0);
	}
	
	AgentVisualization(AgentController agent, double posX, double posY) {
		this.agent = agent;
		this.posX = posX;
		this.posY = posY;
	}
	
	public abstract void draw(GraphicsContext ctx);

	public AgentController getAgent() {
		return agent;
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public int getInQueueLength() {
		return inQueueLength;
	}

	public void setInQueueLength(int inQueueLength) {
		this.inQueueLength = inQueueLength;
	}

	public int getOutQueueLength() {
		return outQueueLength;
	}

	public void setOutQueueLength(int outQueueLength) {
		this.outQueueLength = outQueueLength;
	}

}

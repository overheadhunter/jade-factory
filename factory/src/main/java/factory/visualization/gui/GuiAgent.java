package factory.visualization.gui;

import jade.core.Agent;
import javafx.application.Application;

public class GuiAgent extends Agent {
	
	private static final long serialVersionUID = -6493472082907321282L;
	
	@Override
	protected void setup() {
		Application.launch(FactoryGui.class);
	}

}

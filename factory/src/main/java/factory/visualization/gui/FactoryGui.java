package factory.visualization.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FactoryGui extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		final Parent root = FXMLLoader.load(getClass().getResource("/gui.fxml"));
		final Scene scene = new Scene(root);
		primaryStage.setTitle("Rockin Factory");
		primaryStage.centerOnScreen();
		primaryStage.show();
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
	}

}

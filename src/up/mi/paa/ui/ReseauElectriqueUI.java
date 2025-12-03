package up.mi.paa.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ReseauElectriqueUI extends Application {
	public void start(Stage stage) throws Exception {
		stage.setTitle("Reseau Electrique");
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

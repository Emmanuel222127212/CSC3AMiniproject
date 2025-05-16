package maze_gui;



import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.View;

import gamestuff.Game;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class gui_main extends Application{

	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		HBox rootBox = new HBox();

		/*display for maze,
		submit path button
		reset maze button
		timer
		compare path button
		panel of previos attempts
		panel to show graph 
		button
		*/
		
		
		Game game = new Game("tt");
		
		InformationPanel Panel1 = new InformationPanel(game);
		Panel1.setId("root");
		
		
		VBox vBox = new VBox();
		rootBox.getChildren().add(Panel1);
		rootBox.getChildren().add(vBox);
		VBox secondPanel = new VBox();
		Scene mainScene = new Scene(rootBox);
		
		
		Panel1.SelectImage(secondPanel);
		
		
		rootBox.setPrefSize(950,500);
		rootBox.getChildren().add(secondPanel);
		
		
		Panel1.setAttemptButton(mainScene);
		mainScene.getStylesheets().add("/styles/try.css");
		
		VBox start = new VBox();
		TextArea name =new TextArea();
		name.setMaxHeight(10);
		Scene startScene = new Scene(start);
		Button swtiButton = new Button("Start Test");
		
		swtiButton.setOnAction(e -> {
			game.setPlayer(name.getText());
			Panel1.change();
			//System.out.println(game.getplayerName());
			arg0.setScene(mainScene);});
		
		
		start.getChildren().addAll(name,swtiButton);
	
		arg0.setScene(startScene);
		arg0.show();
		
	}
	
	public static void main(String[] args) {
		


		launch(args);
	}

}

package maze_gui;



import GraphADT.MazeProcessor;
import gamestuff.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class gui_main extends Application{

	@Override
	public void start(Stage arg0) throws Exception {
		
		//removing style things
		//arg0.initStyle(StageStyle.UNDECORATED);
		arg0.getIcons().add(new Image("/utilityImg/brain.png"));
		

		
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
		
		 // Integration of MazeProcessor
        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        MazeProcessor mazeProcessor = new MazeProcessor(imageView);
        Button similarityButton = new Button("Show Similarity");
        String[] imagePath = {null}; // Store path
        // Wrap InformationPanel.SelectImage
        InformationPanelWrapper wrapper = new InformationPanelWrapper(Panel1, imageView);
        wrapper.SelectImage(secondPanel, path -> imagePath[0] = path);
        similarityButton.setOnAction(e -> {
            if (imagePath[0] != null) {
                mazeProcessor.processMazeImage(imagePath[0]);
            } else {
                System.out.println("No image selected.");
            }
        });
        secondPanel.getChildren().addAll(similarityButton);
		
		
		
		
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

		startScene.getStylesheets().add("/styles/try.css");

		//Start button

		Button swtiButton = new Button("Start Test");
		
		swtiButton.setOnAction(e -> {
			game.setPlayer(name.getText());
			Panel1.change();
			//System.out.println(game.getplayerName());
			arg0.setTitle("Maze Congiitive Tester for :" + name.getText());
			arg0.setScene(mainScene);});
		
		
		start.getChildren().addAll(name,swtiButton);
		arg0.setTitle("Maze Congiitive Tester");
		arg0.setScene(startScene);
		arg0.show();
		
	}
	
	public static void main(String[] args) {
		


		launch(args);
	}

}

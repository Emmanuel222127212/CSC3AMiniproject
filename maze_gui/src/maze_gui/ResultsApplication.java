package maze_gui;



import java.io.File;

import GraphADT.MazeProcessor;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ResultsApplication{
	private ImageView currentImage;
	private MazeProcessor mazeProcessor;
	private File currentFile;
	Button similarityButton = new Button("Show Similarity");
	
	public ResultsApplication(Image I) {
		
		        currentImage = new ImageView(I);
		
		        System.out.println("Error: Image is null!");
		  

		mazeProcessor =  new MazeProcessor(currentImage);
	}
	
	public ResultsApplication(File i) {
		currentFile = i;
		currentImage = new ImageView(new Image(currentFile.toURI().toString()));
		mazeProcessor =  new MazeProcessor(currentImage);
	}
	

	public ResultsApplication(GraphADT.ArrayList<Vertex<SuperPixel>> playerpath, Image I) {
		 new ResultsApplication(I);
		//handle logic for drawing the path
	}
	
	private void setButton() {
		 
	        // Wrap InformationPanel.SelectImage
	      
	        similarityButton.setOnAction(e -> {
	            if (currentFile != null) {
	            	System.out.println(currentFile.getPath());
	                mazeProcessor.processMazeImage(currentFile.getPath().toString());
	            } else {
	                System.out.println("No image selected.");
	            }
	        });
	}
	// From InfoPanel
	private void setStyleAllign(Pane pane) {
		pane.getStyleClass().add(".vbox");
	}
	
	
	public void startWin() {
		// TODO Auto-generated method stub
		
		Platform.runLater(()->{
			 Stage arg0 = new Stage();
				VBox controlPane = new VBox();
				HBox imageVBox = new HBox();
				
				/**
				 * The game shows this maze on the screen, and you can try to solve it. 
				 * But the game also has a cool feature: it can analyze the maze to find parts that look similar to each other,
				 *  like paths that have the same color or are close together. 
				 *  This is called similarity detection, and it helps the game understand the maze better. 
				 *  The game draws red lines between these similar parts so you can see them clearly.
				 */
				Label explain = new Label(
						" 		* The game shows this maze on the screen, and you can try to solve it. \r\n"
				+ "				* But the game also has a cool feature: it can analyze the maze to find parts that look similar to each other,\r\n"
				+ "				* like paths that have the same color or are close together. \r\n"
				+ "				* This is called similarity detection, and it helps the game understand the maze better. \r\n"
				+ "				* The game draws red lines between these similar parts so you can see them clearly. :)"
				);
				explain.setAlignment(Pos.BASELINE_CENTER);
				
				
				imageVBox.getChildren().addAll(explain,currentImage);
				setButton();

				controlPane.getChildren().addAll(imageVBox,similarityButton);
				setStyleAllign(controlPane);
				
				//Icon
				arg0.getIcons().add(new Image("/utilityImg/brain.png"));
				
				arg0.setScene(new Scene(controlPane));
				arg0.show();	
		});
	}

}

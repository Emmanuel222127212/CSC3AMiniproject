package maze_gui;



import java.io.File;

import GraphADT.MazeProcessor;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
	
	
	public void startWin() {
		// TODO Auto-generated method stub
		
		Platform.runLater(()->{
			 Stage arg0 = new Stage();
				VBox controlPane = new VBox();
				HBox imageVBox = new HBox();
				
				Label explain = new Label("explanation will go here");
				imageVBox.getChildren().addAll(explain,currentImage);
				setButton();

				controlPane.getChildren().addAll(imageVBox,similarityButton);
				
				arg0.setScene(new Scene(controlPane));
				arg0.show();	
		});
	}

}

package maze_gui;

import java.io.File;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class revealApplication{

	private ImageView currentImage;
	private File currentFile;
	Button pathfindingButton = new Button("Show Solution");

	public revealApplication(Image I) {

		currentImage = new ImageView(I);

		System.out.println("Error: Image is null!");

 
	}

	public revealApplication(File i) {
		currentFile = i;
		currentImage = new ImageView(new Image(currentFile.toURI().toString()));
	 
	}


	public revealApplication(GraphADT.ArrayList<Vertex<SuperPixel>> systempath, Image I) {
		new ResultsApplication(I);
		//handle logic for drawing the path
	}

	private void setButton() {
	    pathfindingButton.setOnAction(e -> {
	        if (currentFile != null) {
	            String newPath = "Converted/" + currentFile.getName();
	            File newFile = new File(newPath);

	            if (newFile.exists()) {
	                Image newImage = new Image(newFile.toURI().toString());
	             
	                currentImage.setFitWidth(800);
	                currentImage.setFitHeight(800);
	                currentImage.setPreserveRatio(true);
	                currentImage.setSmooth(true);
	                currentImage.setCache(true);
	                currentImage.setImage(newImage);  
	                System.out.println("Updated image view with path: " + newPath);
	            } else {
	                System.out.println("Processed file not found at: " + newPath);
	            }
	        } else {
	            System.out.println("No image selected.");
	        }
	    });
	}


	public void startWin() {
		// TODO Auto-generated method stub

		Platform.runLater(()->{
			Stage arg0 = new Stage();
			arg0.setTitle("Path Finder");
			arg0.getIcons().add(new Image("/utilityImg/brain.png"));
			VBox controlPane = new VBox();
			HBox imageVBox = new HBox();
 
			imageVBox.getChildren().addAll(currentImage);
			setButton();

			controlPane.getChildren().addAll(imageVBox,pathfindingButton);

			arg0.setScene(new Scene(controlPane));
			arg0.show();	
		});
	}

}

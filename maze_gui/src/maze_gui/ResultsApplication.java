package maze_gui;



import java.awt.image.BufferedImage;
import java.io.File;

import GraphADT.ArrayList;
import GraphADT.MazeProcessor;
import GraphADT.SimilarityDetector;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
	
	//to show the similarity
	Canvas overlaySimilarity ;
	Pane imagePane ;
	
	public ResultsApplication(Image I)
	{
		
        currentImage = new ImageView(I);
        System.out.println("Error: Image is null!");
        mazeProcessor =  new MazeProcessor(currentImage);
        
        if (currentImage != null && currentImage.getImage() != null) 
        {
            overlaySimilarity = new Canvas(currentImage.getImage().getWidth(), currentImage.getImage().getHeight());
        } 
        else
        {
            System.out.println("Image is not loaded yet, cannot create canvas.");
        }
        
        imagePane = new Pane(currentImage,overlaySimilarity);
	}
	
	public ResultsApplication(File i) 
	{
		currentFile = i;
		currentImage = new ImageView(new Image(currentFile.toURI().toString()));
		
		mazeProcessor =  new MazeProcessor(currentImage);
		
		if (currentImage != null && currentImage.getImage() != null) 
        {
            overlaySimilarity = new Canvas(currentImage.getImage().getWidth(), currentImage.getImage().getHeight());
        } 
        else
        {
            System.out.println("Image is not loaded yet, cannot create canvas.");
        }
		
		imagePane = new Pane(currentImage,overlaySimilarity);
	}
	

	public ResultsApplication(GraphADT.ArrayList<Vertex<SuperPixel>> playerpath, Image I) {
		// new ResultsApplication(I);
		//handle logic for drawing the path
		this(I);
		if(I == null)
		{
			throw new IllegalArgumentException("Image cant be null");
		}
		
	}
	
	private void setButton() {
		 
	        // Wrap InformationPanel.SelectImage
	      
	        similarityButton.setOnAction(e -> {
	            if (currentFile != null) {
	            	System.out.println(currentFile.getPath());
	                mazeProcessor.processMazeImage(currentFile.getPath().toString());
	                
	                
	                //
	                // Assume you have user and system superpixels from MazeProcessor
	                ArrayList<SuperPixel> userSPs = mazeProcessor.getUserPX();
	                ArrayList<SuperPixel> systemSPs = mazeProcessor.getSystemPx();
	                BufferedImage bufferedImage = mazeProcessor.getBufferedImage();

	                SimilarityDetector detector = new SimilarityDetector(userSPs, systemSPs, bufferedImage);
	                ArrayList<SuperPixel[]> matchedPairs = detector.findMostSimilarPairs(0.8);
	                // 0.8 similarity threshold
	                
	                drawSimilarityLines(overlaySimilarity,matchedPairs);
	            }
	            else 
	            {
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
				
				String expText =  
						"  The maze is divided into tiny blocks, called superpixels. These are small regions that represent parts of the maze.\r\n"
						+ "\r\n"
						+ "The app looks for two different paths in the image:\r\n"
						+ "\r\n"
						+ "*The solution path (usually shown in green).\r\n"
						+ "\r\n"
						+ "*The user's path (usually shown in blue — the one you drew).\r\n"
						+ "\r\n"
						+ "For each part of the user’s path, the app tries to find a similar part of the solution path, based on two things:\r\n"
						+ "\r\n"
						+ "-Color similarity (are they similar shades?).\r\n"
						+ "\r\n"
						+ "-Position similarity (are they close together in the maze).\r\n"
						+ "\r\n"
						+ "*When the app finds a close match between a user and solution superpixel, it:\r\n"
						+ "\r\n"
						+ "Calculates a similarity score (from 0 to 1).\r\n"
						+ "\r\n"
						+ "If the score is high enough, it draws a red line connecting the matching parts — so you can see where your path matches the correct one. ";
				Label explain = new Label(expText);
				
				explain.setWrapText(true);
				
				
				VBox explanation = new VBox(explain);
				explanation.setAlignment(Pos.TOP_LEFT);
				explanation.setMaxWidth(500);
				
				
				//imageVBox.getChildren().addAll(explanation,currentImage);
				imageVBox.getChildren().addAll(explanation,imagePane);
				setButton();

				controlPane.getChildren().addAll(imageVBox,similarityButton);
				setStyleAllign(controlPane);
				
				//Icon
				arg0.getIcons().add(new Image("/utilityImg/brain.png"));
				
				arg0.setScene(new Scene(controlPane));
				arg0.setTitle("Similarity Detection");
				arg0.show();	
		});
	}
	
	
	private void drawSimilarityLines(Canvas canvas, GraphADT.ArrayList<SuperPixel[]> matchedPairs)
	{
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		for(SuperPixel[] pairs : matchedPairs)
		{
			SuperPixel userPx = pairs[0];
			SuperPixel systemPx = pairs[1];
			
			
			double x1 = userPx.getAvgPixelXPos();
	        double y1 = userPx.getyAvgPixelYPos();
	        double x2 = systemPx.getAvgPixelXPos();
	        double y2 = systemPx.getyAvgPixelYPos();

	        gc.strokeLine(x1, y1, x2, y2);
		}
	}

}

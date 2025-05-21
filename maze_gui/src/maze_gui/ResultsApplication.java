package maze_gui;

import java.awt.image.BufferedImage;
import java.io.File;
import GraphADT.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
	
	//to show the similarity
	Canvas overlaySimilarity ;
	Pane imagePane ;
	
	public ResultsApplication(Image I)
	{
		
        currentImage = new ImageView(I);
        mazeProcessor = new MazeProcessor(currentImage);
        if (currentImage != null && currentImage.getImage() != null) {
            overlaySimilarity = new Canvas(currentImage.getImage().getWidth(), currentImage.getImage().getHeight());
        } else {
            System.out.println("Image is not loaded yet, cannot create canvas.");
        }
        imagePane = new Pane(currentImage, overlaySimilarity);
    }

    public ResultsApplication(File i) {
        currentFile = i;
        currentImage = new ImageView(new Image(currentFile.toURI().toString()));
        mazeProcessor = new MazeProcessor(currentImage);
        if (currentImage != null && currentImage.getImage() != null) {
            overlaySimilarity = new Canvas(currentImage.getImage().getWidth(), currentImage.getImage().getHeight());
        } else {
            System.out.println("Image is not loaded yet, cannot create canvas.");
        }
        imagePane = new Pane(currentImage, overlaySimilarity);
    }

    public ResultsApplication(ArrayList<Vertex<SuperPixel>> playerPath, Image I) {
        this(I);
        if (I == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        // Optionally, pass playerPath to MazeProcessor for processing
    }

    private void setButton() {
        similarityButton.setOnAction(e -> {
            if (currentFile != null) {
                mazeProcessor.processMazeImage(currentFile.getPath());
                ArrayList<Vertex<SuperPixel>> userPath = mazeProcessor.getUserPath();
                ArrayList<Vertex<SuperPixel>> systemPath = mazeProcessor.getSystemPath();
                BufferedImage bufferedImage = mazeProcessor.getBufferedImage();
                SimilarityDetector detector = new SimilarityDetector(userPath, systemPath, bufferedImage);
                ArrayList<Vertex<SuperPixel>[]> matchedPairs = detector.findMostSimilarPairs(0.8);
                drawSimilarityLines(overlaySimilarity, matchedPairs);
            } else {
                System.out.println("No image selected.");
            }
        });
    }

    private void setStyleAllign(Pane pane) {
        pane.getStyleClass().add("vbox");
    }

    @Override
    public void start(Stage arg0) {
        VBox controlPane = new VBox();
        HBox imageVBox = new HBox();
        String expText = 
            "The maze is divided into superpixels, represented as vertices in a graph. " +
            "The app compares the user's path (blue) with the system's solution path (green) " +
            "using graph-based similarity (vertex overlap or adjacency), color, and position. " +
            "Red lines connect matching vertices between the paths.";
        Label explain = new Label(expText);
        explain.setWrapText(true);

        VBox explanation = new VBox(explain);
        explanation.setAlignment(Pos.TOP_LEFT);
        explanation.setMaxWidth(500);

        imageVBox.getChildren().addAll(explanation, imagePane);
        setButton();
        controlPane.getChildren().addAll(imageVBox, similarityButton);
        setStyleAllign(controlPane);

        arg0.getIcons().add(new Image("/utilityImg/brain.png"));
        arg0.setScene(new Scene(controlPane));
        arg0.setTitle("Graph-Based Similarity Detection");
        arg0.show();
    }

    private void drawSimilarityLines(Canvas canvas, ArrayList<Vertex<SuperPixel>[]> matchedPairs) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(javafx.scene.paint.Color.RED);
        gc.setLineWidth(1);

        for (Vertex<SuperPixel>[] pair : matchedPairs) {
            SuperPixel userSp = pair[0].GetElement();
            SuperPixel systemSp = pair[1].GetElement();
            double x1 = userSp.getAvgPixelXPos();
            double y1 = userSp.getyAvgPixelYPos();
            double x2 = systemSp.getAvgPixelXPos();
            double y2 = systemSp.getyAvgPixelYPos();
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    public void startWin() {
        Platform.runLater(() -> start(new Stage()));
    }
}
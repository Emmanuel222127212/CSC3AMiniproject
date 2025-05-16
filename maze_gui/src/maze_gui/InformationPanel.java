package maze_gui;

import java.awt.RenderingHints.Key;
import java.awt.desktop.AboutEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.ArrayList;
import java.util.Arrays;

import gamestuff.AttemptRecord;
import gamestuff.Game;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InformationPanel extends VBox{

	//contents
	private Game game;
	

	
	private Label name = new Label();
	private Label menu = new Label(this.game.menu);
	private Label attemptProgress = new Label();
	private TextArea attempts = new TextArea();
	private Button selectButton = new Button("Choose Maze");
	private Button attemptButton = new Button("attempt");
	private Button resetButton = new Button("reset");
	private Button resultButton = new Button("results");
	private VBox menuBox = new VBox();
	private Canvas canvas;
	private GraphicsContext gc;
	
	public InformationPanel(Game game) {
		this.game = game;
		name.setText(String.format("Congnitive Test for : %s",this.game.getplayerName()));
		this.setMaxSize(500,1000);
		setSpacing(10);
		setButtons();
		setPane();
	}
	
	public void change() {
		name.setText(String.format("Congnitive Test for : %s",this.game.getplayerName()));
	}
	
	
	private void setMenu() {
		
	}

	
	private void DisplayAttempts() {
		attempts.clear();
		
		for(AttemptRecord attempt : game.getplayerRecords()) {
			attempts.appendText(attempt.toString());
		}
	}
	
	

	
	public void setAttemptButton(Scene s){
		//attempt button
				attemptButton.setOnAction( e -> {
					changeProgress();
					game.startRound(attempts,s);
					
					
				});
	}
	
	private void changeProgress() {
		attemptProgress.setText(game.showProgress());
		
	}
	
	private void setMenuBox(){
		this.menuBox.getChildren().addAll(name,menu,attemptProgress);
		this.menuBox.setPadding( new Insets(5,5,5,5));
	}
	
	
	public void SelectImage(Pane s) {
		//select image button
				selectButton.setOnAction(e -> {
					FileChooser pickFile = new FileChooser();
					pickFile.setInitialDirectory(new File("./data"));
					pickFile.setTitle("Pick Maze");
					
					Stage stage = new Stage();
					
					File chosenFile = pickFile.showOpenDialog(stage);
					
					if(chosenFile != null && chosenFile.getAbsolutePath().endsWith(".png")) {
						Image image = new Image(chosenFile.toURI().toString());
						
						System.out.println("image set");	
						canvas = new Canvas(image.getWidth(),image.getHeight());
						s.getChildren().add(canvas);
						gc = canvas.getGraphicsContext2D();
						gc.drawImage(image, 0,0,canvas.getWidth(),canvas.getHeight());
						
							game.setGraph(chosenFile,image.getWidth(),image.getHeight(),gc);	
							
					
						
					}else {
						
						System.err.println("file type not image or not found");
					}
				});
	}
	private void setButtons() {
		//reset button
		resetButton.setOnAction(e -> {
			
		});
		
		//result button 
		resultButton.setOnAction(e -> {
			
		});
		
		
	}

	private void setTextArea() {
		this.attempts.setDisable(true);
		int width = 1000;
		this.attempts.setMaxSize(width, width * 100 );
		
		
	}
	
	private void padding(int top,int right, int bottom, int left) {
		this.setPadding(new Insets(top,right,bottom,left));
	}
	
	
	private void setPane() {
		padding(10, 10, 10, 10);
		setTextArea();
		setMenuBox();

		getChildren().addAll(menuBox,attempts,selectButton,attemptButton,resetButton,resultButton);
	}
	
}

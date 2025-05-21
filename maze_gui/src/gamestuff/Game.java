package gamestuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import GraphADT.Edge;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;



public class Game {
	
	int keyCount = 0;
	private User player;

	
	//graph stuff
	private Image currentImage;

	private Image characterImage = new Image("/utilityImg/ujLogo.png");
	private boolean inRound = false;
	private GraphADT.Graph<SuperPixel> currentGraph;
	private GraphicsContext gc;
	private Vertex<SuperPixel> currentVertex;
	
	
	public static String menu = 
		    "Press 'Select Maze' to choose a maze.\n" +
		    "Press 'Attempt' to keep attempting the maze.\n" +
		    "Press 'Results' to view cognitive ability results.\n" +
		    "Press 'Reset' to try a new maze.\n\n" +
		    "Use arrow keys to move.\n" +
		    "Press Enter or Escape to finish the maze.";

	private static String[] moveList = {"UP","DOWN","LEFT","RIGHT"};
	
	public Game() {};
	
	public  void setPlayer(String username) {
		this.player.setName(username);
	}
	
	public Game(String username) {
		this.player = new User(username);	}
	
	public String getplayerName() {
		return player.getName();
	}
	
	public GraphADT.ArrayList<AttemptRecord> getplayerRecords(){
		return player.getAttempts();
	}
	
	
	
	/**
	 * @return the currentImage
	 */
	public Image getCurrentImage() {
		return currentImage;
	}

	/**
	 * @param currentImage the currentImage to set
	 */
	public void setCurrentImage(Image currentImage) {
		this.currentImage = currentImage;
	}

	public int numberOfAttempts() {
		return player.numberOfAttempts();
	}
	
	public void startRound(TextArea t,Scene s) {
		inRound = true;
		AttemptRecord pRecord = new AttemptRecord(player.numberOfAttempts());
		
		 player.setCurrentAttempt(pRecord);
		 player.getCurrentAttempt().addToPath(currentGraph.getStartVertex());
		System.out.println("listening..");
		keyCount = 0;
		s.getRoot().requestFocus();
		s.setOnKeyPressed(event ->{
		
			handleKeyPressed(s,event, t);
		});
		
	}
	
	
	
	public void setGraph(File i,double width, double height, GraphicsContext gc,Image image) {
	
				this.gc = gc;
				this.currentImage = image;
				this.currentGraph = new GraphADT.Graph<SuperPixel>(i.getPath());
				System.out.println("graph made");
				player.resetAttempts();
				gc.drawImage(image, 0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
				currentVertex = currentGraph.getStartVertex();
				int startx = currentGraph.getStartVertex().GetElement().getAvgPixelXPos();
				int starty = currentGraph.getStartVertex().GetElement().getyAvgPixelYPos();
				
				player.setCurrentx(startx);
				player.setCurrenty(starty);
				gc.drawImage(new Image("/utilityImg/location.png"),startx,starty,10,10);	
	}
	
	public GraphADT.Graph<SuperPixel> getgraph(){
		return this.currentGraph;
	}
	
	
	public String showProgress() {
		 
		return String.format("On Attempt : %d ", player.numberOfAttempts());	
	}
	
	
	public void reset() {
		this.player.setAttempts(new GraphADT.ArrayList<AttemptRecord>());;
	}
	
	public String DisplayAttempts(int numberOfAttempts) {
		
		if(numberOfAttempts == this.player.numberOfAttempts()-1-1) {
			return this.player.getAttempts().get(numberOfAttempts).toString();
		}
		
		return this.player.getAttempts().get(numberOfAttempts).toString()+ DisplayAttempts(++numberOfAttempts);
		
		
	}
	
	
	public void moveToVertex(String move) {
		boolean moved = false;
		
		System.out.println(String.format(" the current x : %d \n the current y : %d",player.getCurrentx(), player.getCurrenty()));
			
			
			for(Edge<SuperPixel> edge: currentVertex.EdgeList()) {
				
				boolean foundNextVertex = false;
				 Vertex<SuperPixel> nextVert ;
				 
				 if(currentVertex == currentGraph.getStartVertex()) {
					 System.out.println("at start");
				 }
				 
				 if (edge.getVertFrom() == currentVertex) {
					 nextVert = edge.getVertTO();
		            } else {
		            	nextVert = edge.getVertFrom();
		            }
				 System.out.println(String.format(" the next x : %d \n the next y : %d",nextVert.GetElement().getAvgPixelXPos() ,nextVert.GetElement().getyAvgPixelYPos()));
				switch (move) {
				case "LEFT": {
					if(nextVert.GetElement().getAvgPixelXPos() < player.getCurrentx()) {
						foundNextVertex = true;
						System.out.println("to left");
					}
					break;
				}
				case "RIGHT": {
					if(nextVert.GetElement().getAvgPixelXPos() > player.getCurrentx()) {
						foundNextVertex = true;
						System.out.println("to right");
					}
					break;
				}
				case "UP": {
					if(nextVert.GetElement().getyAvgPixelYPos() < player.getCurrenty()) {
						foundNextVertex = true;
						System.out.println("to up");
					}
					break;
				}
				case "DOWN": {
					if(nextVert.GetElement().getyAvgPixelYPos() > player.getCurrenty()) {
						foundNextVertex = true;
						System.out.println("to down");
					}
					break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + move);
				}
				
				if(foundNextVertex && nextVert.GetElement().GetType() == 1) {
					
					
					currentVertex = nextVert;
					System.out.println("found nextvert");
							moved= true;
							player.setCurrentx(nextVert.GetElement().getAvgPixelXPos() );
							player.setCurrenty(nextVert.GetElement().getyAvgPixelYPos());
							//draw movement will be done like this
							//gc.fillRect(player.getCurrentx(), player.getCurrenty(), 10, 10);
							
							drawMovement(player.getCurrentx(), player.getCurrenty());
							player.getCurrentAttempt().addToPath(nextVert);
							
							
							GraphADT.ArrayList<Vertex<SuperPixel>> playerPath = player.getCurrentAttempt().getAttemptPath();
							
							//checking if player is done
						if(playerPath.get(playerPath.size()-1).GetElement().compare(currentGraph.getEndVertex().GetElement()) == 0) {
							player.getCurrentAttempt().CompleteAttempt();
							inRound = false;
							gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
							gc.drawImage(currentImage, 0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
							currentVertex = currentGraph.getStartVertex();
							player.setCurrentx(currentVertex.GetElement().getAvgPixelXPos());
							player.setCurrenty(currentVertex.GetElement().getyAvgPixelYPos());
							gc.drawImage(new Image("/utilityImg/location.png"),player.getCurrentx(),player.getCurrenty(),10,10);
						}
						
						
				}else {
					System.out.println("not next");
					System.out.println(" the type is " +  nextVert.GetElement().GetType());
				}
			}
			
			
			if(moved) {
				System.out.println("move");
				return;
			}
		
			
			System.out.println("none found");
		
		
		
		
	}
	
	
	private void drawMovement(int x , int y) {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.drawImage(currentImage, 0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
		gc.drawImage(characterImage,x, y, 15, 15);
	}
	
	public void reset(TextArea t) {
		t.clear();
		
		t.requestFocus();
		System.out.println(DisplayAttempts(0));
		inRound = false;
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		currentVertex = currentGraph.getStartVertex();
		player.setCurrentx(currentVertex.GetElement().getAvgPixelXPos());
		player.setCurrenty(currentVertex.GetElement().getyAvgPixelYPos());
		
	}
	
	public void moveInGame(String move, TextArea t,javafx.scene.input.KeyEvent event) {
		
		
		
		if((!move.equals("ESCAPE") && !move.equals("ENTER")) &&  Arrays.asList(moveList).contains(move)) {
			keyCount++;
			//t.appendText("\nKey count: " + keyCount);
			player.getCurrentAttempt().addMove(move);
		//	t.appendText("\nkey pressed :  " + event.getCode());
			gc.setFill(Color.RED);
			moveToVertex(move);
			
			
			
			
		}else if(move.equals("ESCAPE") || move.equals("ENTER") || player.getCurrentAttempt().isComplete()) {
			t.clear();
			player.getCurrentAttempt().endAttempt();
			player.insertAttempt(player.getCurrentAttempt());
			t.appendText(DisplayAttempts(0));
			System.out.println(DisplayAttempts(0));
			currentVertex = currentGraph.getStartVertex();
			player.setCurrentx(currentVertex.GetElement().getAvgPixelXPos());
			player.setCurrenty(currentVertex.GetElement().getyAvgPixelYPos());
			gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
			gc.drawImage(currentImage, 0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
			gc.drawImage(new Image("/utilityImg/location.png"),player.getCurrentx(),player.getCurrenty(),10,10);
			//attempts.appendText(pRecord);
			

		}else {

			t.appendText(String.format("\n %s is invalid", move));
		}
	}
	
	public void handleKeyPressed(Scene s,javafx.scene.input.KeyEvent event,TextArea t) {
	if(inRound) {
		String move = event.getCode().toString();
		moveInGame(move, t, event);
	}
	
	}
	
}

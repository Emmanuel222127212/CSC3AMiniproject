package gamestuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
	
	
	private GraphADT.Graph<SuperPixel> currentGraph;
	private GraphicsContext gc;
	
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
	
	public ArrayList<AttemptRecord> getplayerRecords(){
		return player.getAttempts();
	}
	
	public int numberOfAttempts() {
		return player.numberOfAttempts();
	}
	
	public void startRound(TextArea t,Scene s) {
		AttemptRecord pRecord = new AttemptRecord(player.numberOfAttempts());
		 player.setCurrentAttempt(pRecord);
		
		System.out.println("listening..");
		keyCount = 0;
		s.getRoot().requestFocus();
		s.setOnKeyPressed(event ->{
		
			handleKeyPressed(s,event, t);
		});
		
	}
	
	
	
	public void setGraph(File i,double width, double height, GraphicsContext gc) {
	
				this.gc = gc;
		
				this.currentGraph = new GraphADT.Graph<SuperPixel>(i.getPath());
				System.out.println("graph made");
			
				int startx = currentGraph.getStartVertex().GetElement().getAvgPixelXPos();
				int starty = currentGraph.getStartVertex().GetElement().getyAvgPixelYPos();
				
				player.setCurrentx(startx);
				player.setCurrenty(starty);
				gc.fillRect(startx,starty,10,10);	
	}
	
	public GraphADT.Graph<SuperPixel> getgraph(){
		return this.currentGraph;
	}
	
	
	public String showProgress() {
		 
		return String.format("On Attempt : %d ", player.numberOfAttempts());	
	}
	
	
	public void reset() {
		this.player.getAttempts().clear();
	}
	
	public String DisplayAttempts(int numberOfAttempts) {
		
		if(numberOfAttempts == this.player.numberOfAttempts()-1-1) {
			return this.player.getAttempts().get(numberOfAttempts).toString();
		}
		
		return this.player.getAttempts().get(numberOfAttempts).toString()+ DisplayAttempts(++numberOfAttempts);
		
		
	}
	
	
	private boolean withiInBounds(int number, int higherBound, int lowerBound) {
		return (number < higherBound) && (number > lowerBound);
	}
	
	private boolean canMoveThere(int x, int y) {
		for(Vertex<SuperPixel> sp : currentGraph.getVertices()) {
			SuperPixel currentSuperPixel = sp.GetElement();
			int halfway = currentSuperPixel.SuperPixelSize()/2;
			 boolean xIsGood = withiInBounds(x, currentSuperPixel.getAvgPixelXPos()+halfway, currentSuperPixel.getAvgPixelXPos() - halfway);
					
			 boolean yIsGood = withiInBounds(y, currentSuperPixel.getyAvgPixelYPos()+halfway, currentSuperPixel.getyAvgPixelYPos()-halfway);
			 if(xIsGood && yIsGood) {
				//check type of sp
				 if(currentSuperPixel.GetType()==2) 
					 return true;
				 
				 return false;
			}
		}
		return false;
	}
	
	private void keepMovingFrom(int x, int y) {
		while(canMoveThere(x, y)) {
			
			x++;
			y++;
		}
	}
	
	public void moveInGame(String move, TextArea t,javafx.scene.input.KeyEvent event) {
		if((!move.equals("ESCAPE") && !move.equals("ENTER")) &&  Arrays.asList(moveList).contains(move)) {
			keyCount++;
			//t.appendText("\nKey count: " + keyCount);
			player.getCurrentAttempt().addMove(move);
		//	t.appendText("\nkey pressed :  " + event.getCode());
			
			int nextX = player.getCurrentx();
			int nextY = player.getCurrenty();
			System.out.println("moving " + move);
			switch (move) {
	case "LEFT": {
				nextX--;
				break;
			}
	case "RIGHT": {
				nextX++;
		break;
	}
	case "UP": {
				nextY++;
		break;
	}
	case "DOWN": {
				nextY--;
		break;
	}
			default:
				throw new IllegalArgumentException("Unexpected value: " + move);
			}
			
			
			int differenceX = nextX-player.getCurrentx();
			int differenceY = nextY-player.getCurrenty();
			
			//if the next path is not a wall
			
			
		}else if(move.equals("ESCAPE") || move.equals("ENTER")) {
			player.setCurrentx(0);
			player.setCurrenty(0);
			player.getCurrentAttempt().endAttempt();
			player.insertAttempt(player.getCurrentAttempt());
			t.clear();
			t.appendText(DisplayAttempts(0));
			System.out.println(DisplayAttempts(0));
			t.requestFocus();
			//attempts.appendText(pRecord);
			

		}else {

			t.appendText(String.format("\n %s is invalid", move));
		}
	}
	
	public void handleKeyPressed(Scene s,javafx.scene.input.KeyEvent event,TextArea t) {
		String move = event.getCode().toString();
		moveInGame(move, t, event);
	
	}
	
}

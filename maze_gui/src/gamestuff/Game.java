package gamestuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.graphstream.graph.Graph;

import GraphADT.SuperPixel;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

public class Game {
	
	int keyCount = 0;
	private User player;
	private Canvas canvas;
	
	
	
	public static String menu = "press select maze \npress attempt to keep attempting the maze \npress results to see cognitive aiblity"
			+ "\n press reset to try a new maze"
			+ "\nUSE ARROW KEYS TO MOVE \nPRESS ENTER OR ESACAP \nFINSISH MAZE";
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
	
	private GraphADT.Graph<SuperPixel> currentGraph;
	
	public void setGraph(File i) {
		this.currentGraph = new GraphADT.Graph<SuperPixel>(i.getPath());
		
		System.out.println("graph made");
	}
	
	public String showProgress() {
		 
		return String.format("on Attempt : %d ", player.numberOfAttempts());	
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
	
	public void moveInGame(String move, TextArea t,javafx.scene.input.KeyEvent event) {
		if((!move.equals("ESCAPE") && !move.equals("ENTER")) &&  Arrays.asList(moveList).contains(move)) {
			keyCount++;
			//t.appendText("\nKey count: " + keyCount);
			player.getCurrentAttempt().addMove(move);
		//	t.appendText("\nkey pressed :  " + event.getCode());
			
			
		}else if(move.equals("ESCAPE") || move.equals("ENTER")) {
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

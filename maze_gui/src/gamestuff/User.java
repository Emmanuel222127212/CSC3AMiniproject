package gamestuff;

import java.util.ArrayList;

public class User {
	private String name;
	private AttemptRecord currentAttempt;
	private ArrayList<AttemptRecord> attempts = new ArrayList<AttemptRecord>();
	
	
	
	/**
	 * @return the currentAttempt
	 */
	public AttemptRecord getCurrentAttempt() {
		return currentAttempt;
	}
	/**
	 * @param currentAttempt the currentAttempt to set
	 */
	public void setCurrentAttempt(AttemptRecord currentAttempt) {
		this.currentAttempt = currentAttempt;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the attempts
	 */
	public ArrayList<AttemptRecord> getAttempts() {
		return attempts;
	}
	/**
	 * @param attempts the attempts to set
	 */
	public void setAttempts(ArrayList<AttemptRecord> attempts) {
		this.attempts = attempts;
	}
	
	public User(String name) {
		this.name = name;
	}
	
	public void insertAttempt(AttemptRecord attempt) {
		
		this.attempts.add(attempt);
	
	}
	
	public int numberOfAttempts() {
		return this.attempts.size() + 1;
	}
	
	public AttemptRecord getBest() {
		return this.attempts.get(0);
	}
	
	public AttemptRecord getWorst() {
		return this.attempts.get(this.attempts.size() -1);
	}
}

package gamestuff;



public class User {
	private String name;
	private int bestindex= 0;
	private AttemptRecord currentAttempt;
	private GraphADT.ArrayList<AttemptRecord> attempts = new GraphADT.ArrayList<AttemptRecord>();
	int currentx = 0;
	int currenty =0;
	
	
	public AttemptRecord getBest() {
		return attempts.get(bestindex);
	}
	
	/**
	 * @return the currentx
	 */
	public int getCurrentx() {
		return currentx;
	}
	/**
	 * @param currentx the currentx to set
	 */
	public void setCurrentx(int currentx) {
		this.currentx = currentx;
	}
	/**
	 * @return the currenty
	 */
	public int getCurrenty() {
		return currenty;
	}
	/**
	 * @param currenty the currenty to set
	 */
	public void setCurrenty(int currenty) {
		this.currenty = currenty;
	}
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
	public GraphADT.ArrayList<AttemptRecord> getAttempts() {
		return attempts;
	}
	/**
	 * @param attempts the attempts to set
	 */
	public void setAttempts(GraphADT.ArrayList<AttemptRecord> attempts) {
		this.attempts = attempts;
	}
	
	public User(String name) {
		this.name = name;
	}
	
	public void insertAttempt(AttemptRecord attempt) {
		
		this.attempts.add(attempt);
		findBestTime();
	
	}
	
	public void resetAttempts() {
		setAttempts(new GraphADT.ArrayList<AttemptRecord>());
		bestindex =0;
		currentAttempt = null;
	}
	public void findBestTime() {
		AttemptRecord min = this.attempts.get(0);
		bestindex = 0;
		if(this.attempts.size() >1) {
			for(int i =1;i < this.attempts.size();i++) {
				if(min.getAttemptReport() >= this.attempts.get(i).getAttemptReport()) {
					min = this.attempts.get(i);
					bestindex = i;
				}
			}
		}
		
	}
	
	
	public int numberOfAttempts() {
		return this.attempts.size() + 1;
	}
	
	
	public AttemptRecord getWorst() {
		return this.attempts.get(this.attempts.size() -1);
	}
}

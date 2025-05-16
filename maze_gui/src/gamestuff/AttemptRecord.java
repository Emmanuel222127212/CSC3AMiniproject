package gamestuff;

import java.time.LocalTime;
import java.util.ArrayList;

public class AttemptRecord {
	 private int AttemptNum;
	 private int KeyCount = 0;
	 private LocalTime starTime;
	 private LocalTime endTime;
	 private static int HOUR = 0;
	 private static int MINUTE = 1;
	 private static int SECOND = 2;
	 private int[] performanceTime = new int[3];
	 private ArrayList<String> Moves  = new ArrayList<String>();
	 
	 public AttemptRecord(int attempnum) {
		 this.AttemptNum = attempnum;
		 this.starTime = LocalTime.now();
	 }
	 
	 public void addMove(String move) {
		 this.Moves.add(move);
	 }
	 
	 public ArrayList<String>  getMoves(){
		 return this.Moves;
	 }
	 
	/**
	 * @return the keyCount
	 */
	public int getKeyCount() {
		return KeyCount;
	}
	/**
	 * @param keyCount the keyCount to set
	 */
	public void setKeyCount(int keyCount) {
		KeyCount = keyCount;
	}
	/**
	 * @return the starTime
	 */
	public LocalTime getStarTime() {
		return starTime;
	}
	/**
	 * @param starTime the starTime to set
	 */
	public void setStarTime(LocalTime starTime) {
		this.starTime = starTime;
	}
	/**
	 * @return the endTime
	 */
	public LocalTime getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
		if(this.endTime != null && this.starTime!= null) {
			setPerformanceTime();
		}
	}
	/**
	 * @return the performanceTime
	 */
	public int[] getPerformanceTime() {
		return performanceTime;
	}
	/**
	 * @param performanceTime the performanceTime to set
	 */
	public void setPerformanceTime() {
		performanceTime[HOUR] = endTime.getHour() - starTime.getHour();
		performanceTime[MINUTE] = endTime.getMinute() - starTime.getMinute();
		performanceTime[SECOND] = endTime.getSecond() - starTime.getSecond();		
	}
	
	public String format(int attempt, int move, LocalTime start, LocalTime end, int[] performance) {
		return String.format("\n[Attempt number : %d, MoveCount : %d, start : %d:%d:%d, end : %d:%d:%d, performance : %dh%dm%ds]", attempt,move,start.getHour()
				, start.getMinute(), start.getSecond(), end.getHour(),end.getMinute(), end.getSecond(),performance[HOUR],performance[MINUTE], performance[SECOND]);
		
	}
	
	
	
	public int getAttemptReport() {
		return performanceTime[HOUR] + performanceTime[MINUTE] + performanceTime[SECOND];
	}
	
	@Override 
	public String toString() {
		return format(AttemptNum, Moves.size(), starTime, endTime, performanceTime);
	}
	
	
	public void endAttempt() {
		setEndTime(LocalTime.now());
	}

	/**
	 * @return the attemptNum
	 */
	public int getAttemptNum() {
		return AttemptNum;
	}

	/**
	 * @param attemptNum the attemptNum to set
	 */
	public void setAttemptNum(int attemptNum) {
		AttemptNum = attemptNum;
	}
	
	
	 
	 
	 
}

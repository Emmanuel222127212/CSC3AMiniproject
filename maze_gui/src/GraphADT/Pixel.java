package GraphADT;
public class Pixel {

	
	private int XPos;
	private int YPos;
	private int intensity;
	
	
	
	public Pixel(int xPos, int yPos, int intensity) {
		super();
		XPos = xPos;
		YPos = yPos;
		this.intensity = intensity;
	}
	public int getXPos() {
		return XPos;
	}
	public void setXPos(int xPos) {
		XPos = xPos;
	}
	public int getYPos() {
		return YPos;
	}
	public void setYPos(int yPos) {
		YPos = yPos;
	}
	public int getIntensity() {
		return intensity;
	}
	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}
	
}

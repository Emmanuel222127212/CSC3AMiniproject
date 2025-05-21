package GraphADT;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SuperPixel {

	//protected comparator
	protected class SPComparator implements Comparator<SuperPixel>{

		private boolean isClose(SuperPixel o1, SuperPixel o2) {
			return (Math.abs(o1.getAvgPixelXPos() - o2.getAvgPixelXPos()) <= 10 && o1.getyAvgPixelYPos() - o2.getyAvgPixelYPos() <= 10);
		}
		
		@Override
		public int compare(SuperPixel o1, SuperPixel o2) {
			// TODO Auto-generated method stub
			if(isClose(o1, o2))
			return 0;
			
			if(o1.getAvgPixelXPos() >= o2.getAvgPixelXPos() || o1.getAvgPixelXPos() >= o2.getAvgPixelXPos())
				return 1;
			
			return -1;
		}
		
	}
	
	
	private SPComparator comparator = new SPComparator();
	
	
	private int AvgPixelXPos;
	private int AvgPixelYPos;
	private int SuperPixelID;
	private int Type; // 1 for wall
						// 2 for path

	private List<Pixel> PixelList;

	private static int BaseId = 100;
	private int SuperPixelSize;

	public SuperPixel() {
		PixelList = new ArrayList<Pixel>();
		BaseId += 1;
		SuperPixelID = BaseId;
		SuperPixelSize = 0;
	}

	public void AddPixel(Pixel P) {
		this.PixelList.add(P);
		this.SuperPixelSize++;
	}

	public int getId() {
		return SuperPixelID;

	}

	public int SuperPixelSize() {
		return PixelList.size();
	}

	public int getAvgPixelXPos() {
		return AvgPixelXPos;
	}

	public void setAvgPixelXPos(int xPos) {
		this.AvgPixelYPos = xPos;
	}

	public int getyAvgPixelYPos() {
		return AvgPixelYPos;
	}

	public void setAvgPixelYPos(int yPos) {
		this.AvgPixelYPos = yPos;
	}

	public List<Pixel> getAllPixels() {
		return this.PixelList;
	}

	public int GetType() {
		return this.Type;
	}

	public void CalculateCetroids(double xscale, double yscale) {
		int xSum = 0;
		int ySum = 0;

		for (Pixel pixel : PixelList) {
			xSum += pixel.getXPos();
			ySum += pixel.getYPos();
		}

		AvgPixelXPos = (int) (xscale*(xSum / SuperPixelSize));
		AvgPixelYPos = (int) (yscale* (ySum / SuperPixelSize));

		DetermineType();

	}

	private void DetermineType() {

		int intensitySum = 0;
		int Avgintensity = 0;

		for (Pixel pixel : PixelList) {
			intensitySum += pixel.getIntensity();
		}
		Avgintensity = intensitySum / SuperPixelSize;

		if (Avgintensity > 100) {
			Type = 1; // Paths
		} else {
			Type = 2; // Walls
		}
		
		

	}
	
	
	//Comparing
	
	public int compare(SuperPixel sp) {
		return comparator.compare(this, sp);
	}


}

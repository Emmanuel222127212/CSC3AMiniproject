package GraphADT;

import java.awt.image.BufferedImage;
import java.util.List;

public class SimilarityDetector 
{
    private ArrayList<SuperPixel> superpixels; // List of SuperPixel objects
    private BufferedImage image;
    private double maxDistance;

    
    //version 2
    private ArrayList<SuperPixel> userPixels;
    private ArrayList<SuperPixel> systemPixels;
	
    
    public SimilarityDetector(ArrayList<SuperPixel> superpixels, BufferedImage image) 
    {
        this.superpixels = superpixels;
        this.image = image;
        this.maxDistance = Math.sqrt(Math.pow(image.getWidth() - 1, 2) + Math.pow(image.getHeight() - 1, 2)) / 6;
    }
    
    public SimilarityDetector(ArrayList<SuperPixel> userPx,ArrayList<SuperPixel> systemPx, BufferedImage image) 
    {
        this.userPixels = userPx;
        this.systemPixels = systemPx;
        this.image = image;
        this.maxDistance = Math.sqrt(Math.pow(image.getWidth() - 1, 2) + Math.pow(image.getHeight() - 1, 2)) / 6;
    }

    

	public double computeSimilarity(int userID, int systemID) 
    {
        /*if (id1 < 0 || id1 >= superpixels.size() || id2 < 0 || id2 >= superpixels.size()) {
            return 0.0; // Invalid ID
        }*/
		
		if (userID < 0 || userID >= userPixels.size() || systemID < 0 || systemID >= systemPixels.size()) {
	        return 0.0;
	    }

        SuperPixel sp1 = superpixels.get(userID);
        //for system
        SuperPixel sp2 = superpixels.get(systemID);

        // Check if both are paths (Type 1) for your green image
        if (sp1.GetType() != 1 || sp2.GetType() != 1)
        {
            return 0.0; // Only compare path superpixels
        }

        double[] avgUserRGB= getAverageRGB(sp1);
        double[] avgSystemRGB = getAverageRGB(sp2);
        
        double colorDiff = Math.sqrt(
                Math.pow(avgUserRGB[0] - avgSystemRGB[0], 2) +
                Math.pow(avgUserRGB[1] - avgSystemRGB[1], 2) +
                Math.pow(avgUserRGB[2] - avgSystemRGB[2], 2)
            );
        
        
        double colorSimilarity = Math.max(0, 1 - colorDiff / 441.67); // 441.67 = sqrt(3*255^2)

        // Spatial similarity using centroids
        int x1 = sp1.getAvgPixelXPos();
        int y1 = sp1.getyAvgPixelYPos();
        int x2 = sp2.getAvgPixelXPos();
        int y2 = sp2.getyAvgPixelYPos();
        //the euclidean distance
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double spatialSimilarity = Math.max(0, 1 - distance / maxDistance);

        return 0.4 * colorSimilarity + 0.6 * spatialSimilarity;
        
      /*  // Color similarity (all green, so 1.0)
        double[] avgRGB1 = getAverageRGB(sp1);
        double[] avgRGB2 = getAverageRGB(sp2);
        double colorDiff = Math.sqrt(
            Math.pow(avgRGB1[0] - avgRGB2[0], 2) +
            Math.pow(avgRGB1[1] - avgRGB2[1], 2) +
            Math.pow(avgRGB1[2] - avgRGB2[2], 2)
        );
        double colorSimilarity = Math.max(0, 1 - colorDiff / 441.67); // 441.67 = sqrt(3*255^2)

        // Spatial similarity using centroids
        int x1 = sp1.getAvgPixelXPos();
        int y1 = sp1.getyAvgPixelYPos();
        int x2 = sp2.getAvgPixelXPos();
        int y2 = sp2.getyAvgPixelYPos();
        //the euclidean distance
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double spatialSimilarity = Math.max(0, 1 - distance / maxDistance);

        return 0.5 * colorSimilarity + 0.5 * spatialSimilarity;*/
        
        
    }

/*
 private double[] getAverageRGB(int id) 
    {
    	ArrayList<int[]> pixels = superpixels.get(id);
        double rSum = 0, gSum = 0, bSum = 0;
        int count = pixels.size();
        for (int[] pixel : pixels) 
        {
            int rgb = image.getRGB(pixel[1], pixel[0]);
            rSum += (rgb >> 16) & 0xff;
            gSum += (rgb >> 8) & 0xff;
            bSum += rgb & 0xff;
        }
        return new double[]{count > 0 ? rSum / count : 0, count > 0 ? gSum / count : 0, count > 0 ? bSum / count : 0};
    	
    	
    }
*/
    private double[] getAverageRGB(SuperPixel sp)
    {
        List<Pixel> pixels = sp.getAllPixels();
        //getting the updated list of pixels and calculate the average of the RGB values of the pixels
        double rSum = 0, gSum = 0, bSum = 0;
        int count = pixels.size();
        for (Pixel pixel : pixels) {
            int rgb = image.getRGB(pixel.getXPos(), pixel.getYPos());
            rSum += (rgb >> 16) & 0xff;
            gSum += (rgb >> 8) & 0xff;
            bSum += rgb & 0xff;
        }
        return new double[]
        {
            count > 0 ? rSum / count : 0,
            count > 0 ? gSum / count : 0,
            count > 0 ? bSum / count : 0
        };
    }

	public ArrayList<SuperPixel[]> findMostSimilarPairs(double threshold) 
	{
		
		ArrayList<SuperPixel[]> matchedPairs = new ArrayList<>();

	    for (int i = 0; i < userPixels.size(); i++) {
	        double maxSim = 0;
	        int bestMatchIdx = -1;

	        for (int j = 0; j < systemPixels.size(); j++) 
	        {
	            double sim = computeSimilarity(i, j);
	            if (sim > maxSim) 
	            {
	                maxSim = sim;
	                bestMatchIdx = j;
	            }
	        }

	        if (maxSim >= threshold && bestMatchIdx != -1) 
	        {
	            matchedPairs.add(new SuperPixel[] 
	            {
	                userPixels.get(i),
	                systemPixels.get(bestMatchIdx)
	            });
	        }
	    }

	    return matchedPairs;
		
	}
}
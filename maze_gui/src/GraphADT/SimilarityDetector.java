
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

public class SimilarityDetector 
{
    private Map<Integer, ArrayList<int[]>> superpixels;
    private Map<Integer, int[]> centroids;
    private BufferedImage image;
    private double maxDistance;

    public SimilarityDetector(Map<Integer, ArrayList<int[]>> superpixels, Map<Integer, int[]> centroids, BufferedImage image) {
        this.superpixels = superpixels;
        this.centroids = centroids;
        this.image = image;
        this.maxDistance = Math.sqrt(Math.pow(image.getWidth() - 1, 2) + Math.pow(image.getHeight() - 1, 2)) / 6; // 1/6 of diagonal
    }

    public double computeSimilarity(int id1, int id2)
    {
        double[] avgRGB1 = getAverageRGB(id1);
        double[] avgRGB2 = getAverageRGB(id2);
        double colorDiff = Math.sqrt(
            Math.pow(avgRGB1[0] - avgRGB2[0], 2) +
            Math.pow(avgRGB1[1] - avgRGB2[1], 2) +
            Math.pow(avgRGB1[2] - avgRGB2[2], 2)
        );
        double colorSimilarity = Math.max(0, 1 - colorDiff / 441.67); // Max RGB distance = sqrt(3*255^2)

        int[] c1 = centroids.get(id1);
        int[] c2 = centroids.get(id2);
        double distance = Math.sqrt(
            Math.pow(c1[0] - c2[0], 2) + Math.pow(c1[1] - c2[1], 2)
        );
        double spatialSimilarity = Math.max(0, 1 - distance / maxDistance);

        return 0.7 * colorSimilarity + 0.3 * spatialSimilarity;
    }

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
}

                                                                                                                                                          

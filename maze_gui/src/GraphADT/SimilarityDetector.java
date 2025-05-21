package GraphADT;

import java.awt.image.BufferedImage;
import java.util.List;

public class SimilarityDetector {
    private ArrayList<Vertex<SuperPixel>> userPath;
    private ArrayList<Vertex<SuperPixel>> systemPath;
    private BufferedImage image;
    private double maxDistance;

    public SimilarityDetector(ArrayList<Vertex<SuperPixel>> userPath, ArrayList<Vertex<SuperPixel>> systemPath, BufferedImage image) {
        this.userPath = userPath;
        this.systemPath = systemPath;
        this.image = image;
        this.maxDistance = Math.sqrt(Math.pow(image.getWidth() - 1, 2) + Math.pow(image.getHeight() - 1, 2)) / 6;
    }

    public double computeSimilarity(int userID, int systemID) {
        if (userID < 0 || userID >= userPath.size() || systemID < 0 || systemID >= systemPath.size()) {
            return 0.0;
        }

        Vertex<SuperPixel> v1 = userPath.get(userID);
        Vertex<SuperPixel> v2 = systemPath.get(systemID);
        SuperPixel sp1 = v1.GetElement();
        SuperPixel sp2 = v2.GetElement();

        // Check if both are path superpixels (Type 1)
        if (sp1.GetType() != 1 || sp2.GetType() != 1) {
            return 0.0;
        }

        // Color similarity (optional, retained from original)
        double[] avgUserRGB = getAverageRGB(sp1);
        double[] avgSystemRGB = getAverageRGB(sp2);
        double colorDiff = Math.sqrt(
            Math.pow(avgUserRGB[0] - avgSystemRGB[0], 2) +
            Math.pow(avgUserRGB[1] - avgSystemRGB[1], 2) +
            Math.pow(avgUserRGB[2] - avgSystemRGB[2], 2)
        );
        double colorSimilarity = Math.max(0, 1 - colorDiff / 441.67);

        // Spatial similarity using centroids
        int x1 = sp1.getAvgPixelXPos();
        int y1 = sp1.getyAvgPixelYPos();
        int x2 = sp2.getAvgPixelXPos();
        int y2 = sp2.getyAvgPixelYPos();
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double spatialSimilarity = Math.max(0, 1 - distance / maxDistance);

        // Graph-based similarity: shortest path distance in the graph
        double graphSimilarity = computeGraphSimilarity(v1, v2);

        // Weighted combination: 30% color, 30% spatial, 40% graph
        return 0.3 * colorSimilarity + 0.3 * spatialSimilarity + 0.4 * graphSimilarity;
    }

    private double computeGraphSimilarity(Vertex<SuperPixel> v1, Vertex<SuperPixel> v2) {
        // Simple approach: Check if vertices are the same or adjacent
        if (v1.equals(v2)) {
            return 1.0; // Same vertex
        }
        for (Edge<SuperPixel> edge : v1.EdgeList()) {
            if (edge.getVertFrom().equals(v2) || edge.getVertTO().equals(v2)) {
                return 0.9; // Adjacent vertices
            }
        }
        // Could implement BFS to find shortest path distance, but for simplicity, use spatial proximity
        return 0.0; // Not adjacent
    }

    private double[] getAverageRGB(SuperPixel sp) {
        List<Pixel> pixels = sp.getAllPixels();
        double rSum = 0, gSum = 0, bSum = 0;
        int count = pixels.size();
        for (Pixel pixel : pixels) {
            int rgb = image.getRGB(pixel.getXPos(), pixel.getYPos());
            rSum += (rgb >> 16) & 0xff;
            gSum += (rgb >> 8) & 0xff;
            bSum += rgb & 0xff;
        }
        return new double[] {
            count > 0 ? rSum / count : 0,
            count > 0 ? gSum / count : 0,
            count > 0 ? bSum / count : 0
        };
    }

    public ArrayList<Vertex<SuperPixel>[]> findMostSimilarPairs(double threshold) {
        ArrayList<Vertex<SuperPixel>[]> matchedPairs = new ArrayList<>();

        for (int i = 0; i < userPath.size(); i++) {
            double maxSim = 0;
            int bestMatchIdx = -1;

            for (int j = 0; j < systemPath.size(); j++) {
                double sim = computeSimilarity(i, j);
                if (sim > maxSim) {
                    maxSim = sim;
                    bestMatchIdx = j;
                }
            }

            if (maxSim >= threshold && bestMatchIdx != -1) {
                matchedPairs.add(new Vertex[] {
                    userPath.get(i),
                    systemPath.get(bestMatchIdx)
                });
            }
        }

        return matchedPairs;
    }
}
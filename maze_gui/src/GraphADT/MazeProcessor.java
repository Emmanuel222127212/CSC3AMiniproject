package GraphADT;

import GraphADT.SimilarityDetector;

import GraphADT.SuperPixel;
import GraphADT.Pixel;
import GraphADT.ArrayList;
import java.awt.image.BufferedImage;
//import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MazeProcessor {
    private BufferedImage mazeImage;
    private int width, height;
    
    private ArrayList<SuperPixel> superPixelList;
    private SimilarityDetector similarityDetector;
    private ImageView imageView; // To update the displayed image

    public MazeProcessor(ImageView imageView) {
        this.imageView = imageView;
    }

    public void processMazeImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                System.out.println("Maze image not found: " + imagePath);
                return;
            }
            mazeImage = ImageIO.read(file);
            width = mazeImage.getWidth();
            height = mazeImage.getHeight();

            // Segment into superpixels
            segmentSuperpixels();
            computeSimilaritiesAndVisualize();
            saveVisualizedImage(imagePath);
            displayProcessedImage();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void segmentSuperpixels() {
        superPixelList = new ArrayList<>();
        int gridSize = 5;

        for (int row = 0; row < height; row += gridSize) {
            for (int col = 0; col < width; col += gridSize) {
                SuperPixel sp = new SuperPixel();
                boolean hasPath = false;
                for (int r = row; r < row + gridSize && r < height; r++) {
                    for (int c = col; c < col + gridSize && c < width; c++) {
                        int rgb = mazeImage.getRGB(c, r);
                        int gValue = (rgb >> 8) & 0xff;
                        if (gValue > 200) { // Green channel for green image
                            sp.AddPixel(new Pixel(c, r, gValue)); // Use green channel for intensity
                            hasPath = true;
                        }
                    }
                }
                if (hasPath) {
                    sp.CalculateCetroids(); // Sets centroids and type
                    if (sp.GetType() == 1) { // Only add path superpixels (type 1)
                        superPixelList.add(sp);
                    }
                }
            }
        }
        similarityDetector = new SimilarityDetector(superPixelList, mazeImage);
        System.out.println("Generated " + superPixelList.size() + " superpixels");
    }

    private void computeSimilaritiesAndVisualize() {
        BufferedImage visualization = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = visualization.createGraphics();
        g2d.drawImage(mazeImage, 0, 0, null);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1));

        int similarPairs = 0;
        for (int id1 = 0; id1 < superPixelList.size(); id1++) {
            for (int id2 = id1 + 1; id2 < superPixelList.size(); id2++) {
                double similarity = similarityDetector.computeSimilarity(id1, id2);
                if (similarity > 0.95) {
                    similarPairs++;
                    SuperPixel sp1 = superPixelList.get(id1);
                    SuperPixel sp2 = superPixelList.get(id2);
                    int x1 = sp1.getAvgPixelXPos();
                    int y1 = sp1.getyAvgPixelYPos();
                    int x2 = sp2.getAvgPixelXPos();
                    int y2 = sp2.getyAvgPixelYPos();
                    g2d.drawLine(x1, y1, x2, y2);
                    System.out.println("Similar pair " + id1 + " and " + id2 + " (Similarity: " + String.format("%.3f", similarity) + ")");
                }
            }
        }
        g2d.dispose();
        mazeImage = visualization;
        System.out.println("Found " + similarPairs + " similar superpixel pairs (similarity > 0.95)");
    }

    private void saveVisualizedImage(String inputPath) {
        try {
            String outputPath = inputPath.replaceFirst("(\\.[^.]+)$", "_similarity$1");
            File outputFile = new File(outputPath);
            ImageIO.write(mazeImage, "png", outputFile);
            System.out.println("Similarity visualization saved as: " + outputPath);
        } catch (Exception e) {
            System.out.println("Error saving visualized image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayProcessedImage() {
        Image fxImage = SwingFXUtils.toFXImage(mazeImage, null);
        imageView.setImage(fxImage);
    }
}
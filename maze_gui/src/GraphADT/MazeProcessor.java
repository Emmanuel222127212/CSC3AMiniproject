package GraphADT;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.*;
import java.util.List;

public class MazeProcessor {
    private BufferedImage mazeImage;
    private int width, height;
    private Graph<SuperPixel> graph;
    private ArrayList<Vertex<SuperPixel>> userPath;
    private ArrayList<Vertex<SuperPixel>> systemPath;
    private ImageView imageView;

    public MazeProcessor(ImageView imageView) {
        this.imageView = imageView;
        this.userPath = new ArrayList<>();
        this.systemPath = new ArrayList<>();
    }

    public void processMazeImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                System.out.println("Maze image not found: " + imagePath);
                return;
            }
            System.out.println("Processing image: " + imagePath);
            mazeImage = ImageIO.read(file);
            if (mazeImage == null) {
                System.out.println("Failed to load image: " + imagePath);
                return;
            }
            width = mazeImage.getWidth();
            height = mazeImage.getHeight();
            System.out.println("Image loaded: " + width + "x" + height);

            // Initialize graph
            graph = new Graph<>(imagePath);
            System.out.println("Graph initialized with " + graph.getVertices().size() + " vertices");

            // Get system path
            systemPath = graph.findPath();
            if (systemPath.isEmpty()) {
                System.out.println("No system path found. Check start/end vertices.");
                System.out.println("Start vertex: " + (graph.getStartVertex() != null ? graph.getStartVertex().GetElement().getId() : "null"));
                System.out.println("End vertex: " + (graph.getEndVertex() != null ? graph.getEndVertex().GetElement().getId() : "null"));
                return;
            }
            systemPath = graph.simplifyPath(systemPath);
            System.out.println("System path vertices: " + systemPath.size());

            // Segment user path
            segmentUserPath();
            System.out.println("User path vertices: " + userPath.size());

            // Compute similarities and visualize
            computeSimilaritiesAndVisualize();
            saveVisualizedImage(imagePath);
            displayProcessedImage();
        } catch (Exception e) {
            System.out.println("Error processing maze image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void segmentUserPath() {
        userPath = new ArrayList<>();
        boolean[][] visited = new boolean[height][width];
        int gridSize = 5;

        for (int row = 0; row < height; row += gridSize) {
            for (int col = 0; col < width; col += gridSize) {
                SuperPixel userSp = new SuperPixel();
                for (int r = row; r < row + gridSize && r < height; r++) {
                    for (int c = col; c < col + gridSize && c < width; c++) {
                        if (visited[r][c]) continue;
                        int rgb = mazeImage.getRGB(c, r);
                        int bValue = rgb & 0xff;
                        if (bValue > 150) { // Lowered threshold for user path
                            userSp.AddPixel(new Pixel(c, r, bValue));
                            visited[r][c] = true;
                        }
                    }
                }
                if (userSp.getAllPixels().size() > 0) {
                    userSp.CalculateCetroids();
                    if (userSp.GetType() == 1) {
                        Vertex<SuperPixel> vertex = findVertexForSuperPixel(userSp);
                        if (vertex != null) {
                            userPath.add(vertex);
                        }
                    }
                }
            }
        }
        System.out.println("Segmented user path with " + userPath.size() + " vertices");
    }

    private Vertex<SuperPixel> findVertexForSuperPixel(SuperPixel sp) {
        for (Vertex<SuperPixel> vertex : graph.getVertices()) {
            SuperPixel vertexSp = vertex.GetElement();
            for (Pixel p : sp.getAllPixels()) {
                if (vertexSp.getAllPixels().contains(p)) {
                    return vertex;
                }
            }
        }
        return null;
    }

    private void computeSimilaritiesAndVisualize() {
    	
        if (userPath.isEmpty() || systemPath.isEmpty()) {
            System.out.println("Cannot compute similarities: userPath or systemPath is empty");
            mazeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = mazeImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();
            return;
        }

        BufferedImage visualization = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = visualization.createGraphics();
        g2d.drawImage(mazeImage, 0, 0, null);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1));

        SimilarityDetector detector = new SimilarityDetector(userPath, systemPath, mazeImage);
        ArrayList<Vertex<SuperPixel>[]> matchedPairs = detector.findMostSimilarPairs(0.6); // Lowered threshold
        System.out.println("Matched pairs found: " + matchedPairs.size());

        for (Vertex<SuperPixel>[] pair : matchedPairs) {
            SuperPixel sp1 = pair[0].GetElement();
            SuperPixel sp2 = pair[1].GetElement();
            int x1 = sp1.getAvgPixelXPos();
            int y1 = sp1.getyAvgPixelYPos();
            int x2 = sp2.getAvgPixelXPos();
            int y2 = sp2.getyAvgPixelYPos();
            g2d.drawLine(x1, y1, x2, y2);
            System.out.println("Drawing line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
        }
        g2d.dispose();
        mazeImage = visualization;
    }

    private void saveVisualizedImage(String inputPath) {
        try {
            String outputPath = inputPath.replaceFirst("(\\.[^.]+)$", "_similarity$1");
            File outputFile = new File(outputPath);
            System.out.println("Attempting to save to: " + outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
                System.out.println("Created directory: " + outputFile.getParentFile().getAbsolutePath());
            }
            if (!outputFile.getParentFile().canWrite()) {
                System.out.println("Cannot write to directory: " + outputFile.getParentFile().getAbsolutePath());
                return;
            }
            if (mazeImage == null) {
                System.out.println("Error: mazeImage is null, cannot save.");
                return;
            }
            ImageIO.write(mazeImage, "png", outputFile);
            System.out.println("Similarity visualization saved as: " + outputPath);
        } catch (Exception e) {
            System.out.println("Error saving visualized image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayProcessedImage() {
        if (mazeImage == null) {
            System.out.println("Error: mazeImage is null, cannot display.");
            return;
        }
        Image fxImage = SwingFXUtils.toFXImage(mazeImage, null);
        imageView.setImage(fxImage);
        System.out.println("Processed image displayed.");
    }

    public ArrayList<Vertex<SuperPixel>> getUserPath() {
        return userPath;
    }

    public ArrayList<Vertex<SuperPixel>> getSystemPath() {
        return systemPath;
    }

    public BufferedImage getBufferedImage() {
        return mazeImage;
    }
}
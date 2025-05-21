package PathFinding;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import GraphADT.ArrayList;
import GraphADT.Graph;
import GraphADT.SuperPixel;
import GraphADT.Vertex;
import image_preprocessing.ImagePreProcessor;

public class PathFinder {
    public static void main(String[] args) {

        // Load the maze graph
        Graph<SuperPixel> gp = new Graph<>("data/2.png");

        // Find path from start to end
        ArrayList<Vertex<SuperPixel>> path = gp.findPath();
        

        System.out.println("START: " + gp.getStartVertex());
        System.out.println("END: " + gp.getEndVertex());

        if (path.isEmpty()) {
            System.out.println("No path found from start to end.");
            return;
        }

        try {
          
            
            ImagePreProcessor processor = new ImagePreProcessor("data/2.png");
            BufferedImage background = processor.getProcessedImage();
    		 

            // Create overlay image
            BufferedImage overlay = new BufferedImage(
                background.getWidth(),
                background.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            // Create graphics context
            Graphics2D g2 = overlay.createGraphics();
            g2.drawImage(background, 0, 0, null);

            // Enable anti-aliasing for smoother lines
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set thick red stroke
            g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.RED);

            // Draw stepped lines between path nodes
            for (int i = 1; i < path.size(); i++) {
                Vertex<SuperPixel> a = path.get(i - 1);
                Vertex<SuperPixel> b = path.get(i);

                int ax = a.GetElement().getAvgPixelXPos();
                int ay = a.GetElement().getyAvgPixelYPos();
                int bx = b.GetElement().getAvgPixelXPos();
                int by = b.GetElement().getyAvgPixelYPos();

                g2.drawLine(ax, ay, bx, by);
            }

            g2.dispose();

            // Save final output
            File output = new File("Solutions/FinalPath.png");
            output.getParentFile().mkdirs();
            ImageIO.write(overlay, "png", output);
            System.out.println("Solved maze written to " + output.getPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

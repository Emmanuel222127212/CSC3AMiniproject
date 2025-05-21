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

	public Graph<SuperPixel> gp;
	private String filepath;

	public PathFinder(Graph<SuperPixel> Graph,String fpath)
	{


		this.filepath=fpath;
		this.gp=Graph;


	}
	
	public void findPath()
	{
		System.out.println("Searching For Path.....");
		// Find path from start to end
		ArrayList<Vertex<SuperPixel>> path = gp.findPath();

		if (path.isEmpty()) {
			System.out.println("No path found from start to end.");
			return;
		}
	
		try {

			ImagePreProcessor processor = new ImagePreProcessor(filepath);
			BufferedImage original = processor.getProcessedImage();

			
			
			int ScaledHeight=800;
			int ScaledWidth=800;
			
			// Create overlay image
			BufferedImage scaledOverlay = new BufferedImage(
					ScaledWidth,
					ScaledHeight,
					BufferedImage.TYPE_INT_RGB
					);

			// Create graphics context
			Graphics2D g2 = scaledOverlay.createGraphics();
			g2.drawImage(original, 0, 0,ScaledWidth,ScaledHeight, null);

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
			String fileName = new File(filepath).getName();
			File output = new File("Converted/" + fileName);
			output.getParentFile().mkdirs();
			ImageIO.write(scaledOverlay, "png", output);
			System.out.println("Solved maze written to " + output.getPath());

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {

	}
}

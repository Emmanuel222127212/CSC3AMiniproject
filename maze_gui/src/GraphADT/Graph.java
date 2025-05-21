package GraphADT;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import image_preprocessing.ImagePreProcessor;

public class Graph<T> {

	// List of superpixel vertices
	private ArrayList<Vertex<SuperPixel>> SuperPixelList;
	private int NumRegions;
	private int Imgheight;
	private int Imgwidth;
	// Map from unique superpixel ID to its vertex
	private HashTable<Integer, Vertex<SuperPixel>> VertexMap;

	static int edgecount = 0;
	// Edge detection threshold: adjust if necessary for subtle intensity differences.
	private final int Threshhold = 150;
	// Maximum number of pixels per superpixel.
	// For complex mazes you may want to lower this value, so more but smaller superpixels are created.
	private final int MAXSUPERPIXELAMOUNT = 16;
	private Vertex<SuperPixel> startVertex = null;
	private Vertex<SuperPixel> endVertex = null;

	/**
	 * assume image passed though is already grey scaled
	 * @param FileName the filename
	 */
	public Graph(String FileName) {
		// Process the image using the ImagePreProcessor.
		ImagePreProcessor processor = new ImagePreProcessor(FileName);
		BufferedImage processedImg = processor.getProcessedImage();
		BufferedImage ReadGrey = GreyScaleImage(processedImg); // Convert to grayscale

		Imgheight = ReadGrey.getHeight(); // image height
		Imgwidth = ReadGrey.getWidth();   // image width

		// Estimate number of superpixels (using image area and a fixed 16-pixel region)
		int estimatedNumSuperPixels = Imgheight * Imgwidth / 16;
		if (estimatedNumSuperPixels % 2 == 0) {
			estimatedNumSuperPixels++;
		}
		SuperPixelList = new ArrayList<Vertex<SuperPixel>>();
		VertexMap = new HashTable<Integer, Vertex<SuperPixel>>(estimatedNumSuperPixels);

		// Locate all edges in the image (maze walls, borders, etc.)
		boolean[][] EdgesFound = EdgeDetect(ReadGrey);

		// Build the graph from the detected edges and the grayscale image.
		// (Note: Only one call is used to avoid duplicating vertices or connections.)
		ConstructConnectedGraph(EdgesFound, ReadGrey);

		// Determine the start and end vertices from image borders.
		findStartAndEndFromEdges(ReadGrey);

		// Generate an RGB copy of the image based on grayscale values.
		BufferedImage rgbImage = new BufferedImage(Imgwidth, Imgheight, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < Imgheight; y++) {
			for (int x = 0; x < Imgwidth; x++) {
				int gray = new Color(ReadGrey.getRGB(x, y)).getRed();
				Color grayColor = new Color(gray, gray, gray);
				rgbImage.setRGB(x, y, grayColor.getRGB());
			}
		}

		// Colorize superpixels for visualization.
		for (Vertex<SuperPixel> vertex : SuperPixelList) {
			SuperPixel sp = vertex.GetElement();
			// Choose a fixed color (or randomize if desired)
			Color color = new Color(120, 100, 120);
			for (Pixel p : sp.getAllPixels()) {
				int x = p.getXPos();
				int y = p.getYPos();
				// Only color path (passage) pixels (based on type)
				if (sp.GetType() == 1) {
					rgbImage.setRGB(x, y, color.getRGB());
				}
			}
		}

		// Create an overlaid image that shows edges (connections)
		BufferedImage edgeOverlay = new BufferedImage(rgbImage.getWidth(), rgbImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = edgeOverlay.getGraphics();
		g.drawImage(rgbImage, 0, 0, null);

		// Draw edges between vertices using the centroids of the superpixels.
		for (Vertex<SuperPixel> spVertex : this.SuperPixelList) {
			for (Edge edge : spVertex.EdgeList()) {
				Vertex<SuperPixel> fromVertex = edge.getVertFrom();
				Vertex<SuperPixel> toVertex = edge.getVertTO();
				int fromX = fromVertex.GetElement().getAvgPixelXPos();
				int fromY = fromVertex.GetElement().getyAvgPixelYPos();
				int toX = toVertex.GetElement().getAvgPixelXPos();
				int toY = toVertex.GetElement().getyAvgPixelYPos();
				// Draw connection lines (blue color)
				g.setColor(Color.blue);
				g.drawLine(fromX, fromY, toX, toY);
				System.err.println(edge.getWeight());
			}
		}
		g.dispose();

		// Save the resulting overlaid image (for visual debugging).
		try {
			System.out.println("Making image for " + FileName);
			ImageIO.write(edgeOverlay, "png", new File("Output_maze.png"));
			System.out.println("Image made");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Returns a rectangle bounding the maze based on border pixels.
	private Rectangle getMazeBounds(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int top = height, bottom = 0, left = width, right = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y) & 0xFF;
				if (rgb < 100) { // Consider dark pixels as walls.
					if (x < left) left = x;
					if (x > right) right = x;
					if (y < top) top = y;
					if (y > bottom) bottom = y;
				}
			}
		}
		if (left > right || top > bottom) {
			// Fallback to full image bounds.
			return new Rectangle(0, 0, image.getWidth(), image.getHeight());
		}
		return new Rectangle(left, top, right - left + 1, bottom - top + 1);
	}

	// Finds start and end vertices by scanning the perimeter of the maze.
	public void findStartAndEndFromEdges(BufferedImage image) {
		 
		Rectangle mazeBounds = getMazeBounds(image);
		int left = mazeBounds.x;
		int right = left + mazeBounds.width - 1;
		int top = mazeBounds.y;
		int bottom = top + mazeBounds.height - 1;

		 
		// Search top edge for start vertex.
		for (int x = left; x <= right; x++) {
			if (isPathPixel(image.getRGB(x, top))) {
				startVertex = findSuperPixelAt(x, top);
				break;
			}
		}
		// Search bottom edge for end vertex.
		for (int x = left; x <= right; x++) {
			if (isPathPixel(image.getRGB(x, bottom))) {
				if (endVertex == null) {
					endVertex = findSuperPixelAt(x, bottom);
					break;
				}
			}
		}
		// If start not found, search left edge.
		if (startVertex == null) {
			for (int y = top; y <= bottom; y++) {
				if (isPathPixel(image.getRGB(left, y))) {
					startVertex = findSuperPixelAt(left, y);
					break;
				}
			}
		}
		// If end not found, search right edge.
		if (endVertex == null) {
			for (int y = top; y <= bottom; y++) {
				if (isPathPixel(image.getRGB(right, y))) {
					endVertex = findSuperPixelAt(right, y);
					break;
				}
			}
		}
	}

	// Returns the vertex containing the pixel at (x, y)
	private Vertex<SuperPixel> findSuperPixelAt(int x, int y) {
		for (Vertex<SuperPixel> v : SuperPixelList) {
			for (Pixel p : v.GetElement().getAllPixels()) {
				if (p.getXPos() == x && p.getYPos() == y) {
					return v;
				}
			}
		}
		return null;
	}

	private boolean isPathPixel(int rgb) {
		int red = (rgb >> 16) & 0xFF;
		int green = (rgb >> 8) & 0xFF;
		int blue = rgb & 0xFF;

		int average = (red + green + blue) / 3;
		return average > 200;
	}

	public ArrayList<Vertex<SuperPixel>> findPath() {
	    ArrayList<Vertex<SuperPixel>> path = new ArrayList<>();

	    if (startVertex == null || endVertex == null) {
	        return path;
	    }

	    LinkedQueue<Vertex<SuperPixel>> queue = new LinkedQueue<>();
	    ArrayList<Vertex<SuperPixel>> visited = new ArrayList<>();
	    HashTable<Integer, Vertex<SuperPixel>> parent = new HashTable<>();

	    queue.Enqueue(startVertex);
	    visited.add(startVertex);
	    parent.put(startVertex.GetElement().getId(), null);

	    while (!queue.isEmpty()) {
	        Vertex<SuperPixel> current = queue.Dequeue();

	        if (current.equals(endVertex)) {
	            for (Vertex<SuperPixel> node = current; node != null; node = parent.get(node.GetElement().getId())) {
	                path.add(0, node);  // Prepend node
	            }
	            return path;
	        }

	        for (Edge<SuperPixel> edge : current.EdgeList()) {
	            Vertex<SuperPixel> neighbor = edge.getVertFrom().equals(current)
	                ? edge.getVertTO()
	                : edge.getVertFrom();

	            if (!visited.contains(neighbor)) {
	                visited.add(neighbor);
	                parent.put(neighbor.GetElement().getId(), current);
	                queue.Enqueue(neighbor);
	            }
	        }
	    }

	    return path;
	}


	/**
	 * Method for getting the starting vertex.
	 * @return startVertex
	 */
	public Vertex<SuperPixel> getStartVertex() {
		return startVertex;
	}

	/**
	 * Method for getting the end vertex.
	 * @return endVertex
	 */
	public Vertex<SuperPixel> getEndVertex() {
		return endVertex;
	}

	public ArrayList<Vertex<SuperPixel>> getVertices() {
		return this.SuperPixelList;
	}

	/**
	 * Function to add a superpixel to the graph.
	 * @param value Superpixel/Vertex to add to the graph.
	 */
	private void addSuperPixel(SuperPixel value) {
		Vertex<SuperPixel> V = new Vertex<SuperPixel>(value);
		VertexMap.put(value.getId(), V);
		SuperPixelList.add(V);
		NumRegions++;
	}

	/**
	 * Gets the edge between the two supplied vertices.
	 * @param a Initial vertex.
	 * @param b Next vertex.
	 * @return The edge between the vertices (null if none exists).
	 */
	public Edge<T> getEdge(Vertex<T> a, Vertex<T> b) {
		Vertex<T> initial = a;
		List<Edge<T>> Edges = initial.EdgeList();
		for (Edge<T> E : Edges) {
			if (E.ValidateVertices(a, b)) {
				return E;
			}
		}
		return null;
	}

	/**
	 * Add an edge between two vertices (the graph is undirected).
	 * @param a Initial vertex.
	 * @param b Next vertex.
	 */
	public void addEdge(Vertex<SuperPixel> a, Vertex<SuperPixel> b) {
		Edge<SuperPixel> Edgebtween = new Edge<SuperPixel>(a, b);
		a.AddEdge(Edgebtween);
		b.AddEdge(Edgebtween);
		edgecount++;
	}

	/**
	 * Check if there's already an edge between two superpixels (based on their IDs).
	 * @param superPixelId1 ID of the first superpixel.
	 * @param superPixelId2 ID of the second superpixel.
	 * @return True if an edge exists, otherwise false.
	 */
	private boolean ValidateEdgeExistence(int superPixelId1, int superPixelId2) {
		Vertex<SuperPixel> toCompareA = SearchSuperPixelList(superPixelId1);
		Vertex<SuperPixel> toCompareB = SearchSuperPixelList(superPixelId2);
		if (toCompareA == null || toCompareB == null) {
			return false;
		}
		for (Edge<SuperPixel> E : toCompareA.EdgeList()) {
			if (E.ValidateVertices(toCompareA, toCompareB)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Locate a superpixel by its ID.
	 * @param SPId ID of the superpixel.
	 * @return The corresponding vertex, or null if not found.
	 */
	private Vertex<SuperPixel> SearchSuperPixelList(int SPId) {
		return VertexMap.get(SPId);
	}

	/**
	 * Convert a colored image to grayscale.
	 * @param colourImage The original colored image.
	 * @return The grayscale image.
	 */
	private BufferedImage GreyScaleImage(BufferedImage colourImage) {
		if (colourImage == null) {
			System.err.println("Input image is null.");
			return null;
		}
		BufferedImage greyScale = new BufferedImage(colourImage.getWidth(), colourImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics drawGrey = greyScale.getGraphics();
		drawGrey.drawImage(colourImage, 0, 0, null);
		drawGrey.dispose();
		try {
			File output = new File("Converted/GreyImage.png");
			output.getParentFile().mkdirs();
			ImageIO.write(greyScale, "png", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return greyScale;
	}

	/**
	 * Detect edges in the image based on intensity differences.
	 * @param img The image to process.
	 * @return A 2D boolean array marking border positions.
	 */
	private boolean[][] EdgeDetect(BufferedImage img) {
		boolean[][] DetectedEdges = new boolean[Imgheight][Imgwidth];
		for (int y = 0; y < Imgheight; y++) {
			for (int x = 0; x < Imgwidth; x++) {
				int CPixelIntensity = new Color(img.getRGB(x, y)).getRed();
				if (IsInImage(y, x)) {
					int[] AdjacentPixels = GetAdjacentIntesities(img, y, x);
					boolean EdgeDetected = false;
					for (int i = 0; i < AdjacentPixels.length; i++) {
						if (Math.abs(CPixelIntensity - AdjacentPixels[i]) > Threshhold) {
							EdgeDetected = true;
							break;
						}
					}
					DetectedEdges[y][x] = EdgeDetected;
				} else if (!IsInImage(y, x) && CPixelIntensity == 255) {
					DetectedEdges[y][x] = false;
				} else if (!IsInImage(y, x) && CPixelIntensity == 0) {
					DetectedEdges[y][x] = true;
				}
			}
		}
		return DetectedEdges;
	}

	/**
	 * Create all vertices and add edges between valid vertices based on the detected edges.
	 * @param DetectedEdges 2D array marking border locations.
	 * @param img The image being processed.
	 */
	private void ConstructConnectedGraph(boolean[][] DetectedEdges, BufferedImage img) {
		boolean[][] visitedIndices = new boolean[this.Imgheight][this.Imgwidth];
		int[][] pixelToSuperMap = new int[this.Imgheight][this.Imgwidth];

		// For every pixel, if it's not an edge and not visited, grow a new superpixel.
		for (int y = 0; y < this.Imgheight; y++) {
			for (int x = 0; x < this.Imgwidth; x++) {
				if (!DetectedEdges[y][x] && !visitedIndices[y][x]) {
					SuperPixel toAdd = GrowSuperPixel(x, y, DetectedEdges, visitedIndices, img, pixelToSuperMap);
					addSuperPixel(toAdd);
				}
			}
		}

		// Construct edges between superpixels.
		boolean[][] vistedEdgeConstruction = new boolean[Imgheight][Imgwidth];
		for (int r = 0; r < this.Imgheight; r++) {
			for (int c = 0; c < this.Imgwidth; c++) {
				if (!vistedEdgeConstruction[r][c] && pixelToSuperMap[r][c] != -1) {
					ConstructEdges(c, r, pixelToSuperMap, vistedEdgeConstruction, img);
				}
			}
		}
	}

	/**
	 * Use BFS to create edges between adjacent superpixels.
	 * @param initX Starting pixel X position.
	 * @param initY Starting pixel Y position.
	 * @param superPixelMap 2D array mapping pixels to superpixel IDs.
	 * @param VistedPixels 2D array marking visited pixels for edge construction.
	 * @param img The image.
	 */
	private void ConstructEdges(int initX, int initY, int[][] superPixelMap, boolean[][] VistedPixels, BufferedImage img) {
		LinkedQueue<Pixel> bfsQueue = new LinkedQueue<>();
		bfsQueue.Enqueue(new Pixel(initX, initY, new Color(img.getRGB(initX, initY)).getRed()));
		VistedPixels[initY][initX] = true;

		while (!bfsQueue.isEmpty()) {
			Pixel currPixel = bfsQueue.Dequeue();
			int currX = currPixel.getXPos();
			int currY = currPixel.getYPos();
			int currSpId = superPixelMap[currY][currX];

			// Check the 8 adjacent pixels.
			for (int i = 1; i >= -1; i--) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0) continue;
					int NewX = currX + j;
					int NewY = currY + i;
					if (NewX < 0 || NewX >= Imgwidth || NewY < 0 || NewY >= Imgheight) continue;
					if (VistedPixels[NewY][NewX]) continue;
					if (superPixelMap[NewY][NewX] == -1) continue;

					int neighbouringSpId = superPixelMap[NewY][NewX];
					if (neighbouringSpId != currSpId && !ValidateEdgeExistence(currSpId, neighbouringSpId)) {
						Vertex<SuperPixel> toAddEdgeA = SearchSuperPixelList(currSpId);
						Vertex<SuperPixel> toAddEdgeB = SearchSuperPixelList(neighbouringSpId);
						if (toAddEdgeA != null && toAddEdgeB != null) {
							addEdge(toAddEdgeA, toAddEdgeB);
						}
					}
					if (neighbouringSpId != currSpId && !VistedPixels[NewY][NewX]) {
						bfsQueue.Enqueue(new Pixel(NewX, NewY, new Color(img.getRGB(NewX, NewY)).getRed()));
						VistedPixels[NewY][NewX] = true;
					}
				}
			}
		}
	}

	/**
	 * Run a BFS starting at (InitX, InitY) to grow a new superpixel.
	 * @param InitX Starting pixel X position.
	 * @param InitY Starting pixel Y position.
	 * @param EdgeCollection 2D array marking edge (border) pixels.
	 * @param visitedCollection 2D array marking visited pixels.
	 * @param img The image.
	 * @param superPixelMap 2D array mapping pixels to superpixel IDs.
	 * @return The constructed SuperPixel.
	 */
	private SuperPixel GrowSuperPixel(int InitX, int InitY, boolean[][] EdgeCollection, boolean[][] visitedCollection,
			BufferedImage img, int[][] superPixelMap) {
		LinkedQueue<Pixel> queue = new LinkedQueue<>();
		SuperPixel SP = new SuperPixel();
		Pixel InitPix = new Pixel(InitX, InitY, new Color(img.getRGB(InitX, InitY)).getRed());
		queue.Enqueue(InitPix);
		visitedCollection[InitY][InitX] = true;
		SP.AddPixel(InitPix);
		superPixelMap[InitY][InitX] = SP.getId();

		// Grow the superpixel until the queue is empty or the max allowed size is reached.
		while (!queue.isEmpty() && SP.SuperPixelSize() < MAXSUPERPIXELAMOUNT) {
			Pixel CurrentPix = queue.Dequeue();
			SingleLinkedList<Pixel> ValidNeighboursList = GetAdjacentPixels(
					CurrentPix.getXPos(), CurrentPix.getYPos(), EdgeCollection, visitedCollection, img, superPixelMap);
			for (Pixel P : ValidNeighboursList) {
				if (SP.SuperPixelSize() >= MAXSUPERPIXELAMOUNT) {
					break;
				}
				queue.Enqueue(P);
				visitedCollection[P.getYPos()][P.getXPos()] = true;
				superPixelMap[P.getYPos()][P.getXPos()] = SP.getId();
				SP.AddPixel(P);
			}
		}
		SP.CalculateCetroids();
		return SP;
	}

	/**
	 * Given the position (Xpos, YPos), returns a list of adjacent pixels that are valid
	 * for being added to a new superpixel.
	 * @param Xpos Current pixel X position.
	 * @param YPos Current pixel Y position.
	 * @param Edges 2D array marking edges.
	 * @param visitedPixels 2D array marking visited pixels.
	 * @param img The image.
	 * @param spMap 2D array mapping pixels to superpixel IDs.
	 * @return A SingleLinkedList of valid adjacent pixels.
	 */
	private SingleLinkedList<Pixel> GetAdjacentPixels(int Xpos, int YPos, boolean[][] Edges, boolean[][] visitedPixels,
			BufferedImage img, int[][] spMap) {
		SingleLinkedList<Pixel> ToReturn = new SingleLinkedList<>();
		for (int i = 1; i >= -1; i--) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				int NewX = Xpos + j;
				int NewY = YPos + i;
				if (NewX < 0 || NewX >= Imgwidth || NewY < 0 || NewY >= Imgheight) continue;
				if (Edges[NewY][NewX]) {
					spMap[NewY][NewX] = -1;
					continue;
				}
				if (visitedPixels != null && visitedPixels[NewY][NewX]) continue;
				int intensity = new Color(img.getRGB(NewX, NewY)).getRed();
				ToReturn.Addlast(new Pixel(NewX, NewY, intensity));
			}
		}
		return ToReturn;
	}

	/**
	 * Returns an array of the intensities of the 6 adjacent pixels.
	 * @param img The image.
	 * @param y Current Y position.
	 * @param x Current X position.
	 * @return An int[] of adjacent pixel intensities.
	 */
	private int[] GetAdjacentIntesities(BufferedImage img, int y, int x) {
		int TopLeftIntensity = new Color(img.getRGB(x - 1, y + 1)).getRed();
		int TopRightIntensity = new Color(img.getRGB(x + 1, y + 1)).getRed();
		int MiddleLeftIntensity = new Color(img.getRGB(x - 1, y)).getRed();
		int MiddleRightIntensity = new Color(img.getRGB(x + 1, y)).getRed();
		int BottomLeftIntensity = new Color(img.getRGB(x - 1, y - 1)).getRed();
		int BottomRightIntensity = new Color(img.getRGB(x + 1, y - 1)).getRed();
		int[] AdjacentPixels = {TopLeftIntensity, TopRightIntensity, MiddleLeftIntensity, MiddleRightIntensity,
				BottomLeftIntensity, BottomRightIntensity};
		return AdjacentPixels;
	}

	/**
	 * Check if moving one pixel in any of the 8 directions from (CRow, CCol) would leave the image.
	 * @param CRow Current pixel row.
	 * @param CCol Current pixel column.
	 * @return True if within bounds, false otherwise.
	 */
	private boolean IsInImage(int CRow, int CCol) {
		if (CRow + 1 > this.Imgheight - 1 || CRow - 1 < 0) return false;
		if (CCol + 1 > this.Imgwidth - 1 || CCol - 1 < 0) return false;
		return true;
	}
}

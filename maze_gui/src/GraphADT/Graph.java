package GraphADT;

import image_preprocessing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class Graph<T> {

	private GraphADT.ArrayList<Vertex<SuperPixel>> SuperPixelList;
	private int NumRegions;
	private int Imgheight;
	private int Imgwidth;
	private HashTable<Integer, Vertex<SuperPixel>> VertexMap;

	static int edgecount = 0;
	// 0 black
	// 255 white
	private final int Threshhold = 100;
	private final int MAXSUPERPIXELAMOUNT = 16; // edges are usually 2 pixels on each end so 4 pixels per "Block"
	private Vertex<SuperPixel> startVertex = null;
	private Vertex<SuperPixel> endVertex = null;

	/**
	 * assume image passed though is already grey scaled
	 * 
	 * @param FileName the filename
	 */
	public Graph(String FileName) {

		ImagePreProcessor test = new ImagePreProcessor(FileName);
		BufferedImage ReadGrey = test.getProcessedImage();// convert image to greyscale

		Imgheight = ReadGrey.getHeight(); // get height
		Imgwidth = ReadGrey.getWidth(); // get width

		int estimatedNumSuperPixels = Imgheight * Imgwidth / 16; // used to setup the vertexMap
																	// opt for a non even number due to underlysing hash
																	// function

		if (estimatedNumSuperPixels % 2 == 0) {
			estimatedNumSuperPixels++;
		}
		SuperPixelList = new GraphADT.ArrayList<Vertex<SuperPixel>>(); // initialise the Adjacency List(list of
		// vertices/superpixels)
		VertexMap = new HashTable<Integer, Vertex<SuperPixel>>(estimatedNumSuperPixels);

		boolean[][] EdgesFound = EdgeDetect(ReadGrey); // locate all the edges in the image(all walls and out of bounds
														// areas)

		ConstructConnectedGraph(EdgesFound, ReadGrey); // Use the edges and grey scale to construct superpixels and
														// connect
		// each vertex and edge

		// Convert grayscale image to RGB copy
		BufferedImage rgbImage = new BufferedImage(Imgwidth, Imgheight, BufferedImage.TYPE_INT_RGB);

		// Fill the image with grayscale values (if it's grayscale)
		for (int y = 0; y < Imgheight; y++) {
			for (int x = 0; x < Imgwidth; x++) {
				int gray = new Color(ReadGrey.getRGB(x, y)).getRed(); // Get grayscale value
				Color grayColor = new Color(gray, gray, gray);
				rgbImage.setRGB(x, y, grayColor.getRGB());
			}
		}

		for (Vertex<SuperPixel> vertex : SuperPixelList) {
			SuperPixel sp = vertex.GetElement(); // Get the SuperPixel object from the vertex
			Color color = new Color(120, 100, 120); // Random color for each

			for (Pixel p : sp.getAllPixels()) {
				int x = p.getXPos();
				int y = p.getYPos();

				if (sp.GetType() == 1) {

					rgbImage.setRGB(x, y, color.getRGB()); // Set pixel color for each pixel in superpixel
				}
			}

		}

		ConstructConnectedGraph(EdgesFound, ReadGrey); // Use the edges and grey scale to construct superpixels and
														// connect
		// each vertex and edge

		findStartAndEndFromEdges(ReadGrey);

	}

	// Returns a rectangle bounding the maze based on border pixels.
	private Rectangle getMazeBounds(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int top = height, bottom = 0, left = width, right = 0; // reverse search so to speak
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y) & 0xFF;
				if (rgb < 100) { // Consider dark pixels as walls.
					if (x < left)
						left = x;
					if (x > right)
						right = x;
					if (y < top)
						top = y;
					if (y > bottom)
						bottom = y;
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

	/**
	 * Makes a path shorter by keeping only some of the points. This method goes
	 * through the given path and keeps every 4th SuperPixel (or whatever value is
	 * set in STEP) to make the path simpler. It also makes sure the last point in
	 * the original path is always included, even if it was skipped.
	 * 
	 * @param path The original list of SuperPixel vertices (the full path)
	 * @return A shorter version of the path
	 */
	public GraphADT.ArrayList<Vertex<SuperPixel>> simplifyPath(GraphADT.ArrayList<Vertex<SuperPixel>> path) {
		// If the path has fewer than 2 points, return it as is - would'nt make sense to
		// skip pixels if our path is that short
		if (path.size() < 2)
			return path;

		GraphADT.ArrayList<Vertex<SuperPixel>> simple = new GraphADT.ArrayList<Vertex<SuperPixel>>();

		// Only keep every 4th point from the path - (i was trying to minimise/reduce
		// the lines in the path)
		int STEP = 4;

		// Go through the path, jumping by STEP each time
		for (int i = 0; i < path.size(); i += STEP) {
			simple.add(path.get(i)); // Add the selected point to the simplified path
		}

		// Get the last point in the original path - there'll be cases where we skip
		// over the end vertex
		// so we're trying to make sure that our path is connected from start - middle
		// to end
		Vertex<SuperPixel> last = path.get(path.size() - 1);

		// If the last point was not already added, add it now
		if (simple.get(simple.size() - 1) != last) {
			simple.add(last);
		}

		// Return the simplified path
		return simple;
	}

	/**
	 * Finds a path from the start SuperPixel to the end SuperPixel using
	 * breadth-first search (BFS). The method looks for the shortest path by
	 * checking all possible paths, step by step. It returns the first path that
	 * reaches the end SuperPixel.
	 * 
	 * @return A list of SuperPixel vertices that form the path from start to end.
	 *         If no path is found or start/end is missing, returns an empty list.
	 */
	public ArrayList<Vertex<SuperPixel>> findPath() {
		// Create a list to store the final path from start to end
		ArrayList<Vertex<SuperPixel>> path = new ArrayList<>();

		// If either start or end is missing, return an empty path
		if (startVertex == null || endVertex == null) {
			return path;
		}

		// Queue for BFS
		LinkedQueue<Vertex<SuperPixel>> queue = new LinkedQueue<>();
		// List to keep track of visited vertices
		ArrayList<Vertex<SuperPixel>> visited = new ArrayList<>();
		// Hash table to keep track of each vertex's parent (used for building the path)
		HashTable<Integer, Vertex<SuperPixel>> parent = new HashTable<>();

		// Start BFS by enqueuing the start vertex
		queue.Enqueue(startVertex);
		// Mark the start vertex as visited
		visited.add(startVertex);
		// Set the start vertex's parent to null (it's the root of the path)
		parent.put(startVertex.GetElement().getId(), null);

		// Continue BFS while there are vertices to explore
		while (!queue.isEmpty()) {
			// Get the next vertex in the queue
			Vertex<SuperPixel> current = queue.Dequeue();

			// If we've reached the end vertex, build and return the path
			if (current.equals(endVertex)) {
				return buildPath(parent, current);
			}

			// Go through all edges (connections) from the current vertex
			for (Edge<SuperPixel> edge : current.EdgeList()) {
				Vertex<SuperPixel> neighbor;

				// Determine which vertex is the neighbor (the one not equal to current)
				if (edge.getVertFrom().equals(current)) {
					neighbor = edge.getVertTO();
				} else {
					neighbor = edge.getVertFrom();
				}

				// If we haven't visited this neighbor yet
				if (!visited.contains(neighbor)) {
					// Mark it as visited
					visited.add(neighbor);
					// Record its parent so we can trace the path later
					parent.put(neighbor.GetElement().getId(), current);
					// Add the neighbor to the queue to explore it later
					queue.Enqueue(neighbor);
				}
			}
		}

		// If no path found, return the empty list
		return path;
	}

	// Helper method to build the path from end to start using the parent map
	private ArrayList<Vertex<SuperPixel>> buildPath(HashTable<Integer, Vertex<SuperPixel>> parent, Vertex<SuperPixel> end) {
	    // List to store the path
	    ArrayList<Vertex<SuperPixel>> path = new ArrayList<>();
	    // Start from the end node
	    Vertex<SuperPixel> node = end;

	    // Keep adding each parent node to the front of the path list
	    while (node != null) {
	        path.add(0, node); // Add at the beginning to reverse the path
	        node = parent.get(node.GetElement().getId()); // Move to the parent
	    }

	    // Return the complete path from start to end
	    return path;
	}


	/**
	 * Method for getting the starting vertex
	 * 
	 * @return startVertex
	 */
	public Vertex<SuperPixel> getStartVertex() {
		return startVertex;
	}

	/**
	 * Method for getting the end vertex
	 * 
	 * @return endVertex
	 */
	public Vertex<SuperPixel> getEndVertex() {
		return endVertex;
	}

	public GraphADT.ArrayList<Vertex<SuperPixel>> getVertices() {
		return this.SuperPixelList;
	}

	/**
	 * Function to add superpixels to the adjacency list forming the graph structure
	 * Takes in a superpixel,places it in a vertex and addds to list
	 * 
	 * @param value Superpixel/Vertex to add to graph
	 */
	private void addSuperPixel(SuperPixel value) {

		Vertex<SuperPixel> V = new Vertex<SuperPixel>(value);
		VertexMap.put(value.getId(), V); // add this to the vertex map

		this.SuperPixelList.add(V);

		NumRegions++;

	}

	/**
	 * Gets the Edge between the two vertices supplied 'a' and 'b'
	 * 
	 * @param a Initial vertex
	 * @param b Next Vertex
	 * @return The edge betweeen the vertices (null if none exists)
	 */
	public Edge<T> getEdge(Vertex<T> a, Vertex<T> b) {

		Vertex<T> initial = a;
		List<Edge<T>> Edges = initial.EdgeList();

		for (Edge<T> E : Edges) {
			if (E.ValidateVertices(a, b)) {
				return E;
			}
		}

		// if no edge exists between the two vertices return null
		return null;
	}

	/**
	 * Add an edge between two vertices;thereby connecting them together
	 * 
	 * @param a Intial Vertex Vertex to add edge between
	 * @param b Next Vertex Vertex were adding edge to
	 */
	public void addEdge(Vertex<SuperPixel> a, Vertex<SuperPixel> b) {
		Edge<SuperPixel> Edgebtween = new Edge<SuperPixel>(a, b);

		// Add edge to both as graph is undirected

		a.AddEdge(Edgebtween);
		b.AddEdge(Edgebtween);
		edgecount++;

	}

	/**
	 * Check if theres already an edge between 2 SuperPixels based on their ID
	 * 
	 * @param superPixelId1 id of the second superpixel to check
	 * @param superPixelId2 Id of the first superpixel to check
	 * @return True/False based on if an edge exists
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
	 * Locate a SuperPixel via its ID and return it within a vertex
	 * 
	 * @param SPId ID of the superPixel to retrieve
	 * @return A Vertext containing the found superPixel
	 */
	private Vertex<SuperPixel> SearchSuperPixelList(int SPId) {

		Vertex<SuperPixel> toReturn = VertexMap.get(SPId);
		return toReturn;
	}

	/**
	 * Take in an image and greyscale it
	 * 
	 * @param Filename Filename with path of the image to greyscale
	 * @return GreyScaled image
	 */

	/**
	 * Take an image in and locate the borders/edges within it (where a large change
	 * in intensity occurs)
	 * 
	 * @param img Image To Find borders for
	 * @return A 2D array maping out the image with borders marked off
	 */
	private boolean[][] EdgeDetect(BufferedImage img) {

		// -1 for edges
		// 1 for no edge
		// 0 otherwise

		boolean[][] DetectedEdges = new boolean[Imgheight][Imgwidth];

		// sobel kernels
		int[][] xkernel = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int[][] ykernel = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

		for (int y = 1; y < Imgheight - 1; y++) {
			for (int x = 1; x < Imgwidth - 1; x++) {
				// C,R because it wants x then Y
				int pixelX = 0;
				int pixelY = 0;

				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						int CPixelIntensity = new Color(img.getRGB(x + j, y + i)).getRed(); // greyscale has only
																							// intensity so the value
						pixelX += CPixelIntensity * xkernel[i + 1][j + 1];
						pixelY += CPixelIntensity * ykernel[i + 1][j + 1];
					}
				}
				int magnitude = (int) Math.sqrt((pixelX * pixelX) + (pixelY * pixelY));

				if (magnitude > Threshhold) {
					DetectedEdges[y][x] = true;

				} else {
					DetectedEdges[y][x] = false;
				}
			}

		}

		for (int x = 0; x < Imgwidth; x++) {
			int intesityTop = new Color(img.getRGB(x, 0)).getRed();
			int intesityBottom = new Color(img.getRGB(x,Imgheight - 1)).getRed();
			if (intesityTop > Threshhold) {

				continue;
			}
			if (intesityBottom > Threshhold) {
				continue;
			}

			DetectedEdges[0][x] = true;
			DetectedEdges[Imgheight - 1][x] = true;
		}
		for (int y = 0; y < Imgheight; y++) {

			int intesityLeft = new Color(img.getRGB( 0,y)).getRed();
			int intesityRight = new Color(img.getRGB(Imgwidth - 1,y)).getRed();
			if (intesityLeft > Threshhold) {

				continue;
			}
			if (intesityRight > Threshhold) {
				continue;
			}

			DetectedEdges[y][0] = true;
			DetectedEdges[y][Imgwidth - 1] = true;
		}

		return DetectedEdges;

	}

	/**
	 * Create all the vertices of the graph and add edges between valid vertices
	 * 
	 * @param DetectedEdges Array of all entries marked as borders/edges
	 * @param img           Image beign worked with
	 */
	private void ConstructConnectedGraph(boolean[][] DetectedEdges, BufferedImage img) {
		boolean[][] visitedIndices = new boolean[this.Imgheight][this.Imgwidth]; // Mark the entries that have already
																					// been visited
		int[][] pixelToSuperMap = new int[this.Imgheight][this.Imgwidth]; // Create ID blobs for superpixels to know
																			// start and end Pos quickly
		// that pixel

		for (int y = 0; y < this.Imgheight; y++) {
			for (int x = 0; x < this.Imgwidth; x++) {

				if (DetectedEdges[y][x] == false && visitedIndices[y][x] == false) {

					// Call method that takes in the current X and Y location,the visted pixel,the
					// detected borders and the map
					// Uses them to create a SuperPixel
					SuperPixel toAdd = GrowSuperPixel(x, y, DetectedEdges, visitedIndices, img, pixelToSuperMap);

					addSuperPixel(toAdd); // Add superPixel to adjacency list

				}
			}
		}

		boolean[][] vistedEdgeConstruction = new boolean[Imgheight][Imgwidth]; // Pixels that have already been visited
																				// when constructing edges
		for (int r = 0; r < this.Imgheight; r++) {
			for (int c = 0; c < this.Imgwidth; c++) {

				if (!vistedEdgeConstruction[r][c] && pixelToSuperMap[r][c] != -1) {
					ConstructEdges(c, r, pixelToSuperMap, vistedEdgeConstruction, img); // build the edges between all
																						// the relevant vertices
				}
			}
		}
	}

	/**
	 * BFS run to create edges between vertices that are adjacent to one another
	 * Also keeps number of edges to a min by not connecting every vertex to every
	 * other vertex Adjacnet to it provided the vertex it already connects to can
	 * connect to the other vertex
	 * 
	 * @param initX         Pixel X (the SuperPixel containing this pixel) Position
	 *                      to start creating edges from
	 * @param initY         Pixel Y (the SuperPixel containing this pixel) Position
	 *                      to start creating edges from
	 * @param superPixelMap
	 * @param VistedPixels
	 * @param img
	 */
	private void ConstructEdges(int initX, int initY, int[][] superPixelMap, boolean[][] VistedPixels,
			BufferedImage img) {

		LinkedQueue<Pixel> bfsQueue = new LinkedQueue<Pixel>();

		// Enqueue the initial pixel
		bfsQueue.Enqueue(new Pixel(initX, initY, new Color(img.getRGB(initX, initY)).getRed()));
		VistedPixels[initY][initX] = true;

		// Start at an initial Pixel and check its 8 adjacent pixels
		// If any of the 8 belong to a different SuperPixel check if
		// An edge exists between the current Pixels SuperPixel and
		// The adjacent one, then if it dosent add the edge and add
		// That pixel to the queue to check its adjacent Pixels
		// Process repeats and edges are constructed

		while (!bfsQueue.isEmpty()) {
			Pixel currPixel = bfsQueue.Dequeue();
			int currX = currPixel.getXPos();
			int currY = currPixel.getYPos();
			int currSpId = superPixelMap[currY][currX];

			// Check 8 adjacent Pixels
			for (int i = 1; i >= -1; i--) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0) {
						continue;
					}
					int NewX = currX + j;
					int NewY = currY + i;

					if (NewX < 0 || NewX > Imgwidth - 1) {
						continue;
					} else if (NewY < 0 || NewY > Imgheight - 1) {
						continue;
					} else if (VistedPixels[NewY][NewX] == true) {
						continue;
					} else if (superPixelMap[NewY][NewX] == -1) {
						continue;
					}

					int neigbouringSpid = superPixelMap[NewY][NewX]; // ID of the neighbouring SuperPixel

					// Different ID's mean that its a different SuperPixel
					// Therefore check if an edge already exists and if it
					// Does not then skip adding an edge between it
					if (neigbouringSpid != currSpId && !ValidateEdgeExistence(currSpId, neigbouringSpid)) {

						// Get a SueperPixel Via its ID
						Vertex<SuperPixel> toAddEdgeA = SearchSuperPixelList(currSpId);
						Vertex<SuperPixel> toAddEdgeB = SearchSuperPixelList(neigbouringSpid);

						// Ensure both vertices are'nt null
						// Add an edge if they arent
						if (toAddEdgeA != null && toAddEdgeB != null) {

							addEdge(toAddEdgeA, toAddEdgeB);

						}

					}

					// If the ID's arent the same and the Pixel Has not Yet been visited in this
					// process
					// Add it to the queue to be checked on next iteration
					// Then mark it as visited
					if (neigbouringSpid != currSpId && VistedPixels[NewY][NewX] == false) {

						bfsQueue.Enqueue(new Pixel(NewX, NewY, new Color(img.getRGB(NewX, NewY)).getRed()));
						VistedPixels[NewY][NewX] = true;
					}
				}
			}

		}

	}

	/**
	 * Run a Breadth First Search from inital x and y pixel position adding more and
	 * more Pixels to a superpixel that will be turned into a vertex for the graph
	 * To be added a pixel has to not be visited and not be a border Superpixels
	 * have a max size of 4x4 or 16 pixels
	 * 
	 * @param InitX             Starting pixel X postion
	 * @param InitY             Starting pixel Y postion
	 * @param EdgeCollection    2D array containing all the entries where a border
	 *                          exists
	 * @param visitedCollection 2D array containing all viisted pixel entries
	 * @param img               Image currently being worked with
	 * @param 2D                array containing ids that form regions of the
	 *                          superpixels (used to mark where one superpixel
	 *                          starts and another begins)
	 * @return The SuperPixel Created
	 */
	private SuperPixel GrowSuperPixel(int InitX, int InitY, boolean[][] EdgeCollection, boolean[][] visitedCollection,
			BufferedImage img, int[][] superPixelMap) {

		LinkedQueue<Pixel> queue = new LinkedQueue<Pixel>(); // Create queue to use for BFS
		SuperPixel SP = new SuperPixel(); // Initial SuperPixel creation

		Pixel InitPix = new Pixel(InitX, InitY, (new Color(img.getRGB(InitX, InitY)).getRed())); // get the initial
																									// pixel to add
		queue.Enqueue(InitPix); // add to the queue
		visitedCollection[InitY][InitX] = true; // mark it as visited
		SP.AddPixel(InitPix); // add Pixel to SuperPixl "blob"
		superPixelMap[InitY][InitX] = SP.getId(); // Mark the position of the pixel as under the current SuperPixels ID

		// Check if the queue is not empty and if the superpixel is not at its maximum
		// size
		// If it isnt take the current pixel at the top of the queue
		// Get its valid Neighbouring pixels(those not yet visited and those that arent
		// borderss)
		// Load all of those pixels into the queue to be visited
		// Mark those added to the queue as visited
		// Mark them under the ID of the current SuperPixel being made
		// Finally add all of them to the SuperPixel
		while (!queue.isEmpty() && SP.SuperPixelSize() < MAXSUPERPIXELAMOUNT) {
			Pixel CurrentPix = queue.Dequeue();

			SingleLinkedList<Pixel> ValidNeighboursList = GetAdjacentPixels(CurrentPix.getXPos(), CurrentPix.getYPos(),
					EdgeCollection, visitedCollection, img, superPixelMap);

			for (Pixel P : ValidNeighboursList) {
				if (SP.SuperPixelSize() >= MAXSUPERPIXELAMOUNT) {
					break; // Dont add over the allowed amount limit to avoid potential issues in a
							// different dataset (added 10 may 2025)
				}

				queue.Enqueue(P);
				visitedCollection[P.getYPos()][P.getXPos()] = true;
				superPixelMap[P.getYPos()][P.getXPos()] = SP.getId();
				SP.AddPixel(P);
			}

		}
		SP.CalculateCetroids(); // Calculate the avg X and Y Pos for a SuperPixel

		return SP;
	}

	/**
	 * Given the current postion of a pixel check pixels adjacent to it and add them
	 * to a list To be used as part of a new super pixel should they pass the
	 * criteria of not being an edge/border and not being visited yet
	 * 
	 * @param Xpos          Current pixel X postion
	 * @param YPos          Current pixel Y position
	 * @param Edges/borders Array containing locations of edges/borders and paths
	 *                      (True for wall false for path)
	 * @param visitedPixels Pixels that have already been visited and are already in
	 *                      other Superpixels
	 * @param img           The image being worked with
	 * @param spMap         2D array containing each superpixels ID taking up an
	 *                      "area" e.g 2x2 blob with id=4
	 * @return List of Pixels surrounding current pixel that are valid for being
	 *         added to a new superpixel
	 */
	private SingleLinkedList<Pixel> GetAdjacentPixels(int Xpos, int YPos, boolean[][] Edges, boolean[][] visitedPixels,
			BufferedImage img, int[][] spMap) {

		SingleLinkedList<Pixel> ToReturn = new SingleLinkedList<Pixel>();

		// loop starts at top left goes to bottom right
		// going along x for each row
		for (int i = 1; i >= -1; i--) {

			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) {
					continue;
				}
				int NewX = Xpos + j;
				int NewY = YPos + i;

				if (NewX < 0 || NewX > Imgwidth - 1) {
					continue;
				} else if (NewY < 0 || NewY > Imgheight - 1) {
					continue;
				} else if (Edges[NewY][NewX] == true) {
					spMap[NewY][NewX] = -1; // no id can be -1 so we know where edges are
					continue;
				} else if (visitedPixels != null && visitedPixels[NewY][NewX]) {
					continue;
				} else {
					int intensity = new Color(img.getRGB(NewX, NewY)).getRed();

					ToReturn.Addlast(new Pixel(NewX, NewY, intensity));
				}
			}
		}

		return ToReturn;
	}

}

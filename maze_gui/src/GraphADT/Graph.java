package GraphADT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Graph<T> {

	private GraphADT.ArrayList<Vertex<SuperPixel>> SuperPixelList;
	private int NumRegions;
	private int Imgheight;
	private int Imgwidth;

	static int edgecount = 0;
	// 0 black
	// 255 white
	private final int Threshhold = 150;
	private final int MAXSUPERPIXELAMOUNT = 16; // edges are usually 2 pixels on each end so 4 pixels per "Block"
	private Vertex<SuperPixel> startVertex = null;
	private Vertex<SuperPixel> endVertex = null;
 
	/**
	 * assume image passed though is already grey scaled
	 * @param FileName the filename
	 */
	public Graph(String FileName) {

		BufferedImage ReadGrey = GreyScaleImage(FileName); // convert image to greyscale

		Imgheight = ReadGrey.getHeight(); // get height
		Imgwidth = ReadGrey.getWidth(); // get width
		SuperPixelList = new GraphADT.ArrayList<Vertex<SuperPixel>>(); // initialise the Adjacency List(list of
																// vertices/superpixels)

		boolean[][] EdgesFound = EdgeDetect(ReadGrey); // locate all the edges in the image(all walls and out of bounds
														// areas)

		ConstructConnectedGraph(EdgesFound, ReadGrey); // Use the edges and grey scale to construct superpixels and connect
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
		
		// Create a copy of the original image to overlay edges
		BufferedImage edgeOverlay = new BufferedImage(rgbImage.getWidth(), rgbImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics g = edgeOverlay.getGraphics();
		g.drawImage(rgbImage, 0, 0, null); // Draw the original image as the background

		// Iterate through the list of edges and draw each edge

		for (Vertex<SuperPixel> Sp : this.SuperPixelList) {
			for (Edge edge : Sp.EdgeList()) {
				// Get the 'from' and 'to' vertices of the edge
				@SuppressWarnings("unchecked")
				Vertex<SuperPixel> fromVertex = edge.getVertFrom();
				@SuppressWarnings("unchecked")
				Vertex<SuperPixel> toVertex = edge.getVertTO();

				// Extract coordinates of each vertex (centroids)
				int fromX = fromVertex.GetElement().getAvgPixelXPos();
				int fromY = fromVertex.GetElement().getyAvgPixelYPos();
				int toX = toVertex.GetElement().getAvgPixelXPos();
				int toY = toVertex.GetElement().getyAvgPixelYPos();

				// Draw a red line between the 'from' and 'to' vertices
				g.setColor(Color.blue);
				g.drawLine(fromX, fromY, toX, toY);

				System.err.println(edge.getWeight());
			}

		}

		g.dispose(); // Clean up graphics

		// Save final image
		try {
			System.out.println("makeing image for " + FileName);
			ImageIO.write(edgeOverlay, "png", new File("Output_maze.png"));
			System.out.println("Image made");
		} catch (IOException e) {
			e.printStackTrace();
		}

		findStartAndEnd();
	}

	/**
	 * Finds and sets the start and end SuperPixels based on their Pixel positions.
	 * The start SuperPixel is the first one found that has a Pixel at the left edge (x = 0).  
	 * The end SuperPixel is the first one found that has a Pixel at the right edge (x = Imgwidth - 1).
	 * Once both are found, the method stops searching.
	 */
	public void findStartAndEnd() {
	    // Goes through every SuperPixel in the list
	    for (Vertex<SuperPixel> v : SuperPixelList) {
	        // Goes through every Pixel in the current SuperPixel
	        for (Pixel p : v.GetElement().getAllPixels()) {
	            
	            // Checks if this Pixel is on the left edge of the image
	            if (startVertex == null && p.getXPos() == 0) {
	                startVertex = v; // Set this SuperPixel as the start
	            }

	            // Checks if this Pixel is on the right edge of the image
	            if (endVertex == null && p.getXPos() == Imgwidth - 1) {
	                endVertex = v; // Set this SuperPixel as the end
	            }

	            // If both start and end are found, stop searching
	            if (startVertex != null && endVertex != null) {
	                return;
	            }
	        }
	    }
	}



	/**
	 * Makes a path shorter by keeping only some of the points.
	 * This method goes through the given path and keeps every 4th SuperPixel 
	 * (or whatever value is set in STEP) to make the path simpler.
	 * It also makes sure the last point in the original path is always included, 
	 * even if it was skipped.
	 * @param path The original list of SuperPixel vertices (the full path)
	 * @return A shorter version of the path
	 */
	public GraphADT.ArrayList<Vertex<SuperPixel>> simplifyPath(GraphADT.ArrayList<Vertex<SuperPixel>> path) {
	    // If the path has fewer than 2 points, return it as is - would'nt make sense to skip pixels if our path is that short
	    if (path.size() < 2) return path;

		GraphADT.ArrayList<Vertex<SuperPixel>> simple = new GraphADT.ArrayList<Vertex<SuperPixel>>();

	 // Only keep every 4th point from the path - (i was trying to minimise/reduce the  lines in the path)
	    int STEP = 4;  

	    // Go through the path, jumping by STEP each time
	    for (int i = 0; i < path.size(); i += STEP) {
	        simple.add(path.get(i)); // Add the selected point to the simplified path
	    }

	 // Get the last point in the original path - there'll be cases where we skip over the end vertex 
	 //so we're trying to make sure that our path is connected from start - middle to end
	    Vertex<SuperPixel> last = path.get(path.size() - 1); 

	    // If the last point was not already added, add it now
	    if (simple.get(simple.size() - 1) != last) {
	        simple.add(last);
	    }

	 // Return the simplified path
	    return simple; 
	}

	
	/**
	 * Finds a path from the start SuperPixel to the end SuperPixel using breadth-first search (BFS).
	 * The method looks for the shortest path by checking all possible paths, step by step.
	 * It returns the first path that reaches the end SuperPixel.
	 * @return A list of SuperPixel vertices that form the path from start to end.
	 *         If no path is found or start/end is missing, returns an empty list.
	 */
	public GraphADT.ArrayList<Vertex<SuperPixel>> findPath() {

	    // If start or end is not set, return an empty path
	    if (startVertex == null || endVertex == null) 
	    {
	        return new GraphADT.ArrayList<Vertex<SuperPixel>>();
	    }

	    // A queue to store paths to explore
		LinkedQueue<GraphADT.ArrayList<Vertex<SuperPixel>>> queue = new LinkedQueue<>();

	    // A list to keep track of visited vertices
	    GraphADT.ArrayList<Vertex<SuperPixel>> visited = new GraphADT.ArrayList<Vertex<SuperPixel>>();
	    // Start a new path from the start vertex
	    GraphADT.ArrayList<Vertex<SuperPixel>> startPath = new GraphADT.ArrayList<>();
	    startPath.add(startVertex);
	    queue.Enqueue(startPath);        // Add the starting path to the queue
	    visited.add(startVertex);        // Mark the start vertex as visited

	    // Keep going while there are paths to check
	    while (!queue.isEmpty()) {


	        // Take the next path from the queue
			GraphADT.ArrayList<Vertex<SuperPixel>> path = queue.Dequeue();

	        // Get the last vertex in the current path
	        Vertex<SuperPixel> v = path.get(path.size() - 1);

	        // If we reached the end vertex, return the path
	        if (v == endVertex) {
	            return path;
	        }

	        // Check all edges connected to this vertex
	        for (Edge<SuperPixel> e : v.EdgeList()) {


	            Vertex<SuperPixel> w;

	            // Get the vertex on the other side of the edge
	            if (e.getVertFrom() == v) {
	                w = e.getVertTO();
	            } else {
	                w = e.getVertFrom();
	            }

	            // If we haven't visited this vertex yet
	            if (!visited.contains(w)) {
	                visited.add(w); // Mark it as visited

	                // Create a new path that includes this vertex
					GraphADT.ArrayList<Vertex<SuperPixel>> newPath = new GraphADT.ArrayList<Vertex<SuperPixel>>(path);
	                newPath.add(w);

	                // Add the new path to the queue to be explored later
	                queue.Enqueue(newPath);
	            }
	        }
	    }

	    // If no path is found, return an empty list
		return new GraphADT.ArrayList<Vertex<SuperPixel>>();
	}


	/**
	 * Method for getting the starting vertex
	 * @return startVertex
	 */
	public Vertex<SuperPixel> getStartVertex() {
		return startVertex;
	}


	/**
	 * Method for getting the end vertex
	 * @return endVertex
	 */
	public Vertex<SuperPixel> getEndVertex() {
		return endVertex;
	}
	
	public List<Vertex<SuperPixel>> getVertices(){
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
	 * @param b Next Vertex  Vertex were adding edge to
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
	 * @param SPId ID of the superPixel to retrieve
	 * @return A Vertext containing the found superPixel
	 */
	private Vertex<SuperPixel> SearchSuperPixelList(int SPId) {

		for (Vertex<SuperPixel> V : SuperPixelList) {
			if (SPId == V.GetElement().getId()) {
				return V;
			} else {
				continue;
			}
		}
		return null;
	}

	/**
	 * Take in an image and greyscale it
	 * @param Filename Filename with path of the image to greyscale
	 * @return GreyScaled image
	 */
	private BufferedImage GreyScaleImage(String Filename) {

		try {
			File file = new File(Filename);
			System.out.println("Trying to read: " + file.getAbsolutePath());
			System.out.println("Exists? " + file.exists());
			BufferedImage ColourImage = ImageIO.read(file);

			BufferedImage GreyScale = new BufferedImage(ColourImage.getWidth(), ColourImage.getHeight(),
					BufferedImage.TYPE_BYTE_GRAY);

			Graphics DrawGrey = GreyScale.getGraphics();

			DrawGrey.drawImage(ColourImage, 0, 0, null);

			DrawGrey.dispose();

			File output = new File("Converted/GreyImage.png");
			output.getParentFile().mkdirs();
			ImageIO.write(GreyScale, "png", output);

			return GreyScale;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Take an image in and locate the borders/edges within it (where a large change in intensity occurs)
	 * @param img Image To Find borders for
	 * @return A 2D array maping out the image with borders marked off
	 */
	private boolean[][] EdgeDetect(BufferedImage img) {

		// -1 for edges
		// 1 for no edge
		// 0 otherwise

		boolean[][] DetectedEdges = new boolean[Imgheight][Imgwidth];

		for (int y = 0; y < Imgheight; y++) {
			for (int x = 0; x < Imgwidth; x++) {
				// C,R because it wants x then Y
				int CPixelIntensity = new Color(img.getRGB(x, y)).getRed(); // greyscale has only intensity so the value
				// the same for all 3 colour is same

				// Check if pixels are on the edges of the image
				// if they arent can do the normal operations
				if (IsInImage(y, x)) {

					// Get 6 pixels that are adjacent to current pixel
					int[] AdjacentPixels = GetAdjacentIntesities(img, y, x);

					// Used to check if an edge was found if it was use it to place wall edges
					boolean EdgeDtected = false;
					for (int i = 0; i < 6; i++) {

						// If theres a large intensity change we've found a path-wall border
						if (Math.abs(CPixelIntensity - AdjacentPixels[i]) > Threshhold) {

							EdgeDtected = true;
							break;
						}
					}
					if (EdgeDtected == true) {

						DetectedEdges[y][x] = true;

					} else {

						DetectedEdges[y][x] = false;

					}

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
	 * Create all the vertices of the graph and add edges between valid vertices
	 * @param DetectedEdges Array of all entries marked as borders/edges 
	 * @param img Image beign worked with
	 */
	private void ConstructConnectedGraph(boolean[][] DetectedEdges, BufferedImage img) {
		boolean[][] visitedIndices = new boolean[this.Imgheight][this.Imgwidth]; //  Mark the entries that have already been visited
		int[][] pixelToSuperMap = new int[this.Imgheight][this.Imgwidth]; // Create ID blobs for superpixels to know start and end Pos quickly
		// that pixel

		for (int y = 0; y < this.Imgheight; y++) {
			for (int x = 0; x < this.Imgwidth; x++) {

				if (DetectedEdges[y][x] == false && visitedIndices[y][x] == false) {
				
					// Call method that takes in the current X and Y location,the visted pixel,the detected borders and the map
					// Uses them to create a SuperPixel
					SuperPixel toAdd = GrowSuperPixel(x, y, DetectedEdges, visitedIndices, img, pixelToSuperMap);

					addSuperPixel(toAdd); //Add superPixel to adjacency list

				}
			}
		}

		boolean[][] vistedEdgeConstruction = new boolean[Imgheight][Imgwidth]; //Pixels that have already been visited when constructing edges
		for (int r = 0; r < this.Imgheight; r++) {
			for (int c = 0; c < this.Imgwidth; c++) {

				if (!vistedEdgeConstruction[r][c] && pixelToSuperMap[r][c] != -1) {
					ConstructEdges(c, r, pixelToSuperMap, vistedEdgeConstruction, img); //build the edges between all the relevant vertices
				}
			}
		}
	}
	
	
	/**
	 * BFS run to create edges between vertices that are adjacent to one another
	 * Also keeps number of edges to a min by not connecting every vertex to every other vertex
	 * Adjacnet to it provided the vertex it already connects to can connect to the other vertex
	 * @param initX Pixel  X  (the SuperPixel containing this pixel) Position to start creating edges from
	 * @param initY Pixel  Y  (the SuperPixel containing this pixel) Position to start creating edges from
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

			
			//Check 8 adjacent Pixels
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

					int neigbouringSpid = superPixelMap[NewY][NewX]; //ID of the neighbouring SuperPixel

					// Different ID's mean that its a different SuperPixel
					// Therefore check if an edge already exists and if it 
					// Does not then skip adding an edge between it
					if (neigbouringSpid != currSpId && !ValidateEdgeExistence(currSpId, neigbouringSpid)) {
						
						//Get a SueperPixel Via its ID 
						Vertex<SuperPixel> toAddEdgeA = SearchSuperPixelList(currSpId);
						Vertex<SuperPixel> toAddEdgeB = SearchSuperPixelList(neigbouringSpid);

						// Ensure both vertices are'nt null
						// Add an edge if they arent
						if (toAddEdgeA != null && toAddEdgeB != null) {
							addEdge(toAddEdgeA, toAddEdgeB);
						}

					}

					// If the ID's arent the same and the Pixel Has not Yet been visited in this process
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
	 * Run a Breadth First Search from inital x and y pixel position adding more and more
	 * Pixels to a superpixel that will be turned into a vertex for the graph
	 * To be added a pixel has to not be visited and not be a border
	 * Superpixels have a max size of 4x4 or 16 pixels
	 * @param InitX  Starting pixel X postion
	 * @param InitY Starting pixel Y postion
	 * @param EdgeCollection 2D array containing all the entries where a border exists
	 * @param visitedCollection 2D array containing all viisted pixel entries
	 * @param img Image currently being worked with 
	 * @param 2D array containing ids that form regions of the superpixels (used to mark where one superpixel starts and another begins)
	 * @return The SuperPixel Created
	 */
	private SuperPixel GrowSuperPixel(int InitX, int InitY, boolean[][] EdgeCollection, boolean[][] visitedCollection,
			BufferedImage img, int[][] superPixelMap) {

		LinkedQueue<Pixel> queue = new LinkedQueue<Pixel>(); //Create queue to use for BFS
		SuperPixel SP = new SuperPixel(); //Initial SuperPixel creation

		Pixel InitPix = new Pixel(InitX, InitY, (new Color(img.getRGB(InitX, InitY)).getRed())); //get the initial pixel to add 
		queue.Enqueue(InitPix); //add to the queue
		visitedCollection[InitY][InitX] = true; //mark it as visited
		SP.AddPixel(InitPix); //add Pixel to SuperPixl "blob"
		superPixelMap[InitY][InitX] = SP.getId(); // Mark the position of the pixel as under the current SuperPixels ID

		// Check if the queue is not empty and if the superpixel is not at its maximum size 
		// If it isnt take the current pixel at the top of the queue
		// Get its valid Neighbouring pixels(those not yet visited and those that arent borderss)
		// Load all of those pixels into the queue to be visited
		// Mark those added to the queue as visited 
		// Mark them under the ID of the current SuperPixel being made
		// Finally add all of them to the SuperPixel
		while (!queue.isEmpty() && SP.SuperPixelSize() < MAXSUPERPIXELAMOUNT) {
			Pixel CurrentPix = queue.Dequeue();

			SingleLinkedList<Pixel> ValidNeighboursList = GetAdjacentPixels(CurrentPix.getXPos(), CurrentPix.getYPos(),
					EdgeCollection, visitedCollection, img, superPixelMap);

			for (Pixel P : ValidNeighboursList) {
				 if(SP.SuperPixelSize() >=MAXSUPERPIXELAMOUNT) {
					 break; //Dont add over the allowed amount limit to avoid potential issues in a different dataset (added 10 may 2025)
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
	 * Given the current postion of a pixel check pixels adjacent to it and add them to a list
	 * To be used as part of a new super pixel should they pass the criteria of not being an edge/border and not
	 * being visited yet
	 * @param Xpos Current  pixel X postion
	 * @param YPos Current pixel Y position
	 * @param Edges/borders Array containing locations of edges/borders and paths (True for wall false for path)
	 * @param visitedPixels Pixels that have already been visited and are already in other Superpixels 
	 * @param img The image being worked with
	 * @param spMap 2D array containing each superpixels ID taking up an "area" e.g 2x2 blob with id=4
	 * @return List of Pixels surrounding current pixel that are valid for being added to a new superpixel
	 */
	private SingleLinkedList<Pixel> GetAdjacentPixels(int Xpos, int YPos, boolean[][] Edges, boolean[][] visitedPixels,
			BufferedImage img, int[][] spMap) {

		SingleLinkedList<Pixel> ToReturn = new SingleLinkedList<Pixel>();
		
		//loop starts at top left goes to bottom right
		//going along x for each row
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

	/**
	 * Get the pixel intensities of the 8 pixels around pixel youre currently at
	 * @param img The image you're working with
	 * @param y Current Y position
	 * @param x Current X position
	 * @return Array containing the intensities of the surrounding 8 pixels
	 */
	private int[] GetAdjacentIntesities(BufferedImage img, int y, int x) {

		int TopLeftIntensity = new Color(img.getRGB(x - 1, y + 1)).getRed();
		int TopRightIntensity = new Color(img.getRGB(x + 1, y + 1)).getRed();
		int MiddleLeftIntensity = new Color(img.getRGB(x - 1, y)).getRed();
		int MiddleRightIntensity = new Color(img.getRGB(x + 1, y)).getRed();
		int BottomLeftIntensity = new Color(img.getRGB(x - 1, y - 1)).getRed();
		int BottomRightIntensity = new Color(img.getRGB(x + 1, y - 1)).getRed();

		int[] AdjacentPixels = { TopLeftIntensity, TopRightIntensity, MiddleLeftIntensity, MiddleRightIntensity,
				BottomLeftIntensity, BottomRightIntensity };

		return AdjacentPixels;
	}

	/**
	 * Check to see if at your current position will moving in any of the 8 directions
	 * around you goes out of the "World"
	 * @param CRow Current Pixel Row
	 * @param CCol Current Pixel Column
	 * @return  True or false based on if move goes out of world
	 */
	private boolean IsInImage(int CRow, int CCol) {

		if (CRow + 1 > this.Imgheight - 1 || CRow - 1 < 0) {
			return false;
		} else if (CCol + 1 > this.Imgwidth - 1 || CCol - 1 < 0) {
			return false;
		}
		return true;
	}
}

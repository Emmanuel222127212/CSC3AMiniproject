package GraphADT;
public class Edge<T> {

	
	private double weight;
	private Vertex<T> VertFrom;
	private Vertex<T> VertTO;
	
	
	public Edge(Vertex<T> To,Vertex<T> From) {
		
		if(To!=null && From!=null ) {
			this.VertTO=To;
			this.VertFrom=From;
			
			SuperPixel Temp1 = (SuperPixel) VertFrom.GetElement();
			SuperPixel Temp2 = (SuperPixel) VertTO.GetElement();
			
			double SquaredX = Math.pow((Temp1.getAvgPixelXPos()-Temp2.getAvgPixelXPos()), 2);
			double SquaredY = Math.pow((Temp1.getyAvgPixelYPos()-Temp2.getyAvgPixelYPos()), 2);

			this.weight= Math.abs(Math.sqrt(SquaredX+SquaredY));
			
		}else {
			System.err.println("Invalid Vertices");
			
		}
		
	}
	public double getWeight() {
		return weight;
	}

	public Vertex<T> getVertFrom() {
		return VertFrom;
	}

	public Vertex<T> getVertTO() {
		return VertTO;
	}

	//check if the vertex to (a) is the same
	public boolean ValidateVertices(Vertex<T> a,Vertex<T> b) {
		
		if((a==VertTO || a == VertFrom) && (b==VertFrom || b==VertTO))
			return true;
		return false;
		
	}
}

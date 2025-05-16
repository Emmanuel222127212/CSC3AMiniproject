package GraphADT;
import java.util.ArrayList;
import java.util.List;

public class  Vertex <T> implements Postion<T> {

	
	private T element;
	private List<Edge<T>> OutgoingEdges;
	private	int ListSize; 
	
	
	public Vertex(T Value){
		this.element=Value;
		
		this.OutgoingEdges=new ArrayList<>();
		ListSize=0;
	}
	
	
	public void AddEdge(Edge<T> Toadd ) {
		
		if(Toadd!=null) {
		
		
		this.OutgoingEdges.add(Toadd);
		ListSize++;
		}
		else {
			System.err.println("Unable to add edge to vertex containing" +this.element.toString());
		}
	}
	
	public List<Edge<T>> EdgeList(){
		return this.OutgoingEdges;
	}

	@Override
	public T GetElement() {
		// TODO Auto-generated method stub
		return this.element;
	}
	public int getSize() {
		return this.ListSize;
	}


}

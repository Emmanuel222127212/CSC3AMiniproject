package GraphADT;
public class Node<T> {

	private Node<T> nxtNode;
	private T item;
	
	public Node(Node<T> Nxt,T Element) {
		
		this.nxtNode=Nxt;
		this.item=Element;
	}
	
	
	public Node<T> Next() {
		
		return this.nxtNode;
	}
	
	public void SetNext(Node<T> Nxt) {
		this.nxtNode = Nxt;
	}
	
	public T GetData() {
		return this.item;
	}
}

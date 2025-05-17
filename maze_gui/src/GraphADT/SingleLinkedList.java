package GraphADT;
import java.util.Iterator;

public class SingleLinkedList<T>  implements Iterable<T>{

	public Node<T> Head;
	public Node<T> Tail;
	private int size;

	public SingleLinkedList() {
		this.Tail = null;
		this.Head =null;
		size = 0;
	}

	public Node<T> getHead() {
		return Head;
	}
	public void Addlast(T item) {
		Node<T> Newest = new Node<T>(null, item);

		if (isEmpty()) {
			Head = Newest;
			Tail=Newest;

		} else {
			Tail.SetNext(Newest);
			Tail = Newest;
		}
		
		size++;
	}

	public T First() {

		if (size != 0) {
			return this.Head.GetData();
		}else {
			return null;
		}
	}

	public T RemoveFirst() {

		if (isEmpty()) {
			return null;
		}
		T DataRemoved=Head.GetData();
		Node<T> Temp = Head.Next();
		this.Head.SetNext(null);// garbage collector handles this
		this.Head = Temp;
		if(Head==null) {
			Tail=null; //in the event of a single item in the list
					   //go back to intial construction state
		}
		
		size--;

		return DataRemoved;

	}
	public T Remove(Node<T> toRemove) {
		if(isEmpty() || toRemove==null) {
			return null;
		}
		
		if(toRemove==Head) {
			RemoveFirst();
		}
		
		Node<T> temp= this.Head;
		while (temp!=null && temp.Next()!=toRemove) {
			temp=temp.Next();
		
		}
		if(temp==null || temp.Next()!=toRemove) {
			return null;
		}
		
		
		Node<T> removalNode = temp.Next();
		temp.SetNext(removalNode.Next());
		
		if(removalNode==Tail) {
			Tail=temp;
		}
		
		removalNode.SetNext(null);
		size--;
		
		
		return removalNode.GetData();
	}

	public boolean isEmpty() {
		if (size == 0) {

			return true;
		} else {
			return false;
		}
	}
	
	public int getSize() {
		return this.size;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return new SingleLinkedListIterator(this);
	}


	private class SingleLinkedListIterator implements Iterator<T>{
		
		private Node<T> Current;
		
		public SingleLinkedListIterator(SingleLinkedList<T> List){
			Current=List.getHead();
		}
		
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			
			return Current!=null;
		}

		@Override
		public T next() {
			// TODO Auto-generated method stub
			
			T data=Current.GetData();
			Current=Current.Next();
			return data;
			
		}
		
	}

}

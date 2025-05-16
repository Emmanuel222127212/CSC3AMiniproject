package GraphADT;
public class LinkedQueue<T>  implements IQueue<T>{

	private SingleLinkedList<T> SList;
	
	public LinkedQueue() {
		SList=new SingleLinkedList<T>();
	}
	
	@Override
	public void Enqueue(T Val) {
		// TODO Auto-generated method stub
		SList.Addlast(Val);
		
		
	}

	@Override
	public T Dequeue() {
		// TODO Auto-generated method stub
		
		T removedItem;
		removedItem=SList.RemoveFirst(); //can get a null if nothing is left in the queue
		
	
		return removedItem;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		
	
		return SList.isEmpty();
	}

	@Override
	public int Size() {
		// TODO Auto-generated method stub
		return SList.getSize();
	}

	@Override
	public T First() {
		// TODO Auto-generated method stub
		
		T firstItem;
		
		firstItem=SList.First();
		return firstItem;
	}

}

package GraphADT;
public interface IQueue<T> {
 
	
	
	public void Enqueue(T Val);
	
	public T Dequeue();
	
	public boolean isEmpty();
	
	public int Size();
	
	public T First();
}

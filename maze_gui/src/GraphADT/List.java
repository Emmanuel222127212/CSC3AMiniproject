package GraphADT;

public interface List<T> {

	public T get(Integer index);
	public void set(Integer i,T element);
	public T remove(Integer index);
	public void add(Integer index,T element);
	public void add(T element);
	
	public int size();
	public boolean isEmpty();
	
}

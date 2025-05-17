package GraphADT;

import java.util.Iterator;

public class ArrayList<T> implements List<T>, Iterable<T> {

	
	private T[] array;
	private int size; //current number of elements
	private int Capacity = 10;


	public ArrayList() {
		this.size=0;
		array=createArray(Capacity);
	}
	
	
	
	
	
	
	public ArrayList(ArrayList<T> List) {
		// TODO Auto-generated constructor stub
		this.array=createArray(List.size+1);
		
		for (int i = 0; i < List.size(); i++) {
			this.array[i]=List.get(i);
			
		}
	}






	@SuppressWarnings("unchecked")
	private T[] createArray(int arraySize) {
		

		T[] temp = (T[]) (new Object[arraySize]);
		return temp;
	}
	
	private void growArray() {
		Capacity=Capacity*2; //double capacity of the array
		T[] newArray = createArray(Capacity);
		
		System.arraycopy(this.array, 0, newArray, 0, size); //set adt array to newarray with adjusted size
		this.array=newArray;
	}
	
	private void shiftElementsRight(int index) {
		
		for (int i = this.size; i >=index; i--) {
				//dont need to expand as this is alays called after expansion
					array[i]=array[i-1];
			
		}
	}
	private void shiftElementsLeft(int index) {
		
		for (int i = index; i < size; i++) {
				this.array[i]=this.array[i+1];
			
		}
		
	}
	
	public boolean contains(T element) {
		// TODO Auto-generated method stub
		if(size!=0) {
		for (int i = 0; i < size; i++) {
			if(array[i]==element) {
				return true;
			}
		}
		}
		return false;
	}
	
	
	@Override
	public T get(Integer index) {
		// Need to create exception classes
		if(index>=size || index<0) {
			
			System.err.println("Outside bounds of List");
			return null;
		}else if (index<0) {
			System.err.println("Index Out of Range");
			return null;
		}
		
		return array[index];
	}

	@Override
	public void set(Integer index, T element) {
		// Need to create exception classes
		if(index+1>=size) {
			
			System.err.println("Outside bounds of List");
	
		}else if (index<0) {
			System.err.println("Index Out of Range");

		}
		
		 array[index]= element;
		
	}
	
	@Override
	public void add(Integer index, T element) {
	
		if(isEmpty()) {
			this.array[0]=element;
			size++;
			return;
		}
		
		if(size==Capacity) {
			growArray();
		}
		if(array[index]!=null) {
			shiftElementsRight(index);
			
		}
		
		this.array[index] = element;
		size++;
	}


	@Override
	public void add(T element) {
		
		add(size, element);
		
	}


	@Override
	public T remove(Integer index) {
		// TODO Auto-generated method stub
		T toRemove=null;
		
		if(index>=0 && index<Capacity) {
			if(array[index]!=null) {
				
				toRemove=this.array[index];
				this.array[index]=null;
				shiftElementsLeft(index);
				size--;
			}else {
				System.err.println("Trying to remove from index storing nothing");
			}
		}
		
		return toRemove;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		
		if(size==0) {
			return true;
		}
		return false;
	}
	
	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return new ArrayListIterator(this);
	}




	private class ArrayListIterator implements Iterator<T>{
		
		private ArrayList<T> list;
		private int cursor=0;
		
		public ArrayListIterator(ArrayList<T> list){
			this.list=list;
		
		}

		@Override
		public boolean hasNext() {
			if(cursor<list.size()) {
			return true;
			}else {
				return false;
			}
		}

		@Override
		public T next() {
			// TODO Auto-generated method stub
			T item =list.get(cursor);
			cursor++;
			return item;
		}
		
	}












}

package GraphADT;

import java.util.Iterator;

public class HashTable<K, V> implements IMap<K, V> {

	private Object[] table;
	private int size;
	private int capacity;

	private final double LOADFACTORMAX = 0.65;

	public HashTable() {
		this(100);
	}

	public HashTable(int initialSize) {

		this.capacity = initialSize;
		this.table = createArray(capacity);

	}

	private Object[] createArray(int size) {
		this.table = new Object[size];

		for (int i = 0; i < size; i++) {
			this.table[i] = new SingleLinkedList<Entry<K, V>>();

		}
		return table;
	}

	@SuppressWarnings("unchecked")
	private void growArray() {
		int oldCapacity = capacity;
		capacity = (capacity * 2) + 1; // double capacity of the array and add one so mod is odd

		Object[] newArray = new Object[capacity];

		for (int i = 0; i < capacity; i++) {
			newArray[i] = new SingleLinkedList<Entry<K, V>>();
		}

		for (int i = 0; i < oldCapacity; i++) {
			SingleLinkedList<Entry<K, V>> prevList= (SingleLinkedList<Entry<K,V>>)table[i];
			
			for (Entry<K, V> entry : prevList) {
				int index = hash(entry.getKey());

				((SingleLinkedList<Entry<K, V>>) newArray[index]).Addlast(entry);
				;
			}

		}
		this.table=newArray;

	}

	private int hash(K key) {

		return Math.abs(((Integer) key) % capacity);
	}

	@Override
	public void put(K key, V value) {

		int index = hash(key);

		//problems where ans is always 0 so cast to double
		if ((double)size / capacity >= LOADFACTORMAX) {
			growArray();
		}

		@SuppressWarnings("unchecked")
		SingleLinkedList<Entry<K, V>> SLL = (SingleLinkedList<Entry<K, V>>) table[index];

		for (Entry<K, V> entry : SLL) {
			if (entry.getKey().equals(key)) {

				entry.setValue(value);
				return;
			}
		}

		SLL.Addlast(new Entry<K, V>(key, value));
		size++;

	}

	@Override
	public V get(K key) {
		int index = hash(key);

		@SuppressWarnings("unchecked")
		SingleLinkedList<Entry<K, V>> SLL = (SingleLinkedList<Entry<K, V>>) table[index];

		if (SLL == null || SLL.isEmpty()) {
			return null;
		}
		for (Entry<K, V> entry : SLL) {
			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}
		}

		return null; // no key found
	}

	@Override
	public V remove(K key) {
		// TODO Auto-generated method stub

		int index = hash(key);

		@SuppressWarnings("unchecked")

		SingleLinkedList<Entry<K, V>> SLL = (SingleLinkedList<Entry<K, V>>) table[index];
		if (SLL == null || SLL.isEmpty()) {
			return null;
		}

		Node<Entry<K, V>> currentPos = SLL.getHead();

		while (currentPos != null) {

			Entry<K, V> entry = currentPos.GetData();

			if (entry.getKey().equals(key)) {
				Entry<K, V> removal = SLL.Remove(currentPos);
				return removal.getValue();
			} else {
				currentPos = currentPos.Next();
			}
		}

		return null;
	}

	@Override
	public boolean containsKey(K key) {
		
		int index=hash(key);
	
		SingleLinkedList<Entry<K, V>> SLL = (SingleLinkedList<Entry<K, V>>) table[index];
		for (Entry<K, V> entry : SLL) {
			if(entry.getKey().equals(key)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size == 0;
	}

}

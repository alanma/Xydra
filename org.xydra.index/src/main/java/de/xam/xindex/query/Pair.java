package de.xam.xindex.query;

/**
 * A tuple storing two objects.
 * 
 * @author dscharrer
 */
public class Pair<K, L> {
	
	private final K first;
	private final L second;
	
	public Pair(K first, L second) {
		this.first = first;
		this.second = second;
	}
	
	public K getFirst() {
		return this.first;
	}
	
	public L getSecond() {
		return this.second;
	}
	
	@Override
	public String toString() {
		return "(" + this.first + "," + this.second + ")";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Pair))
			return false;
		Pair p = (Pair)other;
		return (this.first == null ? p.first == null : this.first.equals(p.first))
		        && (this.second == null ? p.second == null : this.second.equals(p.second));
		
	}
	
	@Override
	public int hashCode() {
		return (this.first == null ? 0 : this.first.hashCode())
		        | (this.second == null ? 0 : this.second.hashCode());
	}
	
	public Pair<L,K> inverse() {
		return new Pair<L,K>(this.second, this.first);
	}
	
}

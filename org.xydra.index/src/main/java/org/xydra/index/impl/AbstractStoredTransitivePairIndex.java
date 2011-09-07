package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;
import org.xydra.index.ITransitivePairIndex;
import org.xydra.index.XI;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;



/**
 * Implementation of {@link ITransitivePairIndex} that calculates all implied
 * pairs and stores them internally.
 * 
 * While this allows fast lookup of implied pairs, in the worst case the number
 * of (stored) implied pairs may grow up to O((#pairs)^2), causing slow indexing
 * / deIndexing of pairs and high memory usage.
 * 
 * Worst case index(k1,k2) should be around O(#left * #right) and deIndex(k1,k2)
 * around O(#left * #right * #direct) where #left is the number of implied pairs
 * (*,k1), #right is the number of implied pairs (k2,*) and #direct = max{
 * number of defined pairs (k3,*) | (k3,k1) is implied }
 * 
 * @author dscharrer
 */
abstract public class AbstractStoredTransitivePairIndex<K> implements ITransitivePairIndex<K> {
	
	private static final long serialVersionUID = 6336234551713057933L;
	
	// defined pairs
	private final IPairIndex<K,K> direct;
	
	// implied pairs (including defined ones)
	protected transient IPairIndex<K,K> implied;
	private final Factory<IPairIndex<K,K>> fact;
	
	public AbstractStoredTransitivePairIndex(IPairIndex<K,K> direct,
	        Factory<IPairIndex<K,K>> implied) {
		this.direct = direct;
		this.fact = implied;
		this.implied = this.fact.createInstance();
	}
	
	@Override
    public boolean implies(Constraint<K> c1, Constraint<K> c2) {
		return this.implied.contains(c1, c2);
	}
	
	@Override
    public Iterator<Pair<K,K>> transitiveIterator(Constraint<K> c1, Constraint<K> c2) {
		return this.implied.constraintIterator(c1, c2);
	}
	
	@Override
    public Iterator<Pair<K,K>> constraintIterator(Constraint<K> c1, Constraint<K> c2) {
		return this.direct.constraintIterator(c1, c2);
	}
	
	@Override
    public boolean contains(Constraint<K> c1, Constraint<K> c2) {
		return this.direct.contains(c1, c2);
	}
	
	@Override
    public void clear() {
		this.direct.clear();
		this.implied.clear();
	}
	
	@Override
    public boolean isEmpty() {
		return this.direct.isEmpty();
	}
	
	@Override
    public void index(K k1, K k2) {
		
		if(completesCycle(k1, k2))
			throw new CycleException();
		
		this.direct.index(k1, k2);
		
		addImplied(k1, k2);
		
	}
	
	protected boolean completesCycle(K k1, K k2) {
		return XI.equals(k1, k2)
		        || implies(new EqualsConstraint<K>(k2), new EqualsConstraint<K>(k1));
	}
	
	/**
	 * Add all new pairs implied after the pair (k1,k2) has been added.
	 */
	abstract public void addImplied(K k1, K k2);
	
	@Override
    public void deIndex(K k1, K k2) {
		
		this.direct.deIndex(k1, k2);
		
		removeImplied(k1, k2);
		
	}
	
	/**
	 * Remove all obsolete implied pairs after pair (k1,k2) has been removed.
	 */
	abstract public void removeImplied(K k1, K k2);
	
	@Override
	public String toString() {
		return this.direct.toString();
	}
	
	/**
	 * Add implied pairs (k1,k) for all k = k2 or k is in implied pair (k2,k)
	 */
	protected boolean addAll(K k1, K k2) {
		
		if(implies(new EqualsConstraint<K>(k1), new EqualsConstraint<K>(k2)))
			return false;
		
		this.implied.index(k1, k2);
		
		Iterator<Pair<K,K>> right = constraintIterator(new EqualsConstraint<K>(k2),
		        new Wildcard<K>());
		while(right.hasNext())
			addAll(k1, right.next().getSecond());
		
		return true;
	}
	
	/**
	 * Called on deserialization, needs to restore transient members.
	 */
	private Object readResolve() {
		if(this.implied == null) {
			this.implied = this.fact.createInstance();
			for(Pair<K,K> pair : this.direct)
				addAll(pair.getFirst(), pair.getSecond());
		}
		return this;
	}
	
	@Override
    public Iterator<Pair<K,K>> iterator() {
		return constraintIterator(new Wildcard<K>(), new Wildcard<K>());
	}
	
	@Override
    public Iterator<K> key1Iterator() {
		return this.direct.key1Iterator();
	}
	
	@Override
    public Iterator<K> key2Iterator() {
		return this.direct.key2Iterator();
	}
	
}

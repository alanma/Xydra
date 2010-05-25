package de.xam.xindex.index.impl;

import java.util.Iterator;

import de.xam.xindex.index.IUniformTripleIndex;
import de.xam.xindex.iterator.TransformingIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.KeyKeyEntryTuple;


public class UniformTripleIndex<K> extends TripleIndex<K,K,K> implements IUniformTripleIndex<K> {
	
	private static final long serialVersionUID = 7121877986612175167L;
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @param projectedConstraint (1,2, or 3)
	 * @return
	 */
	
	public Iterator<K> getMatchingAndProject(Constraint<K> c1, Constraint<K> c2, Constraint<K> c3,
	        int projectedConstraint) {
		Iterator<KeyKeyEntryTuple<K,K,K>> tupleIterator = this.index_s_p_o_stmt.tupleIterator(c1,
		        c2, c3);
		TransformingIterator.Transformer<KeyKeyEntryTuple<K,K,K>,K> transformer;
		switch(projectedConstraint) {
		case 1:
			transformer = new TransformingIterator.Transformer<KeyKeyEntryTuple<K,K,K>,K>() {
				public K transform(KeyKeyEntryTuple<K,K,K> in) {
					return in.getKey1();
				}
			};
			break;
		case 2:
			transformer = new TransformingIterator.Transformer<KeyKeyEntryTuple<K,K,K>,K>() {
				public K transform(KeyKeyEntryTuple<K,K,K> in) {
					return in.getKey2();
				}
			};
			break;
		case 3:
			transformer = new TransformingIterator.Transformer<KeyKeyEntryTuple<K,K,K>,K>() {
				public K transform(KeyKeyEntryTuple<K,K,K> in) {
					return in.getEntry();
				}
			};
			break;
		default:
			throw new AssertionError("projectedConstraint must be 1=s, 2=p, 3=o");
		}
		
		return new TransformingIterator<KeyKeyEntryTuple<K,K,K>,K>(tupleIterator, transformer);
	}
	
}

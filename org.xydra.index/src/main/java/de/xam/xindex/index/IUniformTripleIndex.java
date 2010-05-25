package de.xam.xindex.index;

import java.util.Iterator;

import de.xam.xindex.query.Constraint;


/**
 * An {@link ITripleIndex} with a uniform key structure -- all keys have the
 * same type.
 * 
 * @param <K>
 */
public interface IUniformTripleIndex<K> extends ITripleIndex<K,K,K> {
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @param projectedConstraint (1,2, or 3)
	 * @return an Iterator<K> over all keys of the given projected constraint.
	 *         This is like choosing among the queries (k,*,*), (*,k,*), and
	 *         (*,*,k).
	 */
	
	Iterator<K> getMatchingAndProject(Constraint<K> c1, Constraint<K> c2, Constraint<K> c3,
	        int projectedConstraint);
}

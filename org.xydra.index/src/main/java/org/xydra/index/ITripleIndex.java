package org.xydra.index;

import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.ITriple;

import java.util.Iterator;

/**
 * An index for triples of keys that supports queries on all three keys. Indexes
 * three keys (to boolean, either a key combination is there or not).
 * 
 * The same concept for two-tuples instead of three-tuples is called
 * {@link IPairIndex}.
 * 
 * @param <K>
 *            key type
 * @param <L>
 *            key type
 * @param <M>
 *            key type
 */
public interface ITripleIndex<K, L, M> extends IIndex, ITripleSink<K, L, M> {

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return true if there is a triple in the index where s matches c1, p
	 *         matches c2 and o matches c3
	 */
	boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param s
	 *            @NeverNull
	 * @param p
	 *            @NeverNull
	 * @param o
	 *            @NeverNull
	 * @return true iff
	 */
	boolean contains(K s, L p, M o);

	/**
	 * Remove the given triple from the index, if it was present
	 * 
	 * @param s
	 * @param p
	 * @param o
	 */
	void deIndex(K s, L p, M o);

	/**
	 * Dump the contents to Xydra Logging as log.info(...)
	 */
	void dump();

	/**
	 * Add the given triple to the index
	 * 
	 * @param s
	 * @param p
	 * @param o
	 */
	@Override
	void index(K s, L p, M o);

	/**
	 * @param c1
	 *            @NotNull
	 * @param c2
	 *            @NotNull
	 * @param c3
	 *            @NotNull
	 * @return an iterator with all triples matching the given constraints
	 */
	Iterator<ITriple<K, L, M>> getTriples(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param other
	 * @return the diff between this index and the other index
	 */
	IMapMapSetDiff<K, L, M> computeDiff(ITripleIndex<K, L, M> other);

}

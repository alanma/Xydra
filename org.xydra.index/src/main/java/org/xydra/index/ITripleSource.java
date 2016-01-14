package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.ITriple;

/**
 * A read-only triple index, for triples of keys that supports queries on all three keys. Indexes three keys (to
 * boolean, either a key combination is there or not).
 *
 * The same concept for two-tuples instead of three-tuples is called {@link IPairIndex}.
 *
 * @param <K> key type
 * @param <L> key type
 * @param <M> key type
 */
public interface ITripleSource<K, L, M> extends IIndex, ITripleIterable<K, L, M> {

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return true if there is a triple in the index where s matches c1, p matches c2 and o matches c3
	 */
	boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param s @NeverNull
	 * @param p @NeverNull
	 * @param o @NeverNull
	 * @return true iff
	 */
	boolean contains(K s, L p, M o);

	/**
	 * Dump the contents to Xydra Logging as log.info(...)
	 *
	 * @return an empty String, just so that dump() can be used in
	 *
	 *         <pre>
	 * assert foo : dump()
	 *         </pre>
	 */
	String dump();

	/**
	 * @param c1 constraint for component 1 of triple (subject) @NeverNull
	 * @param c2 constraint for component 2 of triple (property) @NeverNull
	 * @param c3 constraint for component 3 of triple (object) @NeverNull
	 * @return an {@link Iterator} over all {@link ITriple} that match the given constraints
	 */
	Iterator<ITriple<K, L, M>> getTriples(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param s @CanBeNull to denote a wildcard
	 * @param p @CanBeNull to denote a wildcard
	 * @param o @CanBeNull to denote a wildcard
	 * @return an iterator with all triples matching the given constraints
	 */
	Iterator<ITriple<K, L, M>> getTriples(K s, L p, M o);

}

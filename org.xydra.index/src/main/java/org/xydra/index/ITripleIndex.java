package org.xydra.index;

import java.util.Iterator;

/**
 * An index for triples of keys that supports queries on all three keys. Indexes three keys (to boolean, either a key
 * combination is there or not).
 *
 * The same concept for two-tuples instead of three-tuples is called {@link IPairIndex}.
 *
 * @param <K> key type
 * @param <L> key type
 * @param <M> key type
 */
public interface ITripleIndex<K, L, M> extends IIndex, IRemovableTripleSink<K, L, M>, ITripleSource<K, L, M> {

	/**
	 * @return a distinct iterator over all objects
	 */
	Iterator<M> getObjects();

	/**
	 * @param s @NeverNull
	 * @param p @NeverNull
	 * @return an iterator over all objects matching (s,p,*)
	 */
	Iterator<M> getObjects_SPX(final K s, final L p);

	/**
	 * @return a distinct iterator over all used predicates
	 */
	Iterator<L> getPredicates();

	/**
	 * @param s @NeverNull
	 * @return a distinct iterator over all predicates ?p occurring in triples (s,?p,*) @NeverNull
	 */
	Iterator<L> getPredicates_SX(final K s);

	/**
	 * @param s @NeverNull
	 * @param o @NeverNull
	 * @return an iterator over all predicates occurring in triples (s,*,o) @NeverNull
	 */
	Iterator<L> getPredicates_SXO(final K s, final M o);

	/**
	 * @return a distinct iterator over all subjects
	 */
	Iterator<K> getSubjects();

	/**
	 * @param p @NeverNull
	 * @param o @NeverNull
	 * @return an iterator over all subjects matching (*,p,o) @NeverNull
	 */
	Iterator<K> getSubjects_XPO(final L p, final M o);

}

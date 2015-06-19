package org.xydra.index;

/**
 * A sink for triples.
 * 
 * RDF nomenclature.
 * 
 * @param <K> s (subject) type
 * @param <L> p (predicate) type
 * @param <M> o (object) type
 */
public interface ITripleSink<K, L, M> {

	/**
	 * Add the given triple to the index
	 * 
	 * @param s
	 * @param p
	 * @param o
	 * 
	 * @return true if something changed, i.e. if triple was not contained
	 *         before
	 */
	boolean index(K s, L p, M o);

}

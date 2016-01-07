package org.xydra.index;

/**
 * A sink for triples (index s-p-o) which additionally supports triple-remove (deIndex s-p-o).
 *
 * RDF nomenclature.
 *
 * @param <K> s (subject) type
 * @param <L> p (predicate) type
 * @param <M> o (object) type
 */
public interface IRemovableTripleSink<K, L, M> extends ITripleSink<K, L, M> {

	/**
	 * Remove the given triple from the index, if it was present
	 *
	 * @param s
	 * @param p
	 * @param o
	 * @return true iff triple was present
	 */
	boolean deIndex(K s, L p, M o);

}

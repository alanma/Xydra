package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.ITriple;

/**
 * {@link Iterable} for {@link ITriple}
 *
 * A simple, read-only stream of triples.
 *
 * @param <K> key type
 * @param <L> key type
 * @param <M> key type
 */
public interface ITripleIterable<K, L, M> {

	/**
	 * @return an {@link Iterator} over all {@link ITriple}
	 */
	Iterator<ITriple<K, L, M>> getTriples();

}

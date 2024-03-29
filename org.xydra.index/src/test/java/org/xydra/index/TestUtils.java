package org.xydra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.index.iterator.Iterators;

/**
 * A helper class to simplify writing tests
 *
 * @author xamde
 */
@RunsInGWT(false)
public class TestUtils {

	/**
	 * @param it
	 * @param expected
	 */
	@SafeVarargs
	public static <E> void assertIteratorContains(final Iterator<E> it, final E... expected) {
		final HashSet<E> exp = new HashSet<E>();
		for (final E e : expected) {
			exp.add(e);
		}
		final HashSet<E> found = new HashSet<E>();
		Iterators.addAll(it, found);

		assertEquals("Should have the same size", exp.size(), found.size());
		for (final E e : exp) {
			assertTrue("Should be returned: " + e + " found only " + found, found.contains(e));
		}
	}

}

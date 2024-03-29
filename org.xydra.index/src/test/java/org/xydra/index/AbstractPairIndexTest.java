package org.xydra.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;

import junit.framework.TestCase;

public abstract class AbstractPairIndexTest extends TestCase {

	protected IPairIndex<Integer, Integer> index;

	@Override
	@Before
	abstract public void setUp();

	@SuppressWarnings("boxing")
	@Test
	public void testEmptyIndex() {

		final Constraint<Integer> e = new EqualsConstraint<Integer>(0);
		final Constraint<Integer> w = new Wildcard<Integer>();

		assertFalse(this.index.contains(w, w));
		assertFalse(this.index.contains(w, e));
		assertFalse(this.index.contains(e, w));
		assertFalse(this.index.contains(e, e));

		assertFalse(this.index.constraintIterator(w, w).hasNext());
		assertFalse(this.index.constraintIterator(w, e).hasNext());
		assertFalse(this.index.constraintIterator(e, w).hasNext());
		assertFalse(this.index.constraintIterator(e, e).hasNext());

		assertTrue(this.index.isEmpty());

	}

	@SuppressWarnings("boxing")
	@Test
	public void testIndex() {

		this.index.index(0, 1);

		final Constraint<Integer> e0 = new EqualsConstraint<Integer>(0);
		final Constraint<Integer> e1 = new EqualsConstraint<Integer>(1);
		final Constraint<Integer> w = new Wildcard<Integer>();

		assertTrue(this.index.contains(w, w));
		assertTrue(this.index.contains(w, e1));
		assertTrue(this.index.contains(e0, w));
		assertTrue(this.index.contains(e0, e1));

		assertFalse(this.index.contains(w, e0));
		assertFalse(this.index.contains(e1, w));
		assertFalse(this.index.contains(e1, e0));

	}

	@SuppressWarnings("boxing")
	@Test
	public void testDeIndex() {

		this.index.index(0, 1);
		this.index.deIndex(0, 1);

		final Constraint<Integer> e0 = new EqualsConstraint<Integer>(0);
		final Constraint<Integer> e1 = new EqualsConstraint<Integer>(1);
		final Constraint<Integer> w = new Wildcard<Integer>();

		assertFalse(this.index.contains(w, w));
		assertFalse(this.index.contains(w, e1));
		assertFalse(this.index.contains(e0, w));
		assertFalse(this.index.contains(e0, e1));

	}

	@Test
	public void testRandomOperations() {

		// number of actors/groups
		final int nKeys = 50;
		// number of operations
		final int nOperations = 150;
		// proportion of operations to be add operations
		final double pAdd = 0.7;
		// check interval
		final int nCheck = 20;

		final long seed = (long) (Math.random() * Long.MAX_VALUE) + System.nanoTime();
		final Random rnd = new Random(seed);
		System.out.println("testRandomOperations(): seed is " + seed);

		// list to store groups for verification
		final List<Pair<Integer, Integer>> pairs = new ArrayList<Pair<Integer, Integer>>();

		// perform some operations
		int chk = nCheck;
		for (int i = 0; i < nOperations; ++i) {

			// add
			if (pairs.isEmpty() || rnd.nextDouble() < pAdd) {

				final Pair<Integer, Integer> pair = makePair(rnd, nKeys);

				// adding a group twice only counts as one
				if (!this.index.contains(new EqualsConstraint<Integer>(pair.getFirst()),
						new EqualsConstraint<Integer>(pair.getSecond()))) {
					pairs.add(pair);
				}

				this.index.index(pair.getFirst(), pair.getSecond());

			}

			// remove
			else {

				final int j = rnd.nextInt(pairs.size());
				final Pair<Integer, Integer> pair = pairs.get(j);

				pairs.remove(j);

				this.index.deIndex(pair.getFirst(), pair.getSecond());

			}

			if (--chk == 0) {
				chk = nCheck;
				checkPairs(nKeys, pairs);
			}
		}

		if (chk != nCheck) {
			checkPairs(nKeys, pairs);
		}

	}

	@SuppressWarnings({ "boxing" })
	protected Pair<Integer, Integer> makePair(final Random rnd, final int nKeys) {
		final int na = rnd.nextInt(nKeys);
		final int nb = rnd.nextInt(nKeys);
		return new Pair<Integer, Integer>(na, nb);
	}

	protected void checkPairs(final int nActors, final List<Pair<Integer, Integer>> pairs) {

		final Set<Pair<Integer, Integer>> t0 = new HashSet<Pair<Integer, Integer>>(pairs);

		// check that all created maps are there
		for (final Pair<Integer, Integer> pair : pairs) {
			assertTrue(this.index.contains(new EqualsConstraint<Integer>(pair.getFirst()),
					new EqualsConstraint<Integer>(pair.getSecond())));
		}

		// check direct group memberships
		final Set<Pair<Integer, Integer>> t1 = new HashSet<Pair<Integer, Integer>>();

		final Iterator<Pair<Integer, Integer>> it = this.index.constraintIterator(
				new Wildcard<Integer>(), new Wildcard<Integer>());
		while (it.hasNext()) {
			t1.add(it.next());
		}

		assertEquals(t0, t1);

	}

	@SuppressWarnings("boxing")
	@Test
	public void testNullEntries() {

		final Constraint<Integer> e = new EqualsConstraint<Integer>(null);
		final Constraint<Integer> w = new Wildcard<Integer>();

		assertTrue(this.index.isEmpty());

		assertFalse(this.index.contains(w, w));
		assertFalse(this.index.contains(w, e));
		assertFalse(this.index.contains(e, w));
		assertFalse(this.index.contains(e, e));

		assertFalse(this.index.constraintIterator(w, w).hasNext());
		assertFalse(this.index.constraintIterator(w, e).hasNext());
		assertFalse(this.index.constraintIterator(e, w).hasNext());
		assertFalse(this.index.constraintIterator(e, e).hasNext());

		this.index.index(null, 1);

		assertFalse(this.index.isEmpty());

		assertTrue(this.index.contains(w, w));
		assertFalse(this.index.contains(w, e));
		assertTrue(this.index.contains(e, w));
		assertFalse(this.index.contains(e, e));

		assertTrue(this.index.constraintIterator(w, w).hasNext());
		assertFalse(this.index.constraintIterator(w, e).hasNext());
		assertTrue(this.index.constraintIterator(e, w).hasNext());

		assertFalse(this.index.constraintIterator(e, e).hasNext());

		this.index.index(2, null);

		assertTrue(this.index.contains(w, w));
		assertTrue(this.index.contains(w, e));
		assertTrue(this.index.contains(e, w));
		assertFalse(this.index.contains(e, e));

		assertTrue(this.index.constraintIterator(w, w).hasNext());
		assertTrue(this.index.constraintIterator(w, e).hasNext());
		assertTrue(this.index.constraintIterator(e, w).hasNext());
		assertFalse(this.index.constraintIterator(e, e).hasNext());

		this.index.deIndex(null, 1);

		assertTrue(this.index.contains(w, w));
		assertTrue(this.index.contains(w, e));
		assertFalse(this.index.contains(e, w));
		assertFalse(this.index.contains(e, e));

		assertTrue(this.index.constraintIterator(w, w).hasNext());
		assertTrue(this.index.constraintIterator(w, e).hasNext());
		assertFalse(this.index.constraintIterator(e, w).hasNext());
		assertFalse(this.index.constraintIterator(e, e).hasNext());

	}

}

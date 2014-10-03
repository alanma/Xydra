package org.xydra.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.xydra.index.ITransitivePairIndex.CycleException;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;

abstract public class AbstractTransitivePairIndexTest extends AbstractPairIndexTest {

	protected ITransitivePairIndex<Integer> idx;

	@SuppressWarnings("boxing")
	@Test
	public void testEmptyIndexImplied() {

		Constraint<Integer> e = new EqualsConstraint<Integer>(0);
		Constraint<Integer> w = new Wildcard<Integer>();

		assertFalse(this.idx.implies(w, w));
		assertFalse(this.idx.implies(w, e));
		assertFalse(this.idx.implies(e, w));
		assertFalse(this.idx.implies(e, e));

		assertFalse(this.idx.transitiveIterator(w, w).hasNext());
		assertFalse(this.idx.transitiveIterator(w, e).hasNext());
		assertFalse(this.idx.transitiveIterator(e, w).hasNext());
		assertFalse(this.idx.transitiveIterator(e, e).hasNext());

		assertTrue(this.idx.isEmpty());

	}

	@SuppressWarnings("boxing")
	@Test
	public void testIndexImplied() {

		this.idx.index(0, 1);
		this.idx.index(1, 2);

		Constraint<Integer> e0 = new EqualsConstraint<Integer>(0);
		Constraint<Integer> e1 = new EqualsConstraint<Integer>(1);
		Constraint<Integer> e2 = new EqualsConstraint<Integer>(2);
		Constraint<Integer> w = new Wildcard<Integer>();

		assertTrue(this.idx.implies(w, w));
		assertTrue(this.idx.implies(w, e1));
		assertTrue(this.idx.implies(w, e2));
		assertTrue(this.idx.implies(e0, w));
		assertTrue(this.idx.implies(e1, w));
		assertTrue(this.idx.implies(e0, e1));
		assertTrue(this.idx.implies(e0, e2));
		assertTrue(this.idx.implies(e1, e2));

		assertFalse(this.idx.implies(w, e0));
		assertFalse(this.idx.implies(e2, w));

	}

	@SuppressWarnings("boxing")
	@Test
	public void testDeIndexImplied() {

		this.idx.index(0, 1);
		this.idx.index(1, 2);
		this.idx.deIndex(1, 2);

		Constraint<Integer> e0 = new EqualsConstraint<Integer>(0);
		Constraint<Integer> e1 = new EqualsConstraint<Integer>(1);
		Constraint<Integer> e2 = new EqualsConstraint<Integer>(2);
		Constraint<Integer> w = new Wildcard<Integer>();

		assertTrue(this.idx.implies(w, w));
		assertTrue(this.idx.implies(w, e1));
		assertFalse(this.idx.implies(w, e2));
		assertTrue(this.idx.implies(e0, w));
		assertFalse(this.idx.implies(e1, w));
		assertTrue(this.idx.implies(e0, e1));
		assertFalse(this.idx.implies(e0, e2));
		assertFalse(this.idx.implies(e1, e2));

		assertFalse(this.idx.implies(w, e0));
		assertFalse(this.idx.implies(e2, w));

	}

	@SuppressWarnings("boxing")
	@Test
	public void testCycles() {

		Constraint<Integer> e0 = new EqualsConstraint<Integer>(0);
		Constraint<Integer> e1 = new EqualsConstraint<Integer>(1);
		Constraint<Integer> e2 = new EqualsConstraint<Integer>(2);

		boolean detected;

		detected = false;
		try {
			this.idx.index(0, 0);
		} catch (CycleException c) {
			detected = true;
		}
		assertTrue(detected);
		assertFalse(this.idx.implies(e0, e0));
		assertFalse(this.idx.contains(e0, e0));

		detected = false;
		try {
			this.idx.index(0, 1);
			this.idx.index(1, 0);
		} catch (CycleException c) {
			detected = true;
		}
		assertTrue(detected);
		assertTrue(this.idx.implies(e0, e1));
		assertTrue(this.idx.contains(e0, e1));
		assertFalse(this.idx.implies(e1, e0));
		assertFalse(this.idx.contains(e1, e0));

		detected = false;
		try {
			this.idx.index(0, 1);
			this.idx.index(1, 2);
			this.idx.index(2, 0);
		} catch (CycleException c) {
			detected = true;
		}
		assertTrue(detected);
		assertTrue(this.idx.implies(e0, e1));
		assertTrue(this.idx.contains(e0, e1));
		assertTrue(this.idx.implies(e1, e2));
		assertTrue(this.idx.contains(e1, e2));
		assertFalse(this.idx.implies(e2, e0));
		assertFalse(this.idx.contains(e2, e0));

	}

	@Override
	@SuppressWarnings("boxing")
	protected Pair<Integer, Integer> makePair(Random rnd, int nKeys) {

		int na = rnd.nextInt(nKeys);
		int nb = rnd.nextInt(nKeys - 1);

		if (nb >= na)
			nb++;

		// no cycles allowed
		if (this.idx.implies(new EqualsConstraint<Integer>(nb), new EqualsConstraint<Integer>(na))) {
			int nc = na;
			na = nb;
			nb = nc;
		}

		return new Pair<Integer, Integer>(na, nb);
	}

	@SuppressWarnings("boxing")
	@Override
	protected void checkPairs(int nActors, List<Pair<Integer, Integer>> grouplist) {

		super.checkPairs(nActors, grouplist);

		Set<Pair<Integer, Integer>> t0 = new HashSet<Pair<Integer, Integer>>();
		// System.out.println("t0 = " + t0);

		for (int i = 0; i < nActors; ++i)
			buildTransitivePairs(i, i, t0);

		// check that all created maps are there
		for (Pair<Integer, Integer> pair : t0)
			assertTrue(this.idx.implies(new EqualsConstraint<Integer>(pair.getFirst()),
					new EqualsConstraint<Integer>(pair.getSecond())));

		// check direct group memberships
		Set<Pair<Integer, Integer>> t1 = new HashSet<Pair<Integer, Integer>>();

		Iterator<Pair<Integer, Integer>> it = this.idx.transitiveIterator(new Wildcard<Integer>(),
				new Wildcard<Integer>());
		while (it.hasNext())
			t1.add(it.next());

		assertEquals(t0, t1);

	}

	private void buildTransitivePairs(Integer start, Integer key, Set<Pair<Integer, Integer>> tz) {

		Iterator<Pair<Integer, Integer>> it = this.idx.constraintIterator(
				new EqualsConstraint<Integer>(key), new Wildcard<Integer>());
		while (it.hasNext()) {

			Integer k = it.next().getSecond();

			Pair<Integer, Integer> pair = new Pair<Integer, Integer>(start, k);

			if (tz.contains(pair))
				continue;

			tz.add(pair);

			buildTransitivePairs(start, k, tz);

		}

	}

}

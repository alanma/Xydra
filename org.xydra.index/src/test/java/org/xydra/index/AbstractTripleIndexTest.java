package org.xydra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import org.xydra.index.ITripleIndex;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

public abstract class AbstractTripleIndexTest<K, L, M> {

	final K s1 = createS("s1");
	final K s2 = createS("s2");
	final L p1 = createP("p1");
	final L p2 = createP("p2");
	final M o1 = createO("o1");
	final M o2 = createO("o2");

	private ITripleIndex<K, L, M> ti;

	public abstract K createS(String label);

	public abstract L createP(String label);

	public abstract M createO(String label);

	public abstract ITripleIndex<K, L, M> create();

	/**
	 * @return true if the index under test contains built-ins and hence returns
	 *         results for *,*,* even on an empty index
	 */
	public boolean containsBuiltIns() {
		return false;
	}

	@Before
	public void setUp() {
		this.ti = create();
	}

	@Test
	public void testIndexAndDeindexWithContainsAndQuery() {
		KeyKeyEntryTuple<K, L, M> s1p1o1 = new KeyKeyEntryTuple<K, L, M>(this.s1, this.p1, this.o1);
		assertFalse(contains(this.s1, this.p1, this.o1));
		assertFalse(Iterators.toList(query(this.s1, this.p1, this.o1)).contains(s1p1o1));
		assertFalse(contains(this.s1, this.p1, null));
		assertFalse(Iterators.toList(query(this.s1, this.p1, null)).contains(s1p1o1));
		assertFalse(contains(this.s1, null, this.o1));
		assertFalse(Iterators.toList(query(this.s1, null, this.o1)).contains(s1p1o1));
		assertFalse(contains(this.s1, null, null));
		assertFalse(Iterators.toList(query(this.s1, null, null)).contains(s1p1o1));
		assertFalse(contains(null, this.p1, this.o1));
		assertFalse(Iterators.toList(query(null, this.p1, this.o1)).contains(s1p1o1));
		assertFalse(contains(null, this.p1, null));
		assertFalse(Iterators.toList(query(null, this.p1, null)).contains(s1p1o1));
		assertFalse(contains(null, null, this.o1));
		assertFalse(Iterators.toList(query(null, null, this.o1)).contains(s1p1o1));

		if (!containsBuiltIns()) {
			assertFalse(contains(null, null, null));
			assertFalse(Iterators.toList(query(null, null, null)).contains(s1p1o1));
		}

		this.ti.index(this.s1, this.p1, this.o1);
		assertTrue(contains(this.s1, this.p1, this.o1));
		assertTrue(Iterators.toList(query(this.s1, this.p1, this.o1)).contains(s1p1o1));
		assertTrue(contains(this.s1, this.p1, null));
		assertTrue(Iterators.toList(query(this.s1, this.p1, null)).contains(s1p1o1));
		assertTrue(contains(this.s1, null, this.o1));
		assertTrue(Iterators.toList(query(this.s1, null, this.o1)).contains(s1p1o1));
		assertTrue(contains(this.s1, null, null));
		assertTrue(Iterators.toList(query(this.s1, null, null)).contains(s1p1o1));
		assertTrue(contains(null, this.p1, this.o1));
		assertTrue(Iterators.toList(query(null, this.p1, this.o1)).contains(s1p1o1));
		assertTrue(contains(null, this.p1, null));
		assertTrue(Iterators.toList(query(null, this.p1, null)).contains(s1p1o1));
		assertTrue(contains(null, null, this.o1));
		assertTrue(Iterators.toList(query(null, null, this.o1)).contains(s1p1o1));
		assertTrue(contains(null, null, null));
		assertTrue(Iterators.toList(query(null, null, null)).contains(s1p1o1));
		assertEquals(1, Iterators.toList(query(this.s1, this.p1, this.o1)).size());

		this.ti.deIndex(this.s1, this.p1, this.o1);
		assertFalse(contains(this.s1, this.p1, this.o1));
		assertFalse(Iterators.toList(query(this.s1, this.p1, this.o1)).contains(s1p1o1));
		assertFalse(contains(this.s1, this.p1, null));
		assertFalse(Iterators.toList(query(this.s1, this.p1, null)).contains(s1p1o1));
		assertFalse(contains(this.s1, null, this.o1));
		assertFalse(Iterators.toList(query(this.s1, null, this.o1)).contains(s1p1o1));
		assertFalse(contains(this.s1, null, null));
		assertFalse(Iterators.toList(query(this.s1, null, null)).contains(s1p1o1));
		assertFalse(contains(null, this.p1, this.o1));
		assertFalse(Iterators.toList(query(null, this.p1, this.o1)).contains(s1p1o1));
		assertFalse(contains(null, this.p1, null));
		assertFalse(Iterators.toList(query(null, this.p1, null)).contains(s1p1o1));
		assertFalse(contains(null, null, this.o1));
		assertFalse(Iterators.toList(query(null, null, this.o1)).contains(s1p1o1));
		if (!containsBuiltIns()) {
			assertFalse(contains(null, null, null));
			assertFalse(Iterators.toList(query(null, null, null)).contains(s1p1o1));
		}
	}

	public boolean contains(K s, L p, M o) {
		Constraint<K> cS = toConstraint(s);
		Constraint<L> cP = toConstraint(p);
		Constraint<M> cO = toConstraint(o);
		boolean b = this.ti.contains(cS, cP, cO);
		assert b == this.ti.getTriples(cS, cP, cO).hasNext();
		return b;
	}

	public Iterator<? extends ITriple<K, L, M>> query(K s, L p, M o) {
		Constraint<K> cS = toConstraint(s);
		Constraint<L> cP = toConstraint(p);
		Constraint<M> cO = toConstraint(o);
		return this.ti.getTriples(cS, cP, cO);
	}

	public static <E> Constraint<E> toConstraint(E expect) {
		if (expect == null)
			return new Wildcard<E>();
		else
			return new EqualsConstraint<E>(expect);
	}

}

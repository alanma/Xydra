package org.xydra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;

public abstract class AbstractMapMapSetIndexTest<K, L, E> {

	K key1k = createKey_K();
	K key2k = createKey_K();
	K key3k = createKey_K();
	L key1l = createKey_L();
	L key2l = createKey_L();
	L key3l = createKey_L();
	E entry1 = createEntry();
	E entry2 = createEntry();
	E entry3 = createEntry();
	E entry4 = createEntry();

	protected IMapMapSetIndex<K, L, E> mapSetIndex;

	public AbstractMapMapSetIndexTest(final IMapMapSetIndex<K, L, E> mapSetIndex) {
		super();
		this.mapSetIndex = mapSetIndex;
	}

	@Before
	public void before() {
		this.mapSetIndex.clear();
	}

	protected abstract E createEntry();

	protected abstract K createKey_K();

	protected abstract L createKey_L();

	@Test
	@Ignore
	public void testComputeDiff() {
		// TODO implement diff test
	}

	@Test
	public void testConstraintIterator() {
		this.mapSetIndex.index(this.key1k, this.key1l, this.entry1);
		this.mapSetIndex.index(this.key2k, this.key2l, this.entry2);
		this.mapSetIndex.index(this.key1k, this.key1l, this.entry3);

		final Iterator<E> it = this.mapSetIndex.constraintIterator(new EqualsConstraint<K>(this.key1k),
				new EqualsConstraint<L>(this.key1l));
		final List<E> list = Iterators.toList(it);
		assertEquals(2, list.size());
		assertTrue(list.contains(this.entry1));
		assertTrue(list.contains(this.entry3));
	}

	/** does not work as it.remove() is not yet supported and maybe never will */
	@Test
	@Ignore
	public void testTupleIterator() {
		this.mapSetIndex.index(this.key1k, this.key1l, this.entry1);
		this.mapSetIndex.index(this.key2k, this.key2l, this.entry2);
		this.mapSetIndex.index(this.key1k, this.key1l, this.entry3);

		List<ITriple<K, L, E>> list;
		list = Iterators
				.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(), new Wildcard<L>(), new Wildcard<E>()));
		assertEquals(3, list.size());

		final Iterator<ITriple<K, L, E>> it = this.mapSetIndex.tupleIterator(new Wildcard<K>(), new Wildcard<L>(),
				new Wildcard<E>());
		while (it.hasNext()) {
			final ITriple<K, L, E> triple = it.next();
			if (triple.p().equals(this.key2l)) {
				it.remove();
			}
		}

		list = Iterators
				.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(), new Wildcard<L>(), new Wildcard<E>()));
		assertEquals(2, list.size());

	}
}

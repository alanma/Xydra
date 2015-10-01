package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapMapMapIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

/**
 * An {@link IMapMapMapIndex} implementation that uses multiple internal indices to allow fast queries on all keys.
 *
 * The remove() method of iterators always works for all constraint combinations.
 *
 * @author dscharrer
 * @param <K> key1
 * @param <L> key2
 * @param <M> key3
 * @param <E> entry
 */
public abstract class AbstractFastTripleMap<K,L,M,E>
implements IMapMapMapIndex<K, L, M, E> {

	final protected IMapMapMapIndex<K, L, M, E> idx_k1_k2_k3;
	protected transient IMapMapMapIndex<L, M, K, E> idx_k2_k3_k1;
	protected transient IMapMapMapIndex<M, K, L, E> idx_k3_k1_k2;

	protected abstract IMapMapMapIndex<K,L,M,E> createMapMapMapIndex_KLME();
	protected abstract IMapMapMapIndex<L,M,K,E> createMapMapMapIndex_LMKE();
	protected abstract IMapMapMapIndex<M,K,L,E> createMapMapMapIndex_MKLE();

	public AbstractFastTripleMap() {
		this.idx_k1_k2_k3 = createMapMapMapIndex_KLME();
		this.idx_k2_k3_k1 = createMapMapMapIndex_LMKE();
		this.idx_k3_k1_k2 = createMapMapMapIndex_MKLE();
	}

	@Override
	public boolean containsKey(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		if (c2.isStar() ? c3.isStar() : !c1.isStar()) {
			return this.idx_k1_k2_k3.containsKey(c1, c2, c3);
		}
		if (!c2.isStar()) {
			return this.idx_k2_k3_k1.containsKey(c2, c3, c1);
		}
		return this.idx_k3_k1_k2.containsKey(c3, c1, c2);
	}

	public boolean containsKey(final K c1, final L c2, final M c3) {
		return containsKey(toConstraint_K(c1), toConstraint_L(c2), toConstraint_M(c3));
	}


	@Override
	public void deIndex(final K key1, final L key2, final M key3) {
		this.idx_k1_k2_k3.deIndex(key1, key2, key3);
		this.idx_k2_k3_k1.deIndex(key2, key3, key1);
		this.idx_k3_k1_k2.deIndex(key3, key1, key2);
	}

	@Override
	public void index(final K key1, final L key2, final M key3, final E entry) {
		this.idx_k1_k2_k3.index(key1, key2, key3, entry);
		this.idx_k2_k3_k1.index(key2, key3, key1, entry);
		this.idx_k3_k1_k2.index(key3, key1, key2, entry);
	}

	@Override
	public E lookup(final K key1, final L key2, final M key3) {
		return this.idx_k1_k2_k3.lookup(key1, key2, key3);
	}

	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(final K c1, final L c2, final M c3) {
		return tupleIterator(toConstraint_K(c1), toConstraint_L(c2), toConstraint_M(c3));
	}


	private Constraint<K> toConstraint_K(final K c1) {
		return c1 == null ? new Wildcard<K>() : new EqualsConstraint<K>(c1);
	}

	private Constraint<L> toConstraint_L(final L c2) {
		return c2 == null ? new Wildcard<L>() : new EqualsConstraint<L>(c2);
	}

	private Constraint<M> toConstraint_M(final M c3) {
		return c3 == null ? new Wildcard<M>() : new EqualsConstraint<M>(c3);
	}

	@Override
	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {

		// Permute the constraints to that all EqualsConstraint come before the
		// Wildcard constraints. This 1) provides optimal performance as there
		// is no brute force searching for over a whole map and 2) allows the
		// remove() method to work in all cases.

		if (c2.isStar() ? c3.isStar() : !c1.isStar()) {
			final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> it = this.idx_k1_k2_k3.tupleIterator(c1, c2, c3);
			return new Iterator<KeyKeyKeyEntryTuple<K, L, M, E>>() {

				private KeyKeyKeyEntryTuple<K, L, M, E> last;

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public KeyKeyKeyEntryTuple<K, L, M, E> next() {
					return this.last = it.next();
				}

				@Override
				public void remove() {
					it.remove();
					if (this.last != null) {
						AbstractFastTripleMap.this.idx_k2_k3_k1.deIndex(this.last.getKey2(), this.last.getKey3(),
								this.last.getKey1());
						AbstractFastTripleMap.this.idx_k3_k1_k2.deIndex(this.last.getKey3(), this.last.getKey1(),
								this.last.getKey2());
					}
				}

			};
		}
		if (!c2.isStar()) {
			final Iterator<KeyKeyKeyEntryTuple<L, M, K, E>> it = this.idx_k2_k3_k1.tupleIterator(c2, c3, c1);
			return new Iterator<KeyKeyKeyEntryTuple<K, L, M, E>>() {

				private KeyKeyKeyEntryTuple<L, M, K, E> last;

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public KeyKeyKeyEntryTuple<K, L, M, E> next() {
					this.last = it.next();
					return new KeyKeyKeyEntryTuple<K, L, M, E>(this.last.getKey3(), this.last.getKey1(),
							this.last.getKey2(), this.last.getEntry());
				}

				@Override
				public void remove() {
					it.remove();
					if (this.last != null) {
						AbstractFastTripleMap.this.idx_k1_k2_k3.deIndex(this.last.getKey3(), this.last.getKey1(),
								this.last.getKey2());
						AbstractFastTripleMap.this.idx_k3_k1_k2.deIndex(this.last.getKey2(), this.last.getKey3(),
								this.last.getKey1());
					}
				}

			};
		}

		final Iterator<KeyKeyKeyEntryTuple<M, K, L, E>> it = this.idx_k3_k1_k2.tupleIterator(c3, c1, c2);
		return new Iterator<KeyKeyKeyEntryTuple<K, L, M, E>>() {

			private KeyKeyKeyEntryTuple<M, K, L, E> last;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public KeyKeyKeyEntryTuple<K, L, M, E> next() {
				this.last = it.next();
				return new KeyKeyKeyEntryTuple<K, L, M, E>(this.last.getKey2(), this.last.getKey3(),
						this.last.getKey1(), this.last.getEntry());
			}

			@Override
			public void remove() {
				it.remove();
				if (this.last != null) {
					AbstractFastTripleMap.this.idx_k1_k2_k3.deIndex(this.last.getKey2(), this.last.getKey3(),
							this.last.getKey1());
					AbstractFastTripleMap.this.idx_k2_k3_k1.deIndex(this.last.getKey3(), this.last.getKey1(),
							this.last.getKey2());
				}
			}

		};
	}

	@Override
	public void clear() {
		this.idx_k1_k2_k3.clear();
		this.idx_k2_k3_k1.clear();
		this.idx_k3_k1_k2.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.idx_k1_k2_k3.isEmpty();
	}

	/**
	 * Called on deserialization, needs to restore transient members.
	 */
	private Object readResolve() {
		if (this.idx_k2_k3_k1 == null) {
			this.idx_k2_k3_k1 = createMapMapMapIndex_LMKE();
			final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> it = this.idx_k1_k2_k3.tupleIterator(new Wildcard<K>(),
					new Wildcard<L>(), new Wildcard<M>());
			while (it.hasNext()) {
				final KeyKeyKeyEntryTuple<K, L, M, E> tuple = it.next();
				this.idx_k2_k3_k1.index(tuple.getKey2(), tuple.getKey3(), tuple.getKey1(), tuple.getEntry());
			}
		}
		if (this.idx_k3_k1_k2 == null) {
			this.idx_k3_k1_k2 = createMapMapMapIndex_MKLE();
			final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> it = this.idx_k1_k2_k3.tupleIterator(new Wildcard<K>(),
					new Wildcard<L>(), new Wildcard<M>());
			while (it.hasNext()) {
				final KeyKeyKeyEntryTuple<K, L, M, E> tuple = it.next();
				this.idx_k3_k1_k2.index(tuple.getKey3(), tuple.getKey1(), tuple.getKey2(), tuple.getEntry());
			}
		}
		return this;
	}

	@Override
	public String toString() {
		return this.idx_k1_k2_k3.toString();
	}

	@Override
	public Iterator<K> key1Iterator() {
		return this.idx_k1_k2_k3.key1Iterator();
	}

	@Override
	public Iterator<L> key2Iterator() {
		return this.idx_k2_k3_k1.key1Iterator();
	}

	@Override
	public Iterator<M> key3Iterator() {
		return this.idx_k3_k1_k2.key1Iterator();
	}

	@Override
	public Iterator<KeyKeyEntryTuple<K, L, M>> keyKeyKeyIterator(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		// IMPROVE optimize by using different indexes?
		return this.idx_k1_k2_k3.keyKeyKeyIterator(c1, c2, c3);
	}

	public Iterator<KeyKeyEntryTuple<K, L, M>> keyKeyKeyIterator(final K c1, final L c2, final M c3) {
		return keyKeyKeyIterator(toConstraint_K(c1), toConstraint_L(c2), toConstraint_M(c3));
	}

	@Override
	public void dump() {
		this.idx_k1_k2_k3.dump();
	}


}
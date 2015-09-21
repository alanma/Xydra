package org.xydra.index.impl.trie;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NotThreadSafe;
import org.xydra.annotations.ReadOperation;
import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.impl.DebugReentrantReadWriteLock;
import org.xydra.index.iterator.ClosableIterator;
import org.xydra.index.iterator.ClosableIteratorAdapter;
import org.xydra.index.iterator.IFilter;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.common.base.Function;

/**
 * Current impl: this class -> {@link SortedStringMapIndex} -> {@link SortedArrayMap}
 *
 * IMPROVE rewrite algorithms into non-recursive form
 *
 * IMPROVE work on byte arrays in utf8 encoding?
 *
 * @author xamde
 *
 *         This classes uses {@link ClosableIterator} and internal read/write locks for thread management. Always close
 *         your iterators or we will starve.
 *
 * @param <E>
 */
@NotThreadSafe
public class SmallTrieStringMapSetIndex<E> implements IMapSetIndex<String, E>, Serializable {

	/**
	 * Implementation note:
	 *
	 * Call {@link #readOperationEnd()} to close the read lock.
	 */

	private static class ConstraintKeyPrefix implements Constraint<String>, Serializable {

		private String keyPrefix;

		public ConstraintKeyPrefix(final String keyPrefix) {
			setKeyPrefix(keyPrefix);
		}

		@Override
		public String getExpected() {
			return this.keyPrefix;
		}

		@Override
		public boolean isExact() {
			return false;
		}

		@Override
		public boolean isStar() {
			return false;
		}

		@Override
		public boolean matches(final String element) {
			return element.startsWith(this.keyPrefix);
		}

		public void setKeyPrefix(final String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}

	public static class KeyFragments implements Serializable {

		public List<String> matched;

		/* the empty string, if nothing remains */
		public String remainder;

		public KeyFragments(final List<String> matched, final String remainder) {
			this.matched = matched;
			this.remainder = remainder;
		}
	}

	/**
	 * public only for debug; non-static to avoid generics-overkill and have access to entrySetFactory
	 *
	 *
	 */
	public class Node implements Serializable {

		private static final long serialVersionUID = -125040148037301604L;

		/**
		 * A map from string (local part of key) to children (other nodes).
		 *
		 * Invariant: Strings are prefix-free, i.e. no two strings in the map start with the same prefix, not even a
		 * single character. Different from normal tries, strings might be longer than 1 character
		 */
		private SortedStringMapIndex<Node> children;

		/**
		 * @NeverNull
		 */
		private IEntrySet<E> entrySet;

		public Node() {
			assert SmallTrieStringMapSetIndex.this.entrySetFactory != null;
			this.entrySet = SmallTrieStringMapSetIndex.this.entrySetFactory.createInstance();
			this.children = new SortedStringMapIndex<Node>();
		}

		public Node(final E value) {
			this();
			assert value != null;
			this.entrySet.index(value);
		}

		public Iterator<E> constraintIterator(final Constraint<String> c1) {
			if (c1.isStar()) {
				return entriesIterator();
			} else {
				// we need to filter
				if (c1.isExact()) {
					final IEntrySet<E> node = lookup(c1.getExpected());
					if (node == null) {
						return NoneIterator.create();
					} else {
						return node.iterator();
					}
				} else {
					// search...
					final Iterator<KeyEntryTuple<String, Node>> it = this.children.tupleIterator(c1);
					return Iterators.cascade(it, SmallTrieStringMapSetIndex.this.TRANSFORMER_KET2EntrySet);
				}
			}
		}

		public boolean deIndex(final Node parent, final String parentKey, final String removeKey) {
			assert removeKey != null;
			return deIndex(parent, parentKey, removeKey, SmallTrieStringMapSetIndex.this.FUNCTION_clear);
		}

		public boolean deIndex(final Node parent, final String parentKey, final String removeKey, final E removeEntry) {
			assert removeKey != null;
			assert removeEntry != null;
			return deIndex(parent, parentKey, removeKey, new Function<Node, Boolean>() {

				@Override
				public Boolean apply(final Node node) {
					return node.entrySet.deIndex(removeEntry);
				}
			});
		}

		private boolean deIndex(final Node parent, final String parentKey, final String removeKey,
				final Function<Node, Boolean> action) {
			assert removeKey != null;
			assert action != null;

			if (removeKey.length() == 0) {
				// base case, perfect match, remove here
				final boolean result = action.apply(this);

				if (parent != null && this.entrySet.isEmpty()) {
					if (this.children.isEmpty()) {
						// kill this node
						parent.children.deIndex(parentKey);
					} else if (this.children.size() == 1) {
						// move this node,
						// re-balance tree
						final Node singleChild = this.children.iterator().next();
						parent.children.deIndex(parentKey);

						if (singleChild.isEmpty()) {
							// boof, gone
						} else {
							// add at new key
							parent.children.index(parentKey + removeKey, singleChild);
						}
					}
					// else leave all untouched
				}
				return result;
			}
			assert removeKey.length() > 0;

			final String matchingKey = this.children.lookupFirstPrefix(removeKey.substring(0, 1));
			if (matchingKey == null) {
				log.trace("removeKey not found");
				return false;
			}

			if (matchingKey.length() <= removeKey.length()) {
				final int commonPrefixLen = getSharedPrefixLength(removeKey, matchingKey);
				assert commonPrefixLen > 0 : "at least 1 char in common: '" + removeKey.substring(0, 1) + "'";
				if (commonPrefixLen == matchingKey.length()) {
					// recurse
					final Node matchingNode = this.children.lookup(matchingKey);
					matchingNode.deIndex(this, removeKey.substring(0, commonPrefixLen),
							removeKey.substring(commonPrefixLen), action);
				}
			}

			log.trace("no match possible");
			return false;
		}

		public void dump(final String indent, final String combinedKey) {
			System.out.println();
		}

		/**
		 * @return recursively all entries
		 */
		public Iterator<E> entriesIterator() {
			return Iterators.cascade(nodeIterator(), SmallTrieStringMapSetIndex.this.TRANSFORMER_NODE2ENTRIES);
		}

		/**
		 * @param searchKey
		 * @return the {@link KeyFragments} for searchKey
		 */
		public KeyFragments getKeyFragmentsFor(final String searchKey) {
			assert searchKey != null;
			// recursion base case
			if (searchKey.length() == 0) {
				return new KeyFragments(new ArrayList<String>(), "");
			}

			assert searchKey.length() > 0;
			final String conflictKey = this.children.lookupFirstPrefix(searchKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				return new KeyFragments(new ArrayList<String>(), searchKey);
			}

			final Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			final int commonPrefixLen = getSharedPrefixLength(searchKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + searchKey + " conflictKey=" + conflictKey;
			if (conflictKey.equals(searchKey)) {
				// the conflicting node is our node
				final List<String> matched = new ArrayList<String>();
				matched.add(conflictKey);
				return new KeyFragments(matched, "");
			} else if (commonPrefixLen >= conflictKey.length()) {
				/* the conflicting key ('ba') is part of the search key ('baz') */
				// RECURSION: just index ('z') in subnode
				final String searchKeyPostfix = searchKey.substring(conflictKey.length());
				final KeyFragments subFragments = conflictingNode.getKeyFragmentsFor(searchKeyPostfix);
				subFragments.matched.add(0, conflictKey);
				return subFragments;
			} else {
				/* the search key ('foo') or ('foobaz') is part of the conflicting key ('foo' in 'foobar') or they share
				 * a common prefix ('foo' from 'foobaz'+'foobar'): insert a new node ('foo') & update children */
				final String commonPrefix = searchKey.substring(0, commonPrefixLen);
				final String insertKeyPostfix = searchKey.substring(commonPrefixLen);
				final List<String> matched = new ArrayList<String>();
				matched.add(commonPrefix);
				final KeyFragments fragments = new KeyFragments(matched, insertKeyPostfix);
				return fragments;
			}
		}

		/**
		 * IMPROVE more recursive version of this algorithm
		 *
		 * @param s
		 * @param start
		 * @return @CanBeNull or a pair (match position, entry set of matching values). If not null, the integer is > 0.
		 */
		public Pair<Integer, Set<E>> getLongestMatch(final String s, final int start) {

			int len = 1;

			while (start + len < s.length()) {
				final String key = s.substring(start, start + len);
				final boolean b = this.containsEntriesWithPrefix(key);
				if (b) {
					// great, maybe even with a longer key
					len++;
				} else {
					// we went one len too far, get last key
					return toLongestMatchResult(s, start, len - 1);
				}
			}
			// we match the whole s-remainder
			return toLongestMatchResult(s, start, len);
		}

		private Pair<Integer, Set<E>> toLongestMatchResult(final String s, final int start, final int len) {
			if (len == 0) {
				return null;
			}

			final String key = s.substring(start, start + len);

			final IEntrySet<E> es = SmallTrieStringMapSetIndex.this.lookup(key);
			if (es != null && !es.isEmpty()) {
				// we have a real match
				return new Pair<Integer, Set<E>>(len, es.toSet());
			} else {
				// no real match
				return null;
			}
		}

		/**
		 * @param insertKey
		 * @param value
		 * @return true if entry was not yet in this node
		 */
		public boolean index(final String insertKey, final E value) {
			assert insertKey != null;
			assert value != null;
			// recursion base case
			if (insertKey.length() == 0) {
				return this.entrySet.index(value);
			}

			assert insertKey.length() > 0;
			final String conflictKey = this.children.lookupFirstPrefix(insertKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				this.children.index(insertKey, new Node(value));
				return true;
			}

			final Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			final int commonPrefixLen = getSharedPrefixLength(insertKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + insertKey + " conflictKey=" + conflictKey;
			if (conflictKey.equals(insertKey)) {
				// the conflicting node is our node
				return conflictingNode.entrySet.index(value);
			} else if (commonPrefixLen >= conflictKey.length()) {
				/* the conflicting key ('ba') is part of the insertion key ('baz') */
				// RECURSION: just index ('z') in subnode
				final String insertKeyPostfix = insertKey.substring(conflictKey.length());
				return conflictingNode.index(insertKeyPostfix, value);
			} else {
				/* the insertion key ('foo') or ('foobaz') is part of the conflicting key ('foo' in 'foobar') or they
				 * share a common prefix ('foo' from 'foobaz'+'foobar'): insert a new node ('foo') & update children */
				this.children.deIndex(conflictKey);
				final Node commonNode = new Node();
				final String commonPrefix = insertKey.substring(0, commonPrefixLen);
				this.children.index(commonPrefix, commonNode);
				final String conflictKeyPostfix = conflictKey.substring(commonPrefixLen);
				commonNode.children.index(conflictKeyPostfix, conflictingNode);
				final String insertKeyPostfix = insertKey.substring(commonPrefixLen);
				commonNode.index(insertKeyPostfix, value);
				return true;
			}
		}

		/**
		 * @param key
		 * @return true iff set K did not contain key yet
		 */
		public boolean indexKey(final String insertKey) {
			assert insertKey != null;
			// recursion base case
			if (insertKey.length() == 0) {
				// the trie always contains the empty key?
				return false;
			}

			assert insertKey.length() > 0;
			final String conflictKey = this.children.lookupFirstPrefix(insertKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				this.children.index(insertKey, new Node());
				return true;
			}

			final Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			final int commonPrefixLen = getSharedPrefixLength(insertKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + insertKey + " conflictKey=" + conflictKey;
			if (conflictKey.equals(insertKey)) {
				// the conflicting node is our node
				return false;
			} else if (commonPrefixLen >= conflictKey.length()) {
				/* the conflicting key ('ba') is part of the insertion key ('baz') */
				// RECURSION: just index ('z') in subnode
				final String insertKeyPostfix = insertKey.substring(conflictKey.length());
				return conflictingNode.indexKey(insertKeyPostfix);
			} else {
				/* the insertion key ('foo') or ('foobaz') is part of the conflicting key ('foo' in 'foobar') or they
				 * share a common prefix ('foo' from 'foobaz'+'foobar'): insert a new node ('foo') & update children */
				this.children.deIndex(conflictKey);
				final Node commonNode = new Node();
				final String commonPrefix = insertKey.substring(0, commonPrefixLen);
				this.children.index(commonPrefix, commonNode);
				final String conflictKeyPostfix = conflictKey.substring(commonPrefixLen);
				commonNode.children.index(conflictKeyPostfix, conflictingNode);
				final String insertKeyPostfix = insertKey.substring(commonPrefixLen);
				commonNode.indexKey(insertKeyPostfix);
				return true;
			}
		}

		public boolean isEmpty() {
			if (!this.entrySet.isEmpty()) {
				return false;
			}

			if (!this.children.isEmpty()) {
				return false;
			}

			return true;
		}

		/**
		 * @param combinedKey
		 * @param keyConstraint
		 * @NeverNull
		 * @return an iterator over all nodes with entries; breadth-first
		 */
		public Iterator<KeyEntryTuple<String, Node>> keyAndNodeIterator(final String combinedKey,
				final Constraint<String> keyConstraint) {

			Iterator<KeyEntryTuple<String, Node>> thisNodeIt = null;
			if (keyConstraint.matches(combinedKey)) {
				thisNodeIt = Iterators.forOne(new KeyEntryTuple<String, Node>(combinedKey, this));
				if (keyConstraint.isExact()) {
					// recursion base case: I am *the* searched node
					return thisNodeIt;
				}
			}

			// else we need to (also) traverse children
			final Iterator<KeyEntryTuple<String, Node>> fromChildrenIt = Iterators.cascade(

					this.children.tupleIterator(ANY_STRING),

					new ITransformer<KeyEntryTuple<String, Node>, Iterator<KeyEntryTuple<String, Node>>>() {

						@Override
						public Iterator<KeyEntryTuple<String, Node>> transform(final KeyEntryTuple<String, Node> tuple) {
							/* does it make sense to recurse here? */
							final String currentKey = combinedKey + tuple.getKey();
							if (canMatch(currentKey, keyConstraint)) {
								// IMPROVE avoid recursion here
								// recursion (!)
								return tuple.getEntry().keyAndNodeIterator(currentKey, keyConstraint);
							} else {
								return NoneIterator.create();
							}
						}

					});

			if (keyConstraint.isStar()) {
				assert thisNodeIt != null;
				// include this node and children
				return Iterators.concat(thisNodeIt, fromChildrenIt);
			} else if (keyConstraint.isExact()) {
				// look only in children
				return fromChildrenIt;
			} else {
				if (keyConstraint.matches(combinedKey)) {
					// include this
					return Iterators.concat(thisNodeIt, fromChildrenIt);
				} else {
					// children only
					return fromChildrenIt;
				}
			}
		}

		/**
		 * @param combinedKey
		 * @return an iterator over all keys for which at least one entry has been indexed
		 */
		public Iterator<String> keyIterator(final String combinedKey) {
			final Iterator<String> fromChildren = Iterators.cascade(

					this.children.tupleIterator(new Wildcard<String>()),

					new ITransformer<KeyEntryTuple<String, Node>, Iterator<String>>() {

						@Override
						public Iterator<String> transform(final KeyEntryTuple<String, Node> in) {
							return in.getSecond().keyIterator(combinedKey + in.getFirst());
						}

					});

			final boolean includeThisNode = !this.entrySet.isEmpty();

			if (this.children.isEmpty()) {
				// nothing else we can do
				assert this.entrySet != null;

				if (includeThisNode) {
					return Iterators.forOne(combinedKey);
				} else {
					return NoneIterator.create();
				}
			} else {
				if (includeThisNode) {
					return Iterators.concat(Iterators.forOne(combinedKey), fromChildren);
				} else {
					return fromChildren;
				}
			}
		}

		public IEntrySet<E> lookup(final String key) {
			assert key != null;
			// recursion base case
			if (key.length() == 0) {
				return this.entrySet;
			}
			if (this.children.isEmpty()) {
				return null;
			}
			/* Search character by character */
			final String prefixCharacter = key.substring(0, 1);
			final String matched = this.children.lookupFirstPrefix(prefixCharacter);
			if (matched == null) {
				return null;
			}
			final int commonPrefixLength = getSharedPrefixLength(key, matched);
			final Node next = this.children.lookup(key.substring(0, commonPrefixLength));
			if (next == null) {
				return null;
			}
			assert next != null : "matched = " + matched;
			// recursion
			return next.lookup(key.substring(commonPrefixLength));
		}

		/**
		 * @return an iterator over all nodes with entries; breadth-first
		 */
		public Iterator<Node> nodeIterator() {
			final Iterator<Node> fromChildren = Iterators.cascade(this.children.iterator(),
					new ITransformer<Node, Iterator<Node>>() {

				@Override
				public Iterator<Node> transform(final Node node) {
					return node.nodeIterator();
				}
			});

			if (this.entrySet == null || this.entrySet.isEmpty()) {
				return fromChildren;
			} else {
				return Iterators.concat(Iterators.forOne(this), fromChildren);
			}
		}

		/**
		 * @param combinedKey
		 * @param keyConstraint
		 * @NeverNull
		 * @return an iterator over all nodes with entries; breadth-first
		 */
		public Iterator<Node> nodeIterator(final String combinedKey, final Constraint<String> keyConstraint) {

			Iterator<Node> thisNodeIt = null;
			if (keyConstraint.matches(combinedKey)) {
				thisNodeIt = Iterators.forOne(this);
				if (keyConstraint.isExact()) {
					// recursion base case: I am *the* searched node
					return thisNodeIt;
				}
			}

			// else we need to (also) traverse children
			final Iterator<Node> fromChildrenIt = Iterators.cascade(

					this.children.tupleIterator(ANY_STRING),

					new ITransformer<KeyEntryTuple<String, Node>, Iterator<Node>>() {

						@Override
						public Iterator<Node> transform(final KeyEntryTuple<String, Node> tuple) {
							/* does it make sense to recurse here? */
							final String currentKey = combinedKey + tuple.getKey();
							if (canMatch(currentKey, keyConstraint)) {
								// IMPROVE avoid recursion here
								// recursion (!)
								return tuple.getEntry().nodeIterator(currentKey, keyConstraint);
							} else {
								return NoneIterator.create();
							}
						}

					});

			if (keyConstraint.isStar()) {
				assert thisNodeIt != null;
				// include this node and children
				return Iterators.concat(thisNodeIt, fromChildrenIt);
			} else if (keyConstraint.isExact()) {
				// look only in children
				return fromChildrenIt;
			} else {
				if (keyConstraint.matches(combinedKey)) {
					// include this
					return Iterators.concat(thisNodeIt, fromChildrenIt);
				} else {
					// children only
					return fromChildrenIt;
				}
			}
		}

		public boolean containsEntries() {
			if (!this.entrySet.isEmpty()) {
				return true;
			}

			for (final SmallTrieStringMapSetIndex<E>.Node childNode : this.children.values()) {
				if (childNode.containsEntries()) {
					return true;
				}
			}

			return false;
		}

		/**
		 * @param searchKeyPrefix
		 * @return true if at least one entry is indexed at a key which starts with or is equal to keyPrefix
		 */
		public boolean containsEntriesWithPrefix(final String searchKeyPrefix) {
			assert searchKeyPrefix != null;
			// recursion base case
			if (searchKeyPrefix.length() == 0) {
				return containsEntries();
			}

			assert searchKeyPrefix.length() > 0;
			final String indexedKey = this.children.lookupFirstPrefix(searchKeyPrefix.substring(0, 1));
			if (indexedKey == null) {
				// there is none
				return false;
			}

			// find common prefix to extract
			final int commonPrefixLen = getSharedPrefixLength(searchKeyPrefix, indexedKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + searchKeyPrefix + " conflictKey="
			+ indexedKey;
			if (indexedKey.equals(searchKeyPrefix)) {
				// the conflicting node is our node
				final Node indexedNode = this.children.lookup(indexedKey);
				return indexedNode.containsEntries();
			} else if (commonPrefixLen <= searchKeyPrefix.length()) {
				// continue searching there
				final String searchKeyPrefixRemainder = searchKeyPrefix.substring(commonPrefixLen);
				final Node indexedNode = this.children.lookup(indexedKey);
				return indexedNode.containsEntriesWithPrefix(searchKeyPrefixRemainder);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return toString("  ", "").toString();
		}

		private StringBuilder toString(final String indent, final String combinedKey) {
			final StringBuilder b = new StringBuilder();
			b.append(indent + "Node id " + hashCode() + " representing " + "'" + combinedKey + "'\n");
			b.append(indent + "Value = '" + this.entrySet + "'\n");

			final Iterator<String> it = this.children.keyIterator();
			while (it.hasNext()) {
				final String key = it.next();
				b.append(indent + "* Key = '" + key + "'\n");
				b.append(this.children.lookup(key).toString(indent + "  ", combinedKey + key));
			}
			return b;
		}

		/**
		 * @param combinedKey
		 * @param keyConstraint
		 * @param entryConstraint
		 * @return all matching keys X their matching entries
		 */
		public Iterator<KeyEntryTuple<String, E>> tupleIterator(final String combinedKey,
				final Constraint<String> keyConstraint, final Constraint<E> entryConstraint) {

			/* get all c1-matching (combinedKey,Node) pairs, recursion happens only here */
			final Iterator<KeyEntryTuple<String, Node>> keyNodeIt = keyAndNodeIterator(combinedKey, keyConstraint);

			final Iterator<KeyEntryTuple<String, E>> keyEntriesIt = Iterators.cascade(keyNodeIt,
					new ITransformer<KeyEntryTuple<String, Node>, Iterator<KeyEntryTuple<String, E>>>() {

				@Override
				public Iterator<KeyEntryTuple<String, E>> transform(
						final KeyEntryTuple<String, Node> ketCombinedKeyNode) {
					/* for each matching (combinedKey-Node)-pair: look in the node and take all
					 * entryConstraint-matching entries and ... */
					final Iterator<E> entryIt = ketCombinedKeyNode.getEntry().entrySet.iterator();

					Iterator<E> filteredEntryIt;
					if (entryConstraint.isStar()) {
						filteredEntryIt = entryIt;
					} else {
						// apply constraint
						filteredEntryIt = Iterators.filter(entryIt, entryConstraint);
					}
					/* ... wrap them back into the final KeyEntry tuples */
					return Iterators.transform(filteredEntryIt,
							new ITransformer<E, KeyEntryTuple<String, E>>() {

						@Override
						public KeyEntryTuple<String, E> transform(final E entry) {
							assert entry != null;

							return new KeyEntryTuple<String, E>(ketCombinedKeyNode.getKey(), entry);
						}
					});
				}
			});

			return keyEntriesIt;
		}

		public Iterator<IEntrySet<E>> valueAsEntrySetIterator(final String combinedKey,
				final Constraint<String> keyConstraint) {
			/* get all c1-matching (combinedKey,Node) pairs, recursion happens only here */
			final Iterator<Node> keyNodeIt = nodeIterator(combinedKey, keyConstraint);

			final Iterator<IEntrySet<E>> entrySetIt = Iterators.transform(keyNodeIt,
					new ITransformer<Node, IEntrySet<E>>() {

				@Override
				public IEntrySet<E> transform(final Node ketCombinedKeyNode) {
					/* for each matching (combinedKey-Node)-pair: look in the node and take all
					 * entryConstraint-matching entries and ... */
					final IEntrySet<E> set = ketCombinedKeyNode.entrySet;
					return set;
				}
			});

			final Iterator<IEntrySet<E>> nonEmptyEntrySetIt = Iterators.filter(entrySetIt,
					SmallTrieStringMapSetIndex.this.FILTER_NON_EMPTY_ENTRYSET);

			return nonEmptyEntrySetIt;
		}

		/**
		 * @param combinedKey
		 * @param keyConstraint
		 * @param optionalEntryFilter
		 * @CanBeNull
		 * @return all matching keys X their matching entries
		 */
		public Iterator<E> valueIterator(final String combinedKey, final Constraint<String> keyConstraint,
				final IFilter<E> optionalEntryFilter) {

			/* get all c1-matching (combinedKey,Node) pairs, recursion happens only here */
			final Iterator<Node> keyNodeIt = nodeIterator(combinedKey, keyConstraint);

			final Iterator<E> keyEntriesIt = Iterators.cascade(keyNodeIt, new ITransformer<Node, Iterator<E>>() {

				@Override
				public Iterator<E> transform(final Node ketCombinedKeyNode) {
					/* for each matching (combinedKey-Node)-pair: look in the node and take all entryConstraint-matching
					 * entries and ... */
					final Iterator<E> entryIt = ketCombinedKeyNode.entrySet.iterator();

					Iterator<E> filteredEntryIt;
					if (optionalEntryFilter == null) {
						return entryIt;
					} else {
						// apply constraint
						filteredEntryIt = Iterators.filter(entryIt, optionalEntryFilter);
						return filteredEntryIt;
					}
				}
			});

			return keyEntriesIt;
		}

	}

	private static final Constraint<String> ANY_STRING = new Wildcard<String>();

	private static final Logger log = LoggerFactory.getLogger(SmallTrieStringMapSetIndex.class);

	/**
	 * @param combinedKey
	 * @param c1 expected constraint
	 * @return true if further descending a prefix tree with current key 'combinedKey' has a chance of statisfying the
	 *         constraint
	 */
	private static boolean canMatch(final String combinedKey, final Constraint<String> c1) {

		if (c1.isStar()) {
			return true;
		}

		if (c1.isExact()) {
			// optimised analysis possible

			/* if we are already to deep in the tree, we won't find a match */
			if (combinedKey.length() > c1.getExpected().length()) {
				return false;
			}

			/* the current combinedKey needs to be a prefix of what we are looking for */
			if (!c1.getExpected().startsWith(combinedKey)) {
				return false;
			}
		}
		// who knows. sure it can match. maybe.
		return true;
	}

	/**
	 * @param master
	 * @param copy
	 * @return the number of shared characters from master in copy
	 */
	public static int getSharedPrefixLength(final String master, final String copy) {
		assert master != null;
		assert copy != null;
		int i = 0;
		while (i < Math.min(master.length(), copy.length())) {
			final int m = master.codePointAt(i);
			final int c = copy.codePointAt(i);
			if (m == c) {
				i += Character.charCount(m);
			} else {
				break;
			}
		}
		return i;
	}

	private final Factory<IEntrySet<E>> entrySetFactory;

	private transient IFilter<IEntrySet<E>> FILTER_NON_EMPTY_ENTRYSET;

	/* this class works around the fact that Java generics + Serialization + anonymous inner class does not work */
	private static class FILTER_NON_EMPTY_ENTRYSET<E> implements IFilter<IEntrySet<E>> {
		@Override
		public boolean matches(final IEntrySet<E> entry) {
			return !entry.isEmpty();
		}
	}

	private transient Function<Node, Boolean> FUNCTION_clear;

	/* this class works around the fact that Java generics + Serialization + anonymous inner class does not work */
	private static class FUNCTION_clear<E> implements Function<SmallTrieStringMapSetIndex<E>.Node, Boolean> {

		@Override
		public Boolean apply(final SmallTrieStringMapSetIndex<E>.Node node) {
			node.entrySet.clear();
			node.children.clear();
			return true;
		}
	}

	private final transient ReadWriteLock readWriteLock = new DebugReentrantReadWriteLock();

	/** Non-final to set it from de-serialisation */
	private Node root;

	public void setRootNote(final Node root) {
		assert root != null;
		this.root = root;
	}

	public Node getRootNode() {
		return this.root;
	}

	private transient ITransformer<KeyEntryTuple<String, Node>, Iterator<E>> TRANSFORMER_KET2EntrySet;

	private transient ITransformer<Node, Iterator<E>> TRANSFORMER_NODE2ENTRIES;

	@SuppressWarnings("unused")
	private transient ITransformer<Node, IEntrySet<E>> TRANSFORMER_NODE2ENTRYSET;

	/* this class works around the fact that Java generics + Serialization + anonymous inner class does not work */
	private static class TRANSFORMER_KET2EntrySet<E>
	implements ITransformer<KeyEntryTuple<String, SmallTrieStringMapSetIndex<E>.Node>, Iterator<E>> {

		@Override
		public Iterator<E> transform(final KeyEntryTuple<String, SmallTrieStringMapSetIndex<E>.Node> ket) {
			return ket.getSecond().entrySet.iterator();
		}
	}

	/* this class works around the fact that Java generics + Serialization + anonymous inner class does not work */
	private static class TRANSFORMER_NODE2ENTRIES<E> implements ITransformer<SmallTrieStringMapSetIndex<E>.Node, Iterator<E>> {

		@Override
		public Iterator<E> transform(final SmallTrieStringMapSetIndex<E>.Node node) {
			return node.entrySet.iterator();
		}
	}

	/* this class works around the fact that Java generics + Serialization + anonymous inner class does not work */
	private static class TRANSFORMER_NODE2ENTRYSET<E>
	implements ITransformer<SmallTrieStringMapSetIndex<E>.Node, IEntrySet<E>> {

		@Override
		public IEntrySet<E> transform(final SmallTrieStringMapSetIndex<E>.Node node) {
			return node.entrySet;
		}
	}

	public SmallTrieStringMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		assert entrySetFactory != null;
		this.entrySetFactory = entrySetFactory;
		this.root = new Node();
		initTransients();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransients();
	}

	private void initTransients() {
		this.FILTER_NON_EMPTY_ENTRYSET = new FILTER_NON_EMPTY_ENTRYSET<E>();
		this.FUNCTION_clear = new FUNCTION_clear<E>();
		this.TRANSFORMER_KET2EntrySet = new TRANSFORMER_KET2EntrySet<E>();
		this.TRANSFORMER_NODE2ENTRIES = new TRANSFORMER_NODE2ENTRIES<E>();
		this.TRANSFORMER_NODE2ENTRYSET = new TRANSFORMER_NODE2ENTRYSET<E>();
	}

	@Override
	@ModificationOperation
	public void clear() {
		writeOperationStart();
		this.root.children.clear();
		writeOperationEnd();
	}

	@Override
	@ReadOperation
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String, E> computeDiff(final IMapSetIndex<String, E> otherFuture) {

		// IMPROVE implement diff function
		throw new UnsupportedOperationException("not impl yet");
	}

	@Override
	@ReadOperation
	public ClosableIterator<E> constraintIterator(final Constraint<String> c1) {
		readOperationStart();
		final Iterator<E> it = this.root.constraintIterator(c1);
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public boolean contains(final Constraint<String> c1, final Constraint<E> entryConstraint) {
		final ClosableIterator<KeyEntryTuple<String, E>> it = tupleIterator(c1, entryConstraint);
		final boolean b = it.hasNext();
		it.close();
		return b;
	}

	@Override
	@ReadOperation
	public boolean contains(final String key, final E entry) {
		final IEntrySet<E> set = this.lookup(key);
		return set != null && set.contains(entry);
	}

	@ReadOperation
	public boolean containsKey(final Constraint<String> c1) {
		if (c1.isStar()) {
			return !isEmpty();
		} else if (c1.isExact()) {
			return containsKey(c1.getExpected());
		} else {
			// third case
			final ClosableIterator<KeyEntryTuple<String, SmallTrieStringMapSetIndex<E>.Node>> it = keyAndNodeIterator(c1);
			final boolean b = it.hasNext();
			it.close();
			return b;
		}
	}

	@Override
	@ReadOperation
	public boolean containsKey(final String key) {
		return lookup(key) != null;
	}

	@Override
	@ModificationOperation
	public boolean deIndex(final String key) {
		writeOperationStart();
		final boolean b = this.root.deIndex(null, null, key);
		writeOperationEnd();
		return b;
	}

	@Override
	@ModificationOperation
	public boolean deIndex(final String key1, final E entry) {
		writeOperationStart();
		final boolean b = this.root.deIndex(null, null, key1, entry);
		writeOperationEnd();
		return b;
	}

	@ReadOperation
	public void dump() {
		this.root.dump("", "");
	}

	public String toDebugString() {
		return this.root.toString();
	}

	@ReadOperation
	public void dumpStats() {
		readOperationStart();
		final int keys = size();
		long chars = 0;
		int entries = Iterators.count(iterator());
		String longestKey = "";
		final Iterator<String> it = this.keyIterator();
		while (it.hasNext()) {
			final String e = it.next();
			entries++;
			final String s = e.toString();
			chars += s.length();
			if (s.length() > longestKey.length()) {
				longestKey = s;
			}
		}
		System.out.println("Keys=" + keys + "\n"

		+ "Entries=" + entries + "\n"

		+ "Key-Chars=" + chars + "\n"

		+ "Longest key=" + longestKey);
		readOperationEnd();
	}

	/**
	 * If you indexed 'aaabbccc' and 'aaaddeee' and 'aaabbcff' the trie looks like this:
	 *
	 * <pre>
	 * 'aaa'
	 *    'bbc'
	 *       'cc'
	 *       'ff'
	 *    'ddeee'
	 * </pre>
	 *
	 * and the key fragments for 'aaabbcg' would be ['aaa','bbc'] with remainder 'g'.
	 *
	 * @param key must not be part of the trie
	 * @return the string key split into fragments as they appear in the trie
	 */
	@ReadOperation
	public KeyFragments getKeyFragmentsFor(final String key) {
		readOperationStart();
		final KeyFragments result = this.root.getKeyFragmentsFor(key);
		readOperationEnd();
		return result;
	}

	/**
	 * @param s
	 * @param start within s
	 * @return a pair: 1) the number of characters, starting from start, are the longest match so that at least one
	 *         result is returned; 2) the entry set of result values
	 */
	@ReadOperation
	public Pair<Integer, Set<E>> getLongestMatch(final String s, final int start) {
		readOperationStart();
		final Pair<Integer, Set<E>> result = this.root.getLongestMatch(s, start);
		readOperationEnd();
		return result;
	}

	@Override
	@ModificationOperation
	public boolean index(final String key, final E entry) {
		writeOperationStart();
		final boolean b = this.root.index(key, entry);
		writeOperationEnd();
		return b;
	}

	/**
	 * @param key
	 * @return true iff set K did not contain key yet
	 */
	@ReadOperation
	public boolean indexKey(final String insertKey) {
		readOperationStart();
		final boolean result = this.root.indexKey(insertKey);
		readOperationEnd();
		return result;
	}

	@Override
	@ReadOperation
	public boolean isEmpty() {
		return this.root.isEmpty();
	}

	@ReadOperation
	public ClosableIterator<E> iterator() {
		readOperationStart();
		final Iterator<E> it = this.root.entriesIterator();
		return unlockIteratorOnClose(it);
	}

	/**
	 * Exposed only for debugging
	 *
	 * @param c1
	 * @return ...
	 */
	@ReadOperation
	public ClosableIterator<KeyEntryTuple<String, Node>> keyAndNodeIterator(final Constraint<String> c1) {
		readOperationStart();
		final Iterator<KeyEntryTuple<String, SmallTrieStringMapSetIndex<E>.Node>> it = this.root.keyAndNodeIterator("", c1);
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public ClosableIterator<String> keyIterator() {
		readOperationStart();
		final Iterator<String> it = this.root.keyIterator("");
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public IEntrySet<E> lookup(final String key) {
		return this.root.lookup(key);
	}

	/**
	 * Let the current thread release its read lock
	 */
	private void readOperationEnd() {
		this.readWriteLock.readLock().unlock();
	}

	private void readOperationStart() {
		assert this.readWriteLock != null;
		this.readWriteLock.readLock().lock();
	}

	/**
	 * IMPROVE performance: create and use a direct prefix search returning just V's, without creating intermediate
	 * KeyEntryTuple
	 *
	 * Contains dupes, e.g. if the term "HelloWorld" has been indexed, it was also indexed as "Hello" and "World". Hence
	 * the query "hell" returns both "hello" and "helloworld", both with the same V.
	 *
	 * @param keyPrefix
	 * @return all tuples matching the keyPrefix, sorted in lexicographical order of the keys. Shorter keys before
	 *         longer keys. No order in values for the same keys.
	 */
	@ReadOperation
	public ClosableIterator<KeyEntryTuple<String, E>> search(final String keyPrefix) {
		readOperationStart();
		final ConstraintKeyPrefix constraintKeyPrefix = new ConstraintKeyPrefix(keyPrefix);
		final ClosableIterator<KeyEntryTuple<String, E>> it = tupleIterator(constraintKeyPrefix, new Wildcard<E>());
		return unlockIteratorOnClose(it);
	}

	/**
	 * @return number of different indexed keys
	 */
	@ReadOperation
	public int size() {
		return Iterators.count(keyIterator());
	}

	@Override
	@ReadOperation
	public String toString() {
		return this.root.toString();
	}

	@Override
	@ReadOperation
	public String toString(final String indent) {
		return indent+this.root.toString();
	}

	@Override
	@ReadOperation
	public ClosableIterator<KeyEntryTuple<String, E>> tupleIterator(final Constraint<String> keyConstraint,
			final Constraint<E> entryConstraint) {
		readOperationStart();
		final Iterator<KeyEntryTuple<String, E>> it = this.root.tupleIterator("", keyConstraint, entryConstraint);
		return unlockIteratorOnClose(it);
	}

	private <T> ClosableIteratorAdapter<T> unlockIteratorOnClose(final Iterator<T> baseIt) {
		return new ClosableIteratorAdapter<T>(baseIt) {
			private boolean closed = false;

			@Override
			public void close() {
				if (this.closed) {
					return;
				}
				this.closed = true;
				super.close();
				SmallTrieStringMapSetIndex.this.readOperationEnd();
			}
		};
	}

	@ReadOperation
	public ClosableIterator<IEntrySet<E>> valueAsEntrySetIterator(final Constraint<String> keyConstraint) {
		readOperationStart();
		final Iterator<IEntrySet<E>> it = this.root.valueAsEntrySetIterator("", keyConstraint);
		return unlockIteratorOnClose(it);
	}

	@ReadOperation
	public ClosableIterator<E> valueIterator(final Constraint<String> keyConstraint,
			final IFilter<E> optionalEntryFilter) {
		readOperationStart();
		final Iterator<E> it = this.root.valueIterator("", keyConstraint, optionalEntryFilter);
		return unlockIteratorOnClose(it);
	}

	private void writeOperationEnd() {
		this.readWriteLock.writeLock().unlock();
	}

	private void writeOperationStart() {
		this.readWriteLock.writeLock().lock();
	}

}

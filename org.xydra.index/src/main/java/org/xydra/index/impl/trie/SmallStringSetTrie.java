package org.xydra.index.impl.trie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
 * Current impl: this class -> {@link SortedStringMap} -> {@link SortedArrayMap}
 * 
 * IMPROVE rewrite algorithms into non-recursive form
 * 
 * IMPROVE work on byte arrays in utf8 encoding?
 * 
 * @author xamde
 * 
 *         This classes uses {@link ClosableIterator} and internal read/write
 *         locks for thread management. Always close your iterators or we will
 *         starve. Or call {@link #readOperationEnd()} to close the read lock.
 * 
 * @param <E>
 */
@NotThreadSafe
public class SmallStringSetTrie<E> implements IMapSetIndex<String, E>, Serializable {

	private static class ConstraintKeyPrefix implements Constraint<String> {

		private String keyPrefix;

		public ConstraintKeyPrefix(String keyPrefix) {
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
		public boolean matches(String element) {
			return element.startsWith(this.keyPrefix);
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}

	public static class KeyFramgents {
		public List<String> matched;

		/* the empty string, if nothing remains */
		public String remainder;

		public KeyFramgents(List<String> matched, String remainder) {
			this.matched = matched;
			this.remainder = remainder;
		}
	}

	/**
	 * public only for debug; non-static to avoid generics-overkill and have
	 * access to entrySetFactory
	 * 
	 * 
	 */
	public class Node implements Serializable {

		private static final long serialVersionUID = -125040148037301604L;

		/**
		 * A map from string (local part of key) to children (other nodes).
		 * 
		 * Invariant: Strings are prefix-free, i.e. no two strings in the map
		 * start with the same prefix, not even a single character. Different
		 * from normal tries, strings might be longer than 1 character
		 */
		private SortedStringMap<Node> children;

		/** @NeverNull */
		private IEntrySet<E> entrySet;

		public Node() {
			this.entrySet = SmallStringSetTrie.this.entrySetFactory.createInstance();
			this.children = new SortedStringMap<Node>();
		}

		public Node(E value) {
			this();
			assert value != null;
			this.entrySet.index(value);
		}

		public Iterator<E> constraintIterator(Constraint<String> c1) {
			if (c1.isStar()) {
				return entriesIterator();
			} else {
				// we need to filter
				if (c1.isExact()) {
					IEntrySet<E> node = lookup(c1.getExpected());
					if (node == null) {
						return NoneIterator.create();
					} else {
						return node.iterator();
					}
				} else {
					// search...
					Iterator<KeyEntryTuple<String, Node>> it = this.children.tupleIterator(c1);
					return Iterators.cascade(it, SmallStringSetTrie.this.TRANSFORMER_KET2EntrySet);
				}
			}
		}

		public void deIndex(Node parent, String parentKey, String removeKey) {
			assert removeKey != null;
			deIndex(parent, parentKey, removeKey, SmallStringSetTrie.this.FUNCTION_clear);
		}

		public boolean deIndex(Node parent, String parentKey, String removeKey, final E removeEntry) {
			assert removeKey != null;
			assert removeEntry != null;
			return deIndex(parent, parentKey, removeKey, new Function<Node, Boolean>() {

				@Override
				public Boolean apply(Node node) {
					return node.entrySet.deIndex(removeEntry);
				}
			});
		}

		private boolean deIndex(Node parent, String parentKey, String removeKey,
				Function<Node, Boolean> action) {
			assert removeKey != null;
			assert action != null;

			if (removeKey.length() == 0) {
				// base case, perfect match, remove here
				boolean result = action.apply(this);

				if (parent != null && this.entrySet.isEmpty()) {
					if (this.children.isEmpty()) {
						// kill this node
						parent.children.deIndex(parentKey);
					} else if (this.children.size() == 1) {
						// move this node,
						// re-balance tree
						Node singleChild = this.children.iterator().next();
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

			String matchingKey = this.children.lookupFirstPrefix(removeKey.substring(0, 1));
			if (matchingKey == null) {
				log.trace("removeKey not found");
				return false;
			}

			if (matchingKey.length() <= removeKey.length()) {
				int commonPrefixLen = getSharedPrefixLength(removeKey, matchingKey);
				assert commonPrefixLen > 0 : "at least 1 char in common: '"
						+ removeKey.substring(0, 1) + "'";
				if (commonPrefixLen == matchingKey.length()) {
					// recurse
					Node matchingNode = this.children.lookup(matchingKey);
					matchingNode.deIndex(this, removeKey.substring(0, commonPrefixLen),
							removeKey.substring(commonPrefixLen), action);
				}
			}

			log.trace("no match possible");
			return false;
		}

		public void dump(String indent, String combinedKey) {
			System.out.println();
		}

		/**
		 * @return recursively all entries
		 */
		public Iterator<E> entriesIterator() {
			return Iterators.cascade(nodeIterator(),
					SmallStringSetTrie.this.TRANSFORMER_NODE2ENTRIES);
		}

		/**
		 * @param searchKey
		 * @return the {@link KeyFramgents} for searchKey
		 */
		public KeyFramgents getKeyFragmentsFor(String searchKey) {
			assert searchKey != null;
			// recursion base case
			if (searchKey.length() == 0) {
				return new KeyFramgents(new ArrayList<String>(), "");
			}

			assert searchKey.length() > 0;
			String conflictKey = this.children.lookupFirstPrefix(searchKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				return new KeyFramgents(new ArrayList<String>(), searchKey);
			}

			Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			int commonPrefixLen = getSharedPrefixLength(searchKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + searchKey
					+ " conflictKey=" + conflictKey;
			if (conflictKey.equals(searchKey)) {
				// the conflicting node is our node
				List<String> matched = new ArrayList<String>();
				matched.add(conflictKey);
				return new KeyFramgents(matched, "");
			} else if (commonPrefixLen >= conflictKey.length()) {
				/*
				 * the conflicting key ('ba') is part of the search key ('baz')
				 */
				// RECURSION: just index ('z') in subnode
				String searchKeyPostfix = searchKey.substring(conflictKey.length());
				KeyFramgents subFragments = conflictingNode.getKeyFragmentsFor(searchKeyPostfix);
				subFragments.matched.add(0, conflictKey);
				return subFragments;
			} else {
				/*
				 * the search key ('foo') or ('foobaz') is part of the
				 * conflicting key ('foo' in 'foobar') or they share a common
				 * prefix ('foo' from 'foobaz'+'foobar'): insert a new node
				 * ('foo') & update children
				 */
				String commonPrefix = searchKey.substring(0, commonPrefixLen);
				String insertKeyPostfix = searchKey.substring(commonPrefixLen);
				List<String> matched = new ArrayList<String>();
				matched.add(commonPrefix);
				KeyFramgents fragments = new KeyFramgents(matched, insertKeyPostfix);
				return fragments;
			}
		}

		/**
		 * IMPROVE more recursive version of this algorithm
		 * 
		 * @param s
		 * @param start
		 * @return @CanBeNull or a pair (match position, entry set of matching
		 *         values). If not null, the integer is > 0.
		 */
		public Pair<Integer, Set<E>> getLongestMatch(String s, int start) {

			int len = 1;

			while (start + len < s.length()) {
				String key = s.substring(start, start + len);
				boolean b = this.containsEntriesWithPrefix(key);
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

		private Pair<Integer, Set<E>> toLongestMatchResult(String s, int start, int len) {
			if (len == 0)
				return null;

			String key = s.substring(start, start + len);

			IEntrySet<E> es = SmallStringSetTrie.this.lookup(key);
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
		public boolean index(String insertKey, E value) {
			assert insertKey != null;
			assert value != null;
			// recursion base case
			if (insertKey.length() == 0) {
				return this.entrySet.index(value);
			}

			assert insertKey.length() > 0;
			String conflictKey = this.children.lookupFirstPrefix(insertKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				this.children.index(insertKey, new Node(value));
				return true;
			}

			Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			int commonPrefixLen = getSharedPrefixLength(insertKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + insertKey
					+ " conflictKey=" + conflictKey;
			if (conflictKey.equals(insertKey)) {
				// the conflicting node is our node
				return conflictingNode.entrySet.index(value);
			} else if (commonPrefixLen >= conflictKey.length()) {
				/*
				 * the conflicting key ('ba') is part of the insertion key
				 * ('baz')
				 */
				// RECURSION: just index ('z') in subnode
				String insertKeyPostfix = insertKey.substring(conflictKey.length());
				return conflictingNode.index(insertKeyPostfix, value);
			} else {
				/*
				 * the insertion key ('foo') or ('foobaz') is part of the
				 * conflicting key ('foo' in 'foobar') or they share a common
				 * prefix ('foo' from 'foobaz'+'foobar'): insert a new node
				 * ('foo') & update children
				 */
				this.children.deIndex(conflictKey);
				Node commonNode = new Node();
				String commonPrefix = insertKey.substring(0, commonPrefixLen);
				this.children.index(commonPrefix, commonNode);
				String conflictKeyPostfix = conflictKey.substring(commonPrefixLen);
				commonNode.children.index(conflictKeyPostfix, conflictingNode);
				String insertKeyPostfix = insertKey.substring(commonPrefixLen);
				commonNode.index(insertKeyPostfix, value);
				return true;
			}
		}

		/**
		 * @param key
		 * @return true iff set K did not contain key yet
		 */
		public boolean indexKey(String insertKey) {
			assert insertKey != null;
			// recursion base case
			if (insertKey.length() == 0) {
				// the trie always contains the empty key?
				return false;
			}

			assert insertKey.length() > 0;
			String conflictKey = this.children.lookupFirstPrefix(insertKey.substring(0, 1));
			if (conflictKey == null) {
				// there is no conflict, just index here
				this.children.index(insertKey, new Node());
				return true;
			}

			Node conflictingNode = this.children.lookup(conflictKey);
			// find common prefix to extract
			int commonPrefixLen = getSharedPrefixLength(insertKey, conflictKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + insertKey
					+ " conflictKey=" + conflictKey;
			if (conflictKey.equals(insertKey)) {
				// the conflicting node is our node
				return false;
			} else if (commonPrefixLen >= conflictKey.length()) {
				/*
				 * the conflicting key ('ba') is part of the insertion key
				 * ('baz')
				 */
				// RECURSION: just index ('z') in subnode
				String insertKeyPostfix = insertKey.substring(conflictKey.length());
				return conflictingNode.indexKey(insertKeyPostfix);
			} else {
				/*
				 * the insertion key ('foo') or ('foobaz') is part of the
				 * conflicting key ('foo' in 'foobar') or they share a common
				 * prefix ('foo' from 'foobaz'+'foobar'): insert a new node
				 * ('foo') & update children
				 */
				this.children.deIndex(conflictKey);
				Node commonNode = new Node();
				String commonPrefix = insertKey.substring(0, commonPrefixLen);
				this.children.index(commonPrefix, commonNode);
				String conflictKeyPostfix = conflictKey.substring(commonPrefixLen);
				commonNode.children.index(conflictKeyPostfix, conflictingNode);
				String insertKeyPostfix = insertKey.substring(commonPrefixLen);
				commonNode.indexKey(insertKeyPostfix);
				return true;
			}
		}

		public boolean isEmpty() {
			if (!this.entrySet.isEmpty())
				return false;

			if (!this.children.isEmpty())
				return false;

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
			Iterator<KeyEntryTuple<String, Node>> fromChildrenIt = Iterators.cascade(

			this.children.tupleIterator(ANY_STRING),

			new ITransformer<KeyEntryTuple<String, Node>, Iterator<KeyEntryTuple<String, Node>>>() {

				@Override
				public Iterator<KeyEntryTuple<String, Node>> transform(
						KeyEntryTuple<String, Node> tuple) {
					/* does it make sense to recurse here? */
					String currentKey = combinedKey + tuple.getKey();
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
		 * @return an iterator over all keys for which at least one entry has
		 *         been indexed
		 */
		public Iterator<String> keyIterator(final String combinedKey) {
			Iterator<String> fromChildren = Iterators.cascade(

			this.children.tupleIterator(new Wildcard<String>()),

			new ITransformer<KeyEntryTuple<String, Node>, Iterator<String>>() {

				@Override
				public Iterator<String> transform(KeyEntryTuple<String, Node> in) {
					return in.getSecond().keyIterator(combinedKey + in.getFirst());
				}

			});

			boolean includeThisNode = !this.entrySet.isEmpty();

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

		public IEntrySet<E> lookup(String key) {
			assert key != null;
			// recursion base case
			if (key.length() == 0) {
				return this.entrySet;
			}
			if (this.children.isEmpty()) {
				return null;
			}
			/* Search character by character */
			String prefixCharacter = key.substring(0, 1);
			String matched = this.children.lookupFirstPrefix(prefixCharacter);
			if (matched == null)
				return null;
			int commonPrefixLength = getSharedPrefixLength(key, matched);
			Node next = this.children.lookup(key.substring(0, commonPrefixLength));
			if (next == null)
				return null;
			assert next != null : "matched = " + matched;
			// recursion
			return next.lookup(key.substring(commonPrefixLength));
		}

		/**
		 * @return an iterator over all nodes with entries; breadth-first
		 */
		public Iterator<Node> nodeIterator() {
			Iterator<Node> fromChildren = Iterators.cascade(this.children.iterator(),
					new ITransformer<Node, Iterator<Node>>() {

						@Override
						public Iterator<Node> transform(Node node) {
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
		public Iterator<Node> nodeIterator(final String combinedKey,
				final Constraint<String> keyConstraint) {

			Iterator<Node> thisNodeIt = null;
			if (keyConstraint.matches(combinedKey)) {
				thisNodeIt = Iterators.forOne(this);
				if (keyConstraint.isExact()) {
					// recursion base case: I am *the* searched node
					return thisNodeIt;
				}
			}

			// else we need to (also) traverse children
			Iterator<Node> fromChildrenIt = Iterators.cascade(

			this.children.tupleIterator(ANY_STRING),

			new ITransformer<KeyEntryTuple<String, Node>, Iterator<Node>>() {

				@Override
				public Iterator<Node> transform(KeyEntryTuple<String, Node> tuple) {
					/* does it make sense to recurse here? */
					String currentKey = combinedKey + tuple.getKey();
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

		/**
		 * @param keyPrefix
		 * @return
		 * @deprecated seems buggy & untested
		 */
		@Deprecated
		public Iterator<E> quick_searchPrefix(final String keyPrefix) {
			ConstraintKeyPrefix constraintKeyPrefix = new ConstraintKeyPrefix(keyPrefix);
			Iterator<Node> it = this.children.entryIterator(constraintKeyPrefix);
			return Iterators.cascade(it, SmallStringSetTrie.this.TRANSFORMER_NODE2ENTRIES);
		}

		public boolean containsEntries() {
			if (!this.entrySet.isEmpty())
				return true;

			for (SmallStringSetTrie<E>.Node childNode : this.children.values()) {
				if (childNode.containsEntries())
					return true;
			}

			return false;
		}

		/**
		 * @param searchKeyPrefix
		 * @return true if at least one entry is indexed at a key which starts
		 *         with or is equal to keyPrefix
		 */
		public boolean containsEntriesWithPrefix(final String searchKeyPrefix) {
			assert searchKeyPrefix != null;
			// recursion base case
			if (searchKeyPrefix.length() == 0) {
				return containsEntries();
			}

			assert searchKeyPrefix.length() > 0;
			String indexedKey = this.children.lookupFirstPrefix(searchKeyPrefix.substring(0, 1));
			if (indexedKey == null) {
				// there is none
				return false;
			}

			// find common prefix to extract
			int commonPrefixLen = getSharedPrefixLength(searchKeyPrefix, indexedKey);
			assert commonPrefixLen > 0 : "commonPrefixLen==0? insertKey=" + searchKeyPrefix
					+ " conflictKey=" + indexedKey;
			if (indexedKey.equals(searchKeyPrefix)) {
				// the conflicting node is our node
				Node indexedNode = this.children.lookup(indexedKey);
				return indexedNode.containsEntries();
			} else if (commonPrefixLen <= searchKeyPrefix.length()) {
				// continue searching there
				String searchKeyPrefixRemainder = searchKeyPrefix.substring(commonPrefixLen);
				Node indexedNode = this.children.lookup(indexedKey);
				return indexedNode.containsEntriesWithPrefix(searchKeyPrefixRemainder);
			} else {
				return false;
			}
		}

		/**
		 * Search via internal tuples
		 * 
		 * @param keyPrefix
		 * @return ...
		 * @deprecated seems buggy & untested
		 */
		@Deprecated
		public Iterator<E> searchPrefix(final String keyPrefix) {
			ConstraintKeyPrefix constraintKeyPrefix = new ConstraintKeyPrefix(keyPrefix);
			Iterator<KeyEntryTuple<String, Node>> it = this.children
					.tupleIterator(constraintKeyPrefix);
			return Iterators.cascade(it, SmallStringSetTrie.this.TRANSFORMER_KET2EntrySet);
		}

		@Override
		public String toString() {
			return toString("  ", "").toString();
		}

		private StringBuilder toString(String indent, String combinedKey) {
			StringBuilder b = new StringBuilder();
			b.append(indent + "Node id " + this.hashCode() + " representing " + "'" + combinedKey
					+ "'\n");
			b.append(indent + "Value = '" + this.entrySet + "'\n");

			Iterator<String> it = this.children.keyIterator();
			while (it.hasNext()) {
				String key = it.next();
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
		public Iterator<KeyEntryTuple<String, E>> tupleIterator(String combinedKey,
				Constraint<String> keyConstraint, final Constraint<E> entryConstraint) {

			/*
			 * get all c1-matching (combinedKey,Node) pairs, recursion happens
			 * only here
			 */
			Iterator<KeyEntryTuple<String, Node>> keyNodeIt = keyAndNodeIterator(combinedKey,
					keyConstraint);

			Iterator<KeyEntryTuple<String, E>> keyEntriesIt = Iterators
					.cascade(
							keyNodeIt,
							new ITransformer<KeyEntryTuple<String, Node>, Iterator<KeyEntryTuple<String, E>>>() {

								@Override
								public Iterator<KeyEntryTuple<String, E>> transform(
										final KeyEntryTuple<String, Node> ketCombinedKeyNode) {
									/*
									 * for each matching
									 * (combinedKey-Node)-pair: look in the node
									 * and take all entryConstraint-matching
									 * entries and ...
									 */
									Iterator<E> entryIt = ketCombinedKeyNode.getEntry().entrySet
											.iterator();

									Iterator<E> filteredEntryIt;
									if (entryConstraint.isStar()) {
										filteredEntryIt = entryIt;
									} else {
										// apply constraint
										filteredEntryIt = Iterators
												.filter(entryIt, entryConstraint);
									}
									/*
									 * ... wrap them back into the final
									 * KeyEntry tuples
									 */
									return Iterators.transform(filteredEntryIt,
											new ITransformer<E, KeyEntryTuple<String, E>>() {

												@Override
												public KeyEntryTuple<String, E> transform(E entry) {
													assert entry != null;

													return new KeyEntryTuple<String, E>(
															ketCombinedKeyNode.getKey(), entry);
												}
											});
								}
							});

			return keyEntriesIt;
		}

		public Iterator<IEntrySet<E>> valueAsEntrySetIterator(String combinedKey,
				Constraint<String> keyConstraint) {
			/*
			 * get all c1-matching (combinedKey,Node) pairs, recursion happens
			 * only here
			 */
			Iterator<Node> keyNodeIt = nodeIterator(combinedKey, keyConstraint);

			Iterator<IEntrySet<E>> entrySetIt = Iterators.transform(keyNodeIt,
					new ITransformer<Node, IEntrySet<E>>() {

						@Override
						public IEntrySet<E> transform(final Node ketCombinedKeyNode) {
							/*
							 * for each matching (combinedKey-Node)-pair: look
							 * in the node and take all entryConstraint-matching
							 * entries and ...
							 */
							IEntrySet<E> set = ketCombinedKeyNode.entrySet;
							return set;
						}
					});

			Iterator<IEntrySet<E>> nonEmptyEntrySetIt = Iterators.filter(entrySetIt,
					SmallStringSetTrie.this.FILTER_NON_EMPTY_ENTRYSET);

			return nonEmptyEntrySetIt;
		}

		/**
		 * @param combinedKey
		 * @param keyConstraint
		 * @param optionalEntryFilter
		 * @CanBeNull
		 * @return all matching keys X their matching entries
		 */
		public Iterator<E> valueIterator(String combinedKey, Constraint<String> keyConstraint,
				final IFilter<E> optionalEntryFilter) {

			/*
			 * get all c1-matching (combinedKey,Node) pairs, recursion happens
			 * only here
			 */
			Iterator<Node> keyNodeIt = nodeIterator(combinedKey, keyConstraint);

			Iterator<E> keyEntriesIt = Iterators.cascade(keyNodeIt,
					new ITransformer<Node, Iterator<E>>() {

						@Override
						public Iterator<E> transform(final Node ketCombinedKeyNode) {
							/*
							 * for each matching (combinedKey-Node)-pair: look
							 * in the node and take all entryConstraint-matching
							 * entries and ...
							 */
							Iterator<E> entryIt = ketCombinedKeyNode.entrySet.iterator();

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

	private static final Logger log = LoggerFactory.getLogger(SmallStringSetTrie.class);

	private static final long serialVersionUID = 1L;

	/**
	 * @param combinedKey
	 * @param c1 expected constraint
	 * @return true if further descending a prefix tree with current key
	 *         'combinedKey' has a chance of statisfying the constraint
	 */
	private static boolean canMatch(String combinedKey, Constraint<String> c1) {

		if (c1.isStar())
			return true;

		if (c1.isExact()) {
			// optimised analysis possible

			/* if we are already to deep in the tree, we won't find a match */
			if (combinedKey.length() > c1.getExpected().length())
				return false;

			/*
			 * the current combinedKey needs to be a prefix of what we are
			 * looking for
			 */
			if (!c1.getExpected().startsWith(combinedKey))
				return false;
		}
		// who knows. sure it can match. maybe.
		return true;
	}

	/**
	 * @param master
	 * @param copy
	 * @return the number of shared characters from master in copy
	 */
	public static int getSharedPrefixLength(String master, String copy) {
		assert master != null;
		assert copy != null;
		int i = 0;
		while (i < Math.min(master.length(), copy.length())) {
			int m = master.codePointAt(i);
			int c = copy.codePointAt(i);
			if (m == c) {
				i += Character.charCount(m);
			} else {
				break;
			}
		}
		return i;
	}

	private transient Factory<IEntrySet<E>> entrySetFactory;

	private final IFilter<IEntrySet<E>> FILTER_NON_EMPTY_ENTRYSET = new IFilter<IEntrySet<E>>() {

		@Override
		public boolean matches(IEntrySet<E> entry) {
			return !entry.isEmpty();
		}
	};

	public final Function<Node, Boolean> FUNCTION_clear = new Function<Node, Boolean>() {

		@Override
		public Boolean apply(Node node) {
			node.entrySet.clear();
			node.children.clear();
			return true;
		}
	};

	private ReentrantReadWriteLock readWriteLock = new DebugReentrantReadWriteLock();

	private Node root;

	public final ITransformer<KeyEntryTuple<String, Node>, Iterator<E>> TRANSFORMER_KET2EntrySet = new ITransformer<KeyEntryTuple<String, Node>, Iterator<E>>() {

		@Override
		public Iterator<E> transform(KeyEntryTuple<String, Node> ket) {
			return ket.getSecond().entrySet.iterator();
		}
	};

	final ITransformer<Node, Iterator<E>> TRANSFORMER_NODE2ENTRIES = new ITransformer<Node, Iterator<E>>() {

		@Override
		public Iterator<E> transform(Node node) {
			return node.entrySet.iterator();
		}
	};

	final ITransformer<Node, IEntrySet<E>> TRANSFORMER_NODE2ENTRYSET = new ITransformer<Node, IEntrySet<E>>() {

		@Override
		public IEntrySet<E> transform(Node node) {
			return node.entrySet;
		}
	};

	public SmallStringSetTrie(Factory<IEntrySet<E>> entrySetFactory) {
		assert entrySetFactory != null;
		this.entrySetFactory = entrySetFactory;
		this.root = new Node();
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
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String, E> computeDiff(
			IMapSetIndex<String, E> otherFuture) {

		// IMPROVE implement diff function
		throw new UnsupportedOperationException("not impl yet");
	}

	@Override
	@ReadOperation
	public ClosableIterator<E> constraintIterator(Constraint<String> c1) {
		readOperationStart();
		Iterator<E> it = this.root.constraintIterator(c1);
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public boolean contains(Constraint<String> c1, Constraint<E> entryConstraint) {
		ClosableIterator<KeyEntryTuple<String, E>> it = tupleIterator(c1, entryConstraint);
		boolean b = it.hasNext();
		it.close();
		return b;
	}

	@Override
	@ReadOperation
	public boolean contains(String key, E entry) {
		IEntrySet<E> set = this.lookup(key);
		return set != null && set.contains(entry);
	}

	@ReadOperation
	public boolean containsKey(Constraint<String> c1) {
		if (c1.isStar()) {
			return !isEmpty();
		} else if (c1.isExact()) {
			return containsKey(c1.getExpected());
		} else {
			// third case
			ClosableIterator<KeyEntryTuple<String, SmallStringSetTrie<E>.Node>> it = keyAndNodeIterator(c1);
			boolean b = it.hasNext();
			it.close();
			return b;
		}
	}

	@Override
	@ReadOperation
	public boolean containsKey(String key) {
		return lookup(key) != null;
	}

	@Override
	@ModificationOperation
	public void deIndex(String key) {
		writeOperationStart();
		this.root.deIndex(null, null, key);
		writeOperationEnd();
	}

	@Override
	@ModificationOperation
	public boolean deIndex(String key1, E entry) {
		writeOperationStart();
		boolean b = this.root.deIndex(null, null, key1, entry);
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
		int keys = size();
		long chars = 0;
		int entries = Iterators.count(iterator());
		String longestKey = "";
		Iterator<String> it = this.keyIterator();
		while (it.hasNext()) {
			String e = it.next();
			entries++;
			String s = e.toString();
			chars += s.length();
			if (s.length() > longestKey.length())
				longestKey = s;
		}
		System.out.println("Keys=" + keys + "\n"

		+ "Entries=" + entries + "\n"

		+ "Key-Chars=" + chars + "\n"

		+ "Longest key=" + longestKey);
		readOperationEnd();
	}

	/**
	 * If you indexed 'aaabbccc' and 'aaaddeee' and 'aaabbcff' the trie looks
	 * like this:
	 * 
	 * <pre>
	 * 'aaa'
	 *    'bbc'
	 *       'cc'
	 *       'ff'  
	 *    'ddeee'
	 * </pre>
	 * 
	 * and the key fragments for 'aaabbcg' would be ['aaa','bbc'] with remainder
	 * 'g'.
	 * 
	 * @param key must not be part of the trie
	 * @return the string key split into fragments as they appear in the trie
	 */
	@ReadOperation
	public KeyFramgents getKeyFragmentsFor(String key) {
		readOperationStart();
		KeyFramgents result = this.root.getKeyFragmentsFor(key);
		readOperationEnd();
		return result;
	}

	/**
	 * @param s
	 * @param start within s
	 * @return a pair: 1) the number of characters, starting from start, are the
	 *         longest match so that at least one result is returned; 2) the
	 *         entry set of result values
	 */
	@ReadOperation
	public Pair<Integer, Set<E>> getLongestMatch(String s, int start) {
		readOperationStart();
		Pair<Integer, Set<E>> result = this.root.getLongestMatch(s, start);
		readOperationEnd();
		return result;
	}

	@Override
	@ModificationOperation
	public boolean index(String key, E entry) {
		writeOperationStart();
		boolean b = this.root.index(key, entry);
		writeOperationEnd();
		return b;
	}

	/**
	 * @param key
	 * @return true iff set K did not contain key yet
	 */
	@ReadOperation
	public boolean indexKey(String insertKey) {
		readOperationStart();
		boolean result = this.root.indexKey(insertKey);
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
		Iterator<E> it = this.root.entriesIterator();
		return unlockIteratorOnClose(it);
	}

	/**
	 * Exposed only for debugging
	 * 
	 * @param c1
	 * @return ...
	 */
	@ReadOperation
	public ClosableIterator<KeyEntryTuple<String, Node>> keyAndNodeIterator(Constraint<String> c1) {
		readOperationStart();
		Iterator<KeyEntryTuple<String, SmallStringSetTrie<E>.Node>> it = this.root
				.keyAndNodeIterator("", c1);
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public ClosableIterator<String> keyIterator() {
		readOperationStart();
		Iterator<String> it = this.root.keyIterator("");
		return unlockIteratorOnClose(it);
	}

	@Override
	@ReadOperation
	public IEntrySet<E> lookup(String key) {
		return this.root.lookup(key);
	}

	/**
	 * Let the current thread release its read lock
	 */
	private void readOperationEnd() {
		this.readWriteLock.readLock().unlock();
	}

	private void readOperationStart() {
		this.readWriteLock.readLock().lock();
	}

	/**
	 * IMPROVE performance: create and use a direct prefix search returning just
	 * V's, without creating intermediate KeyEntryTuple
	 * 
	 * Contains dupes, e.g. if the term "HelloWorld" has been indexed, it was
	 * also indexed as "Hello" and "World". Hence the query "hell" returns both
	 * "hello" and "helloworld", both with the same V.
	 * 
	 * @param keyPrefix
	 * @return all tuples matching the keyPrefix, sorted in lexicographical
	 *         order of the keys. Shorter keys before longer keys. No order in
	 *         values for the same keys.
	 */
	@ReadOperation
	public ClosableIterator<KeyEntryTuple<String, E>> search(final String keyPrefix) {
		readOperationStart();
		ConstraintKeyPrefix constraintKeyPrefix = new ConstraintKeyPrefix(keyPrefix);
		ClosableIterator<KeyEntryTuple<String, E>> it = tupleIterator(constraintKeyPrefix,
				new Wildcard<E>());
		return unlockIteratorOnClose(it);
	}

	/**
	 * @deprecated looks buggy & untested
	 * @param keyPrefix
	 * @return
	 */
	@ReadOperation
	@Deprecated
	public ClosableIterator<E> searchPrefix(String keyPrefix) {
		readOperationStart();
		Iterator<E> it = this.root.searchPrefix(keyPrefix);
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
	public ClosableIterator<KeyEntryTuple<String, E>> tupleIterator(
			Constraint<String> keyConstraint, Constraint<E> entryConstraint) {
		readOperationStart();
		Iterator<KeyEntryTuple<String, E>> it = this.root.tupleIterator("", keyConstraint,
				entryConstraint);
		return unlockIteratorOnClose(it);
	}

	private <T> ClosableIteratorAdapter<T> unlockIteratorOnClose(Iterator<T> baseIt) {
		return new ClosableIteratorAdapter<T>(baseIt) {
			private boolean closed = false;

			@Override
			public void close() {
				if (this.closed)
					return;
				this.closed = true;
				super.close();
				SmallStringSetTrie.this.readOperationEnd();
			}
		};
	}

	@ReadOperation
	public ClosableIterator<IEntrySet<E>> valueAsEntrySetIterator(Constraint<String> keyConstraint) {
		readOperationStart();
		Iterator<IEntrySet<E>> it = this.root.valueAsEntrySetIterator("", keyConstraint);
		return unlockIteratorOnClose(it);
	}

	@ReadOperation
	public ClosableIterator<E> valueIterator(Constraint<String> keyConstraint,
			IFilter<E> optionalEntryFilter) {
		readOperationStart();
		Iterator<E> it = this.root.valueIterator("", keyConstraint, optionalEntryFilter);
		return unlockIteratorOnClose(it);
	}

	private void writeOperationEnd() {
		this.readWriteLock.writeLock().unlock();
	}

	private void writeOperationStart() {
		this.readWriteLock.writeLock().lock();
	}

}

package org.xydra.index.impl.trie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.NotThreadSafe;
import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.iterator.IFilter;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
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
 * @param <E>
 */
@NotThreadSafe
public class SmallStringSetTrie<E> implements IMapSetIndex<String, E>, Serializable {

	public final Function<Node, Boolean> FUNCTION_clear = new Function<Node, Boolean>() {

		@Override
		public Boolean apply(Node node) {
			node.entrySet.clear();
			node.children.clear();
			return true;
		}
	};

	public final ITransformer<KeyEntryTuple<String, Node>, Iterator<E>> TRANSFORMER_KET2EntrySet = new ITransformer<KeyEntryTuple<String, Node>, Iterator<E>>() {

		@Override
		public Iterator<E> transform(KeyEntryTuple<String, Node> ket) {
			return ket.getSecond().entrySet.iterator();
		}
	};

	private static class ConstraintKeyPrefix implements Constraint<String> {

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		private String keyPrefix;

		@Override
		public boolean matches(String element) {
			return element.startsWith(this.keyPrefix);
		}

		@Override
		public boolean isStar() {
			return false;
		}

		@Override
		public boolean isExact() {
			return false;
		}

		@Override
		public String getExpected() {
			return this.keyPrefix;
		}
	}

	private ConstraintKeyPrefix CONSTRAINT_KEY_PREFIX = new ConstraintKeyPrefix();

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

		/**
		 * Search via internal tuples
		 * 
		 * @param keyPrefix
		 * @return ...
		 */
		public Iterator<E> searchPrefix(final String keyPrefix) {
			SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX.setKeyPrefix(keyPrefix);
			Iterator<KeyEntryTuple<String, Node>> it = this.children
					.tupleIterator(SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX);
			return Iterators.cascade(it, SmallStringSetTrie.this.TRANSFORMER_KET2EntrySet);
		}

		/**
		 * @param keyPrefix
		 * @return ...
		 */
		public Iterator<E> quick_searchPrefix(final String keyPrefix) {
			SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX.setKeyPrefix(keyPrefix);
			Iterator<Node> it = this.children
					.entryIterator(SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX);
			return Iterators.cascade(it, SmallStringSetTrie.this.TRANSFORMER_NODE2ENTRIES);
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
			System.out.println(toString(indent, combinedKey));
		}

		/**
		 * @return recursively all entries
		 */
		public Iterator<E> entriesIterator() {
			return Iterators.cascade(nodeIterator(),
					SmallStringSetTrie.this.TRANSFORMER_NODE2ENTRIES);
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

	}

	private final IFilter<IEntrySet<E>> FILTER_NON_EMPTY_ENTRYSET = new IFilter<IEntrySet<E>>() {

		@Override
		public boolean matches(IEntrySet<E> entry) {
			return !entry.isEmpty();
		}
	};

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
		while (

		i < Math.min(master.length(), copy.length())

		&& master.codePointAt(i) == copy.codePointAt(i))

		{
			// TODO make unicode safe
			i++;
		}
		return i;
	}

	public static void main(String[] args) {
		System.out.println(getSharedPrefixLength("Hello World", "Hell a lot of work"));

		SmallStringSetTrie<Integer> st = new SmallStringSetTrie<Integer>(
				new SmallEntrySetFactory<Integer>());
		st.index("Hello World", 13);
		st.index("Hell", 11);
		st.index("Hell a lot of work", 666);

		st.dump();

		List<Integer> ints = new ArrayList<Integer>();
		Iterators.addAll(st.iterator(), ints);
		System.out.println(ints);

		List<String> keys = new ArrayList<String>();
		Iterators.addAll(st.keyIterator(), keys);
		System.out.println(keys);

		List<KeyEntryTuple<String, Integer>> tuples = new ArrayList<KeyEntryTuple<String, Integer>>();
		Iterators.addAll(st.tupleIterator(new Wildcard<String>(), new Wildcard<Integer>()), tuples);
		System.out.println(tuples);

		st.deIndex("Hello");
		st.deIndex("Hell");

		// next test
		SmallStringSetTrie<Void> st2 = new SmallStringSetTrie<Void>(
				new SmallEntrySetFactory<Void>());
		st2.indexKey("aaabbccc");
		st2.indexKey("aaaddeee");
		st2.indexKey("aaabbcff");
		System.out.println("-----");
		st2.dump();
		KeyFramgents ky = st2.getKeyFragmentsFor("aaabbcg");
		assert ky.matched.size() == 2;
		assert ky.matched.get(0).equals("aaa");
		assert ky.matched.get(1).equals("bbc");
		assert ky.remainder.matches("g");
		ky = st2.getKeyFragmentsFor("aaabb");
		assert ky.matched.size() == 2;
		assert ky.matched.get(0).equals("aaa");
		assert ky.matched.get(1).equals("bb");
		assert ky.remainder.matches("");
	}

	private transient Factory<IEntrySet<E>> entrySetFactory;

	private Node root;

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
	public void clear() {
		this.root.children.clear();
	}

	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String, E> computeDiff(
			IMapSetIndex<String, E> otherFuture) {

		// IMPROVE implement diff function
		throw new UnsupportedOperationException("not impl yet");
	}

	@Override
	public Iterator<E> constraintIterator(Constraint<String> c1) {
		return this.root.constraintIterator(c1);
	}

	public Iterator<E> searchPrefix(String keyPrefix) {
		return this.root.searchPrefix(keyPrefix);
	}

	@Override
	public boolean contains(Constraint<String> c1, Constraint<E> entryConstraint) {
		return tupleIterator(c1, entryConstraint).hasNext();
	}

	@Override
	public boolean contains(String key, E entry) {
		IEntrySet<E> set = this.lookup(key);
		return set != null && set.contains(entry);
	}

	public boolean containsKey(Constraint<String> c1) {
		if (c1.isStar()) {
			return !isEmpty();
		} else if (c1.isExact()) {
			return containsKey(c1.getExpected());
		} else {
			// third case
			return keyAndNodeIterator(c1).hasNext();
		}
	}

	@Override
	public boolean containsKey(String key) {
		return lookup(key) != null;
	}

	@Override
	public void deIndex(String key) {
		this.root.deIndex(null, null, key);
	}

	@Override
	public boolean deIndex(String key1, E entry) {
		return this.root.deIndex(null, null, key1, entry);
	}

	public void dump() {
		this.root.dump("", "");
	}

	public void dumpStats() {
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
	}

	@Override
	public boolean index(String key, E entry) {
		return this.root.index(key, entry);
	}

	@Override
	public boolean isEmpty() {
		return this.root.isEmpty();
	}

	public Iterator<E> iterator() {
		return this.root.entriesIterator();
	}

	/**
	 * Exposed only for debugging
	 * 
	 * @param c1
	 * @return ...
	 */
	public Iterator<KeyEntryTuple<String, Node>> keyAndNodeIterator(Constraint<String> c1) {
		return this.root.keyAndNodeIterator("", c1);
	}

	@Override
	public Iterator<String> keyIterator() {
		return this.root.keyIterator("");
	}

	@Override
	public IEntrySet<E> lookup(String key) {
		return this.root.lookup(key);
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
	public Iterator<KeyEntryTuple<String, E>> search(final String keyPrefix) {
		SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX.setKeyPrefix(keyPrefix);
		return tupleIterator(SmallStringSetTrie.this.CONSTRAINT_KEY_PREFIX, new Wildcard<E>());
	}

	/**
	 * @return number of different indexed keys
	 */
	public int size() {
		return Iterators.count(keyIterator());
	}

	@Override
	public String toString() {
		return this.root.toString();
	}

	@Override
	public Iterator<KeyEntryTuple<String, E>> tupleIterator(Constraint<String> keyConstraint,
			Constraint<E> entryConstraint) {
		return this.root.tupleIterator("", keyConstraint, entryConstraint);
	}

	public Iterator<E> valueIterator(Constraint<String> keyConstraint,
			IFilter<E> optionalEntryFilter) {
		return this.root.valueIterator("", keyConstraint, optionalEntryFilter);
	}

	public Iterator<IEntrySet<E>> valueAsEntrySetIterator(Constraint<String> keyConstraint) {
		return this.root.valueAsEntrySetIterator("", keyConstraint);
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
	public KeyFramgents getKeyFragmentsFor(String key) {
		return this.root.getKeyFragmentsFor(key);
	}

	public static class KeyFramgents {
		public KeyFramgents(List<String> matched, String remainder) {
			this.matched = matched;
			this.remainder = remainder;
		}

		public List<String> matched;
		/* the empty string, if nothing remains */
		public String remainder;

	}

	/**
	 * @param key
	 * @return true iff set K did not contain key yet
	 */
	public boolean indexKey(String insertKey) {
		return this.root.indexKey(insertKey);
	}

}

package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.index.IIntegerRangeIndex;
import org.xydra.index.iterator.AbstractLookAheadIterator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Uses internally a sorted map. Fast; memory-efficient;
 *
 * TODO improve performance and keep GWT compatibility
 *
 * @author xamde
 *
 */
public class IntegerRangeIndex implements IIntegerRangeIndex, Serializable {

	/**
	 * API to handle a sequence of ranges
	 */
	public static interface ISplitHandler {

		void onSeparator(int startInclusive, int endExclusive);

		void onToken(int startInclusive, int endExclusive);

		void onDone();

	}

	/**
	 * A span in an integer range -- might or might not belong to an indexed region of this {@link IntegerRangeIndex}.
	 * Query the {@link #isInRange()} of each {@link Span} to find out. This method is intended to split a region (e.g.
	 * a longer string) into smaller pieces (e.g. some tokens).
	 */
	public static class Span {

		int endInclusive;

		boolean isInRange;

		int startInclusive;

		public Span(final int startInclusive, final int endInclusive, final boolean isInRange) {
			super();
			assert startInclusive <= endInclusive : "start=" + startInclusive + " end=" + endInclusive;
			this.startInclusive = startInclusive;
			this.endInclusive = endInclusive;
			this.isInRange = isInRange;
		}

		public int getEndInclusive() {
			return this.endInclusive;
		}

		public int getStartInclusive() {
			return this.startInclusive;
		}

		public boolean isInRange() {
			return this.isInRange;
		}

		@Override
		public String toString() {
			return "Span [startInclusive=" + this.startInclusive + ", endInclusive=" + this.endInclusive
					+ ", isInRange=" + this.isInRange + "]";
		}

		@Override
		public int hashCode() {
			return this.startInclusive + 1024 * this.endInclusive;
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof Span) {
				final Span o = (Span) other;
				return this.startInclusive == o.startInclusive && this.endInclusive == o.endInclusive
						&& this.isInRange == o.isInRange;
			}
			return false;
		}
	}

	private static class SpanIterator extends AbstractLookAheadIterator<Span>implements Iterator<Span> {

		private final Iterator<Entry<Integer, Integer>> baseIt;

		private Span lastSpan = null;

		private final int maxValue;

		private Entry<Integer, Integer> prefetchedEntry = null;

		public SpanIterator(final Iterator<Entry<Integer, Integer>> baseIterator, final int maxValue) {
			this.baseIt = baseIterator;
			this.maxValue = maxValue;
		}

		@Override
		protected boolean baseHasNext() {
			if (this.lastSpan == null) {
				return true;
			} else if (this.lastSpan.endInclusive < this.maxValue) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected Span baseNext() {
			if (this.lastSpan == null) {
				if (!this.baseIt.hasNext()) {
					// completely empty, return the only span
					this.lastSpan = new Span(0, this.maxValue, false);
					return this.lastSpan;
				}

				this.prefetchedEntry = this.baseIt.next();
				if (this.prefetchedEntry.getKey() > 0) {
					// first span is not an integer range
					this.lastSpan = new Span(0, this.prefetchedEntry.getKey() - 1, false);
				} else {
					// coincidentially the first span starts just at 0
					assert this.prefetchedEntry.getKey() == 0;
					this.lastSpan = new Span(0, this.prefetchedEntry.getValue(), true);
					this.prefetchedEntry = null;
				}
				return this.lastSpan;
			} else {
				// we are in the middle of sending spans
				if (this.lastSpan.isInRange) {
					// next span is out of range
					assert this.prefetchedEntry == null : "was just consumed";
					if (this.baseIt.hasNext()) {
						this.prefetchedEntry = this.baseIt.next();
						this.lastSpan = new Span(this.lastSpan.endInclusive + 1, this.prefetchedEntry.getKey() - 1,
								false);
					} else {
						this.lastSpan = new Span(this.lastSpan.endInclusive + 1, this.maxValue, false);
					}
				} else {
					// next span is in range
					assert this.prefetchedEntry != null : "prefetched";
					this.lastSpan = new Span(this.lastSpan.endInclusive + 1, this.prefetchedEntry.getValue(), true);
					this.prefetchedEntry = null;
				}
				return this.lastSpan;
			}
		}

	}

	private static final Logger log = LoggerFactory.getLogger(IntegerRangeIndex.class);

	// /*
	// * Runtime: O(1) + O(1) + O(contained intervals).
	// *
	// * @see org.xydra.index.IIntegerRangeIndex#index(int, int)
	// */
	// @Override
	// public void index_OLD(int start, int end) {
	// assert start <= end : "start=" + start + " end=" + end;
	// if(log.isTraceEnabled())
	// log.trace("Index " + start + "," + end);
	//
	// int mergedStart = start;
	// int mergedEnd = end;
	// if(log.isTraceEnabled())
	// log.trace("Current: " + mergedStart + "," + mergedEnd);
	//
	// /* merge with previous? */
	//
	// // Integer prev_start = this.sortedmap.headMap(start).lastKey();
	// // Integer prev_end = this.sortedmap.get(prev_start);
	//
	// Entry<Integer,Integer> prev = this.sortedmap.floorEntry(start - 1);
	//
	// assert prev == null || start(prev) <= start - 1;
	// // [1,5] & [3,9]
	// if(prev != null && start - 1 <= end(prev)) {
	// if(log.isTraceEnabled())
	// log.trace("Merge with prev: " + prev);
	// mergedStart = start(prev);
	// mergedEnd = Math.max(end(prev), end);
	// this.sortedmap.remove(start(prev));
	// if(log.isTraceEnabled())
	// log.trace("Current: " + mergedStart + "," + mergedEnd);
	// }
	//
	// /* merge with next? */
	// Entry<Integer,Integer> next = this.sortedmap.floorEntry(end + 1);
	// assert next == null || start(next) <= end + 1;
	// if(next != null && start(next) >= mergedStart && mergedEnd + 1 >=
	// start(next)) {
	// if(log.isTraceEnabled())
	// log.trace("Merge with next: " + next);
	// mergedEnd = Math.max(mergedEnd, end(next));
	// this.sortedmap.remove(start(next));
	// if(log.isTraceEnabled())
	// log.trace("Current: " + mergedStart + "," + mergedEnd);
	// }
	//
	// /* prune contained intervals? */
	// int pruneStart = mergedStart + 1;
	// int pruneEnd = mergedEnd - 1;
	// if(pruneEnd - pruneStart > 1) {
	// pruneRanges(pruneStart, pruneEnd);
	// }
	//
	// this.sortedmap.put(mergedStart, mergedEnd);
	// }

	private static final long serialVersionUID = -6793029187873016827L;

	private static int end(final Entry<Integer, Integer> entry) {
		assert entry != null;
		return entry.getValue();
	}

	/**
	 * @param validIntervals
	 * @param s
	 * @NeverNull
	 * @return true if all Unicode codepoints of s are in an indexed range of the given index; true for the empty
	 *         string.
	 */
	public static boolean isAllCharactersInIntervals(final IIntegerRangeIndex validIntervals, final String s) {
		if (s == null) {
			throw new IllegalArgumentException("s is null");
		}
		if (s.length() == 0) {
			return true;
		}

		int i = 0;
		do {
			final int codepoint = s.codePointAt(i);
			if (!validIntervals.isInInterval(codepoint)) {
				log.trace("Invalid character at " + i + " in " + s);
				return false;
			}
			i += Character.charCount(i);
		} while (i < s.length());
		return true;
	}

	/**
	 * @param tabooIntervals
	 * @param s
	 * @NeverNull
	 * @return true if any codepoint in s is indexed in the tabooIntervals; false for the empty string
	 */
	public static boolean isAnyCharacterInIntervals(final IIntegerRangeIndex tabooIntervals, final String s) {
		if (s == null) {
			throw new IllegalArgumentException("s is null");
		}
		if (s.length() == 0) {
			return true;
		}

		int i = 0;
		do {
			final int codepoint = s.codePointAt(i);
			if (tabooIntervals.isInInterval(codepoint)) {
				log.trace("Taboo character at " + i + " in " + s);
				return true;
			}
			i += Character.charCount(i);
		} while (i < s.length());
		return false;
	}

	/**
	 * @param s @CanBeNull
	 * @param startInclusive in s
	 * @param endExclusive in s
	 * @param separators
	 * @param iSplitHandler
	 */
	public static void split(final String s, final int startInclusive, final int endExclusive,
			final IIntegerRangeIndex separators, final ISplitHandler splitHandler) {
		if (s == null || s.length() == 0) {
			return;
		}

		int index = startInclusive;
		int spanStart = index;
		/* as opposed to 'inSeparator' */
		boolean inToken = true;
		while (index < endExclusive) {

			final int c = s.codePointAt(index);

			if (separators.isInInterval(c)) {
				if (inToken) {
					// token ends FIXME can be "the empty token" when spanStart == index
					if (index > startInclusive) {
						splitHandler.onToken(spanStart, index);
					}
					spanStart = index;
					inToken = false;
				}
			} else {
				// it's a token char
				if (!inToken) {
					// separator ends
					if (index > 0) {
						splitHandler.onSeparator(spanStart, index);
					}
					spanStart = index;
					inToken = true;
				}
			}

			index += Character.charCount(c);
		}
		if (inToken) {
			splitHandler.onToken(spanStart, index);
		} else {
			splitHandler.onSeparator(spanStart, index);
		}
		splitHandler.onDone();
	}

	/**
	 * Split a string simultaneously in two kinds of tokens, in a single pass
	 *
	 * @param s @CanBeNull
	 * @param separatorsA
	 * @param separatorsB
	 * @param splitHandlerA
	 * @param splitHandlerB
	 */
	/**
	 * @param s @CanBeNull
	 * @param startInclusive index in s
	 * @param endExclusive index in s
	 * @param separatorsA
	 * @param separatorsB
	 * @param splitHandlerA
	 * @param splitHandlerB
	 */
	public static void split2(final String s, final int startInclusive, final int endExclusive,
			final IIntegerRangeIndex separatorsA, final IIntegerRangeIndex separatorsB,
			final ISplitHandler splitHandlerA, final ISplitHandler splitHandlerB) {
		if (s == null) {
			return;
		}
		final int length = endExclusive - startInclusive;
		if (length == 0) {
			return;
		}

		int i = startInclusive;
		int spanStartA = i;
		int spanStartB = i;
		/* as opposed to 'inSeparator' */
		boolean inTokenA = true;
		boolean inTokenB = true;
		while (i < endExclusive) {
			final int c = s.codePointAt(i);

			// ==== A
			if (separatorsA.isInInterval(c)) {
				if (inTokenA) {
					// token ends
					if (i > 0) {
						splitHandlerA.onToken(spanStartA, i);
					}
					spanStartA = i;
					inTokenA = false;
				}
			} else {
				// it's a token char
				if (!inTokenA) {
					// separator ends
					if (i > 0) {
						splitHandlerA.onSeparator(spanStartA, i);
					}
					spanStartA = i;
					inTokenA = true;
				}
			}

			// ==== B
			if (separatorsB.isInInterval(c)) {
				if (inTokenB) {
					// token ends
					if (i > 0) {
						splitHandlerB.onToken(spanStartB, i);
					}
					spanStartB = i;
					inTokenB = false;
				}
			} else {
				// it's a token char
				if (!inTokenB) {
					// separator ends
					if (i > 0) {
						splitHandlerB.onSeparator(spanStartB, i);
					}
					spanStartB = i;
					inTokenB = true;
				}
			}

			i += Character.charCount(c);
		}
		// last span A
		if (inTokenA) {
			splitHandlerA.onToken(spanStartA, i);
		} else {
			splitHandlerA.onSeparator(spanStartA, i);
		}
		// last span B
		if (inTokenB) {
			splitHandlerB.onToken(spanStartB, i);
		} else {
			splitHandlerB.onSeparator(spanStartB, i);
		}
	}

	private static int start(final Entry<Integer, Integer> entry) {
		assert entry != null;
		return entry.getKey();
	}

	private final TreeMap<Integer, Integer> sortedmap = new TreeMap<Integer, Integer>();

	public void addAll(final IIntegerRangeIndex other) {
		final Iterator<Entry<Integer, Integer>> it = other.rangesIterator();
		while (it.hasNext()) {
			final Map.Entry<Integer, Integer> entry = it.next();
			index(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		this.sortedmap.clear();
	}

	public void deIndex(final int codePoint) {
		deIndex(codePoint, codePoint);
	}

	@Override
	public void deIndex(final int start, final int end) {
		assert start <= end;
		log.debug("De-index " + start + "," + end);

		/**
		 * Cases:
		 *
		 * 1) split existing range,
		 *
		 * 2) adapt 1 range at start + adapt 1 range at end + delete ranges in the middle,
		 *
		 * 3) nothing changes
		 */

		/* prev = entry with the greatest key <= start, or null if there is no such key */

		final SortedMap<Integer, Integer> prevHeadMap = this.sortedmap.headMap(start + 1);
		if (!prevHeadMap.isEmpty()) {
			final Integer prev_start = prevHeadMap.lastKey();
			assert prev_start != null;
			assert prev_start <= start : "start(prev)=" + prev_start + " start=" + start;
			final Integer prev_end = prevHeadMap.get(prev_start);
			if (start <= prev_end) {
				// prev needs to be trimmed/split

				if (end == prev_end) {
					// corner case: trim & done
					this.sortedmap.remove(prev_start);
					this.sortedmap.put(prev_start, start - 1);

					return;
				} else if (end < prev_end) {
					// split & done
					assert prev_start <= start - 1;
					this.sortedmap.remove(prev_start);
					this.sortedmap.put(prev_start, start - 1);
					this.sortedmap.put(end + 1, prev_end);
					return;
				} else {
					// trim & not done
					this.sortedmap.remove(prev_start);
					this.sortedmap.put(prev_start, start - 1);
				}
			}
		}

		// Entry<Integer,Integer> prev = this.sortedmap.floorEntry(start - 1);
		// if(prev != null) {
		// assert start(prev) < start : "start(prev)=" + start(prev) + " start="
		// + start;
		//
		// if(start <= end(prev)) {
		// // prev needs to be trimmed/split
		//
		// if(end == end(prev)) {
		// // corner case: trim & done
		//
		// int oldStart = prev.getKey();
		// this.sortedmap.remove(prev.getKey());
		// this.sortedmap.put(oldStart, start - 1);
		//
		// return;
		// } else if(end < end(prev)) {
		// // split & done
		// int oldEnd = end(prev);
		//
		// assert end < oldEnd;
		// assert start(prev) <= start - 1;
		//
		// int oldStart = start(prev);
		// this.sortedmap.remove(prev.getKey());
		// this.sortedmap.put(oldStart, start - 1);
		//
		// this.sortedmap.put(end + 1, oldEnd);
		// return;
		// } else {
		// // trim & not done
		// int oldStart = start(prev);
		// this.sortedmap.remove(prev.getKey());
		// this.sortedmap.put(oldStart, start - 1);
		// }
		// }
		// }

		/* next = entry with the greatest key <= end, or null if there is no such key */
		final SortedMap<Integer, Integer> nextHeadMap = this.sortedmap.headMap(end - 1);
		// assert prev was null || prev was trimmed
		if (!nextHeadMap.isEmpty()) {
			final Integer next_start = nextHeadMap.lastKey();
			assert next_start != null;
			assert next_start <= end;
			final Integer next_end = nextHeadMap.get(next_start);
			if (end < next_end) {
				// trim start of next
				this.sortedmap.remove(next_start);
				this.sortedmap.put(end + 1, next_end);
			} else {
				assert end >= next_end;
				// delete next, will happen in next loop anyway
			}
		}

		// // assert prev was null || prev was trimmed
		// Entry<Integer,Integer> next = this.sortedmap.floorEntry(end);
		// if(next != null) {
		// assert start(next) <= end;
		//
		// if(end < end(next)) {
		// // trim start of next
		// int oldEnd = end(next);
		// this.sortedmap.remove(next.getKey());
		// this.sortedmap.put(end + 1, oldEnd);
		// } else {
		// assert end >= end(next);
		// // delete next, will happen in next loop anyway
		// }
		// }

		/* delete inner ranges */

		/* prune contained intervals? */
		if (end - start > 1) {
			pruneRanges(start, end);
		}
	}

	@Override
	public void dump() {
		final Iterator<Entry<Integer, Integer>> it = rangesIterator();
		while (it.hasNext()) {
			final Map.Entry<Integer, Integer> entry = it.next();
			log.info("[" + start(entry) + ", " + end(entry) + "]");
		}
	}

	/**
	 * Index the trivial range [i,i]
	 *
	 * @param i
	 * @return this
	 */
	public IntegerRangeIndex index(final int i) {
		index(i, i);
		return this;
	}

	/* Runtime: O(1) + O(1) + O(contained intervals).
	 *
	 * @see org.xydra.index.IIntegerRangeIndex#index(int, int) */
	@Override
	public void index(final int start, final int end) {
		assert start <= end : "start=" + start + " end=" + end;
		if (log.isTraceEnabled()) {
			log.trace("Index " + start + "," + end);
		}

		int mergedStart = start;
		int mergedEnd = end;
		if (log.isTraceEnabled()) {
			log.trace("Current: " + mergedStart + "," + mergedEnd);
		}

		/* merge with previous? */
		final SortedMap<Integer, Integer> prevHeadMap = this.sortedmap.headMap(start);
		if (!prevHeadMap.isEmpty()) {
			final Integer prev_start = prevHeadMap.lastKey();
			assert prev_start != null;
			assert prev_start <= start - 1;
			final Integer prev_end = this.sortedmap.get(prev_start);
			assert prev_end != null;
			// [1,5] & [3,9]
			if (start - 1 <= prev_end) {
				if (log.isTraceEnabled()) {
					log.trace("Merge with prev: [" + prev_start + "," + prev_end + "]");
				}
				mergedStart = prev_start;
				mergedEnd = Math.max(prev_end, end);
				this.sortedmap.remove(prev_start);
				if (log.isTraceEnabled()) {
					log.trace("Current: [" + mergedStart + "," + mergedEnd + "]");
				}
			}
		}

		/* merge with next? */

		final SortedMap<Integer, Integer> nextHeadMap = this.sortedmap.headMap(mergedEnd + 2);
		if (!nextHeadMap.isEmpty()) {
			final Integer next_start = nextHeadMap.lastKey();
			assert next_start != null;
			assert next_start <= end + 1;
			if (next_start >= mergedStart && mergedEnd + 1 >= next_start) {
				final Integer next_end = this.sortedmap.get(next_start);
				assert next_end != null;
				if (log.isTraceEnabled()) {
					log.trace("Merge with next: [" + next_start + "," + next_end + "]");
				}
				mergedEnd = Math.max(mergedEnd, next_end);
				this.sortedmap.remove(next_start);
				if (log.isTraceEnabled()) {
					log.trace("Current: " + mergedStart + "," + mergedEnd);
				}
			}
		}

		/* prune contained intervals? */
		final int pruneStart = mergedStart + 1;
		final int pruneEnd = mergedEnd - 1;
		if (pruneEnd - pruneStart > 1) {
			pruneRanges(pruneStart, pruneEnd);
		}

		this.sortedmap.put(mergedStart, mergedEnd);
	}

	@Override
	public boolean isEmpty() {
		return this.sortedmap.isEmpty();
	}

	@Override
	public boolean isInInterval(final int i) {

		final SortedMap<Integer, Integer> headMap = this.sortedmap.headMap(i + 1);
		if (headMap.isEmpty()) {
			return false;
		}

		final Integer prev_start = headMap.lastKey();
		assert prev_start != null;

		if (prev_start > i) {
			return false;
		}

		final Integer prev_end = headMap.get(prev_start);
		return i <= prev_end;

		//
		// Entry<Integer,Integer> prev = this.sortedmap.floorEntry(i);
		// if(prev == null)
		// return false;
		// return start(prev) <= i && i <= end(prev);
	}

	// /**
	// * @return a new {@link IntegerRangeIndex} in which every span NOT indexed
	// in this index, IS indexed and vice versa.
	// */
	// public IntegerRangeIndex invert(int maxInclusive) {
	// IntegerRangeIndex inverted = new IntegerRangeIndex();
	//
	// }

	private void pruneRanges(final int start, final int end) {
		if (log.isTraceEnabled()) {
			log.trace("pruning in range [" + start + "," + end + "]");
		}
		final SortedMap<Integer, Integer> sub = this.sortedmap.subMap(start, end + 1);
		final Iterator<Entry<Integer, Integer>> it = sub.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, Integer> e = it.next();
			if (log.isTraceEnabled()) {
				log.trace("pruning entry [" + start(e) + "," + end(e) + "]");
			}
			it.remove();
		}
	}

	@Override
	public Iterator<Entry<Integer, Integer>> rangesIterator() {
		return this.sortedmap.entrySet().iterator();
	}

	/**
	 * @return number of stored ranges, NOT number of contained integer numbers
	 */
	public int size() {
		return this.sortedmap.size();
	}

	/**
	 * @param maxValueInclusive inclusive; used only at then end of the integerRange to append a final span. Typically
	 *        use length() - 1.
	 * @return all spans until maxValue; each span has a boolean flag to indicate, if the span is within an integer
	 *         range or between two of them.
	 */
	public Iterator<Span> spanIterator(final int maxValueInclusive) {
		return new SpanIterator(this.sortedmap.entrySet().iterator(), maxValueInclusive);
	}

}

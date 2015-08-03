package org.xydra.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;

/**
 * Some generic tools for working with java.util.Map
 *
 * @author xamde
 */
public class TransformerTool {

	/**
	 * This is not fast.
	 *
	 * @param mapKW
	 * @param transformer
	 * @return a new {@link HashMap} with all values transformed
	 */
	public static <K, V, W> Map<K, V> transformMapValues(final Map<K, W> mapKW,
			final ITransformer<W, V> transformer) {
		if (mapKW == null) {
			return null;
		}

		final Map<K, V> mapKV = new HashMap<K, V>(mapKW.size());
		for (final Entry<K, W> e : mapKW.entrySet()) {
			final K key = e.getKey();
			final V value = transformer.transform(e.getValue());

			mapKV.put(key, value);
		}

		return mapKV;
	}

	/**
	 * This is not fast.
	 *
	 * @param mapIn
	 * @param keyTransformer
	 * @param valueTransformer
	 * @return a new {@link HashMap} with all values transformed
	 */
	public static <KI, KO, VI, VO> Map<KO, VO> transformMapKeyAndValues(final Map<KI, VI> mapIn,
			final ITransformer<KI, KO> keyTransformer, final ITransformer<VI, VO> valueTransformer) {
		if (mapIn == null) {
			return null;
		}

		final Map<KO, VO> mapKV = new HashMap<KO, VO>(mapIn.size());
		for (final Entry<KI, VI> e : mapIn.entrySet()) {
			final KI keyIn = e.getKey();
			final KO keyOut = keyTransformer.transform(keyIn);
			final VI valueIn = e.getValue();
			final VO valueOut = valueTransformer.transform(valueIn);

			mapKV.put(keyOut, valueOut);
		}

		return mapKV;
	}

	/**
	 * This is not fast.
	 *
	 * @param listIn
	 * @param transformer
	 * @return a new {@link ArrayList}
	 */
	public static <I, O> List<O> transformListEntries(final List<I> listIn, final ITransformer<I, O> transformer) {
		if (listIn == null) {
			return null;
		}

		final List<O> listOut = new ArrayList<O>(listIn.size());
		for (final I in : listIn) {
			final O out = transformer.transform(in);
			listOut.add(out);
		}

		return listOut;
	}

	public static <I, O> Iterator<O> transformIterator(final Iterator<I> iteratorIn,
			final ITransformer<I, O> transformer) {
		return new TransformingIterator<I, O>(iteratorIn, transformer);
	}

	public static <I, O> Iterable<O> transformIterable(final Iterable<I> iterableIn,
			final ITransformer<I, O> transformer) {
		return new TransformingIterable<I, O>(iterableIn, transformer);
	}

	public static class TransformingIterable<I, O> implements Iterable<O> {

		private final ITransformer<I, O> transformer;
		private final Iterable<I> iterable;

		public TransformingIterable(final Iterable<I> iterableIn, final ITransformer<I, O> transformer) {
			this.iterable = iterableIn;
			this.transformer = transformer;
		}

		@Override
		public Iterator<O> iterator() {
			return new TransformingIterator<I, O>(this.iterable.iterator(), this.transformer);
		}
	}

}

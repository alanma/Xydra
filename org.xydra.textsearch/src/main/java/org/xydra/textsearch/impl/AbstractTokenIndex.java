package org.xydra.textsearch.impl;

import java.util.Collection;
import java.util.Iterator;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.textsearch.PragmaticTextSearch.Normaliser;


public abstract class AbstractTokenIndex<V> {

	protected MapSetIndex<String, V> matches;

	protected String splitRegex;

	protected Normaliser normaliser;

	public AbstractTokenIndex() {
		Factory<IEntrySet<V>> entrySetFactory = new FastEntrySetFactory<V>();
		this.matches = new MapSetIndex<String, V>(entrySetFactory);
	}

	public Iterator<V> search(String token) {
		return this.matches.constraintIterator(new EqualsConstraint<String>(
				this.normaliser.normalise(token)));
	}

	public void clear() {
		this.matches.clear();
	}

	public void configure(String splitRegex, Normaliser normaliser) {
		this.splitRegex = splitRegex;
		this.normaliser = normaliser;
	}

	/**
	 * @param token
	 * @return fragments of this token to be indexed
	 */
	protected abstract Collection<String> generateTokenFragments(String token);

	public void deIndex(V identifier, String text) {
		// tokenise
		String[] tokens = text.split(this.splitRegex);
		for (String token : tokens) {
			// generate partial tokens
			Collection<String> tokenFragments = generateTokenFragments(token);
			for (String tokenFragment : tokenFragments) {
				// index
				this.matches.deIndex(this.normaliser.normalise(tokenFragment),
						identifier);
			}
		}
	}

	public void index(V identifier, String text) {
		// tokenise
		String[] tokens = text.split(this.splitRegex);
		for (String token : tokens) {
			// generate partial tokens
			Collection<String> tokenFragments = generateTokenFragments(token);
			for (String tokenFragment : tokenFragments) {
				// index
				this.matches.index(this.normaliser.normalise(tokenFragment),
						identifier);
			}
		}
	}

}

package org.xydra.textsearch.impl;

import java.util.Iterator;

import org.xydra.index.iterator.BagUnionIterator;
import org.xydra.textsearch.PragmaticTextSearch;


public class PTSImpl<V> implements PragmaticTextSearch<V> {

	private PrefixIndex<V> prefixIndex;

	private SubstringIndex<V> substringIndex;

	public void dumpStats() {
		// System.out.println(this.prefixMatches.size() + " prefixes and "
		// + this.substringMatches.size() + " substrings indexed.");
	}

	public Iterator<V> search(String token) {
		if (token.length() == 1 || token.length() == 2) {
			return this.prefixIndex.search(token);
		} else {
			return this.substringIndex.search(token);
		}
	}

	public void clear() {
		this.prefixIndex.clear();
		this.substringIndex.clear();
	}

	/* (non-Javadoc)
	 * @see de.xam.ptextsearch.PragmaticTextSearch#configure(java.lang.String, java.lang.String, de.xam.ptextsearch.PragmaticTextSearch.Normaliser)
	 */
	public void configure(String prefixSplitRegex, String substringSplitRegex, Normaliser normaliser) {
		this.prefixIndex = new PrefixIndex<V>(1, 2);
		this.substringIndex = new SubstringIndex<V>(3, 20);
		this.prefixIndex.configure(prefixSplitRegex, normaliser);
		this.substringIndex.configure(substringSplitRegex, normaliser);
	}

	public void deIndex(V identifier, String text) {
		this.prefixIndex.deIndex(identifier, text);
		this.substringIndex.deIndex(identifier, text);
	}

	public void index(V identifier, String text) {
		this.prefixIndex.index(identifier, text);
		this.substringIndex.index(identifier, text);
	}

	public Iterator<V> searchBoth(String token) {
		return new BagUnionIterator<V>(this.prefixIndex
				.search(token), this.substringIndex.search(token));
	}
	
	public static void main(String[] args) {
		
	}
	


}

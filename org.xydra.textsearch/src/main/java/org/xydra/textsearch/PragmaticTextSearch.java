package org.xydra.textsearch;

import java.util.Iterator;

public interface PragmaticTextSearch<V> {

	/**
	 * @param prefixSplitRegex
	 *            must be a valid regex usable for split(). Used to tokenise
	 *            words of which the first one/two characters are used for
	 *            prefix matches.
	 * @param wordSplitRegex
	 *            must be a valid regex usable for split(). Used to tokenise
	 *            words, from which substrings are generated.
	 * @param normaliser
	 */
	void configure(String prefixSplitRegex, String wordSplitRegex,
			Normaliser normaliser);

	/**
	 * Public API. Adds text to the index. Text is split into tokens, tokens are
	 * further processed.
	 * 
	 * @param identifier
	 * @param text
	 *            For a text.length == 1 or == 2: Prefixes are indexed.
	 * 
	 *            For text.length >= 3: all possible substrings are indexed.
	 */
	void index(V identifier, String text);

	/**
	 * Public API. Removes text from the index.
	 * 
	 * @param identifier
	 * @param text
	 */
	void deIndex(V identifier, String text);

	/**
	 * Delete the complete index.
	 */
	void clear();

	/**
	 * Public API. Search the index.
	 * 
	 * @param token
	 *            should not contain characters used for tokenisation itself,
	 *            i.e., no whitespace.
	 * @return Never null. If 'token'.length == 1 or == 2: returns prefix
	 *         matches. For longer tokens: substring-matches.
	 */
	Iterator<V> search(String token);

	public static interface Normaliser {
		/**
		 * Normalises tokens and incoming queries.
		 * 
		 * @param raw
		 * @return a normalised String
		 */
		String normalise(String raw);
	}

}

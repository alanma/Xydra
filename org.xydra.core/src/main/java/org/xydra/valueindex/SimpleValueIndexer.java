package org.xydra.valueindex;

import java.util.HashSet;


/**
 * Simple implementation of {@link StringValueSimpleIndexerAdapter}. Strings are
 * split by using the \W regular expression (basically every single word in the
 * String will be returned as one index string and all non-character symbols
 * will be removed).
 * 
 * @author Kaidel
 * 
 */

public class SimpleValueIndexer extends StringValueSimpleIndexerAdapter {
	public SimpleValueIndexer(ValueIndex index) {
		super(index);
	}
	
	/**
	 * Splits the given String by using the \W regular expression (basically
	 * every single word in the String will be returned as one index string and
	 * all non-character symbols will be removed).
	 * 
	 * Each Index String will only be contained once in the returned array, even
	 * if the given String contains it multiple times.
	 * 
	 * @param value The String which is to be split in its index strings
	 * @return an array containing all different Strings contained in the given
	 *         which are to be indexed.
	 */
	@Override
	public String[] getStringIndexStrings(String value) {
		String[] words = value.split("[\\W]");
		
		// using a hash set gets rid of multiple occurrences of each string
		HashSet<String> indexStrings = new HashSet<String>();
		
		for(int i = 0; i < words.length; i++) {
			indexStrings.add(words[i].toLowerCase());
		}
		
		String[] array = new String[indexStrings.size()];
		return indexStrings.toArray(array);
	}
	
	/**
	 * Simply returns the String "null".
	 */
	@Override
	public String getIndexStringForNull() {
		return "null";
	}
}

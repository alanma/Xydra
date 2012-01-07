package org.xydra.index;

import org.xydra.base.XAddress;


// TODO Document

public class SimpleValueIndexer extends StringValueSimpleIndexerAdapter {
	public SimpleValueIndexer(IMapSetIndex<String,XAddress> index) {
		super(index);
	}
	
	public String[] getStringIndexStrings(String value) {
		// TODO How to deal with punctuation marks etc? (is this the right
		// regex?)
		String[] words = value.split("\\W]");
		String[] indexes = new String[words.length];
		
		for(int i = 0; i < indexes.length; i++) {
			indexes[i] = words[i].toLowerCase();
		}
		
		return indexes;
	}
}

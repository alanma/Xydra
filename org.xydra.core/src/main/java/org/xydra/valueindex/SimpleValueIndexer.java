package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.index.IMapSetIndex;


// TODO Document

public class SimpleValueIndexer extends StringValueSimpleIndexerAdapter {
	public SimpleValueIndexer(IMapSetIndex<String,XAddress> index) {
		super(index);
	}
	
	public String[] getStringIndexStrings(String value) {
		String[] words = value.split("[\\W]");
		String[] indexes = new String[words.length];
		
		for(int i = 0; i < indexes.length; i++) {
			indexes[i] = words[i].toLowerCase();
		}
		
		return indexes;
	}
}
package org.xydra.valueindex;



// TODO Document

public class SimpleValueIndexer extends StringValueSimpleIndexerAdapter {
	public SimpleValueIndexer(ValueIndex index) {
		super(index);
	}
	
	@Override
	public String[] getStringIndexStrings(String value) {
		String[] words = value.split("[\\W]");
		String[] indexes = new String[words.length];
		
		for(int i = 0; i < indexes.length; i++) {
			indexes[i] = words[i].toLowerCase();
		}
		
		return indexes;
	}
}

package org.xydra.valueindex;

import org.xydra.index.IMapSetIndex;


public interface ValueIndex extends IMapSetIndex<String,ValueIndexEntry> {
	/*
	 * TODO Document how these methods differ from the former
	 * (increment/decrement the counter before finally (de)indexing etc.)
	 * 
	 * TODO Don't forget to document that the counter variable in the given
	 * entry-Object is ignored!
	 * 
	 * TODO Document that changes to returned ValueIndexEntries will NOT changed the stored entries
	 */

	@Override
	public void index(String key, ValueIndexEntry entry);
	
	@Override
	public void deIndex(String key, ValueIndexEntry entry);
	
}

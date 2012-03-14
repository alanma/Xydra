package org.xydra.valueindex;

import java.util.HashSet;

import org.xydra.base.XID;


/**
 * Implementation of {@link XModelObjectLevelIndexTest} for testing
 * {@link MemoryMapSetIndex}.
 * 
 * @author Kaidel
 * 
 */

public class InMemoryValueIndexTest extends XModelObjectLevelIndexTest {
	private HashSet<XID> emptySet = new HashSet<XID>();
	
	@Override
	public void initializeIndexes() {
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer, true,
		        this.emptySet, this.emptySet);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer, true,
		        this.emptySet, this.emptySet);
	}
	
	@Override
	public void initializeIndexers() {
		this.oldIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
	}
}

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
	
	@Override
	public void initializeIndexes() {
		HashSet<XID> emptySet = new HashSet<XID>();
		
		// oldModel, oldIndexer, newModel, newIndexer, excludedIds and
		// includedIds need to be set before calling all this!
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer, true, emptySet,
		        this.excludedIds);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer, true, emptySet,
		        this.excludedIds);
		
		this.oldExcludeAllIndex = new XModelObjectLevelIndex(this.oldExcludeAllModel,
		        this.oldExcludeAllIndexer, false, this.includedIds, emptySet);
		this.newExcludeAllIndex = new XModelObjectLevelIndex(this.newExcludeAllModel,
		        this.newExcludeAllIndexer, false, this.includedIds, emptySet);
	}
	
	@Override
	public void initializeIndexers() {
		this.oldIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		
		this.oldExcludeAllIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newExcludeAllIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
	}
}

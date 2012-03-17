package org.xydra.valueindex;

import java.util.HashSet;

import org.xydra.base.XID;


/**
 * Implementation of {@link XModelObjectLevelIndexTest} for testing
 * {@link StringValueIndex}.
 * 
 * @author Kaidel
 * 
 */

public class StringValueIndexTest extends XModelObjectLevelIndexTest {
	
	@Override
	public void initializeIndexes() {
		// TODO this could be moved into the general test itself, its the same
		// for both tests...
		
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
		StringMap oldMap = new MockStringMap();
		StringMap newMap = new MockStringMap();
		
		StringValueIndex oldIndex = new StringValueIndex(oldMap);
		StringValueIndex newIndex = new StringValueIndex(newMap);
		
		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);
		
		StringMap oldExcludeAllMap = new MockStringMap();
		StringMap newExcludeAllMap = new MockStringMap();
		
		StringValueIndex oldExcludeAllIndex = new StringValueIndex(oldExcludeAllMap);
		StringValueIndex newExcludeAllIndex = new StringValueIndex(newExcludeAllMap);
		
		this.oldExcludeAllIndexer = new SimpleValueIndexer(oldExcludeAllIndex);
		this.newExcludeAllIndexer = new SimpleValueIndexer(newExcludeAllIndex);
	}
}

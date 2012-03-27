package org.xydra.valueindex;



/**
 * Implementation of {@link XFieldLevelIndexTest} for testing
 * {@link StringValueIndex}.
 * 
 * @author Kaidel
 * 
 */

public class StringValueIndexTest extends XFieldLevelIndexTest {
	
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

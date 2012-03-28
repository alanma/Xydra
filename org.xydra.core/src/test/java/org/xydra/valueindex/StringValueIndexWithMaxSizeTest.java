package org.xydra.valueindex;



/**
 * Implementation of {@link XFieldLevelIndexTest} for testing
 * {@link StringValueIndex} with the "maxSize" variable set to 1 (= no value
 * will be stored, only the field address).
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Maybe choose a value which is not as restrictive?
 */

public class StringValueIndexWithMaxSizeTest extends XFieldLevelIndexTest {
	
	@Override
	public void initializeIndexers() {
		StringMap oldMap = new MockStringMap();
		StringMap newMap = new MockStringMap();
		
		StringValueIndex oldIndex = new StringValueIndex(oldMap, 1);
		StringValueIndex newIndex = new StringValueIndex(newMap, 1);
		
		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);
		
		StringMap oldExcludeAllMap = new MockStringMap();
		StringMap newExcludeAllMap = new MockStringMap();
		
		StringValueIndex oldExcludeAllIndex = new StringValueIndex(oldExcludeAllMap, 1);
		StringValueIndex newExcludeAllIndex = new StringValueIndex(newExcludeAllMap, 1);
		
		this.oldExcludeAllIndexer = new SimpleValueIndexer(oldExcludeAllIndex);
		this.newExcludeAllIndexer = new SimpleValueIndexer(newExcludeAllIndex);
	}
}

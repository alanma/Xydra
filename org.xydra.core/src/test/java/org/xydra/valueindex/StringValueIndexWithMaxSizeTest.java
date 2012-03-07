package org.xydra.valueindex;

/**
 * Implementation of {@link XModelObjectLevelIndexTest} for testing
 * {@link StringValueIndex} with the "maxSize" variable set to 1 (= no value
 * will be stored, only the field address).
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Maybe choose a value which is not as restrictive?
 */

public class StringValueIndexWithMaxSizeTest extends XModelObjectLevelIndexTest {
	@Override
	public void initializeIndexes() {
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer);
	}
	
	@Override
	public void initializeIndexers() {
		StringMap oldMap = new MockStringMap();
		StringMap newMap = new MockStringMap();
		
		StringValueIndex oldIndex = new StringValueIndex(oldMap, 1);
		StringValueIndex newIndex = new StringValueIndex(newMap, 1);
		
		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);
	}
}

package org.xydra.valueindex;

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
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer);
	}
	
	@Override
	public void initializeIndexers() {
		StringMap oldMap = new MockStringMap();
		StringMap newMap = new MockStringMap();
		
		StringValueIndex oldIndex = new StringValueIndex(oldMap);
		StringValueIndex newIndex = new StringValueIndex(newMap);
		
		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);
	}
}

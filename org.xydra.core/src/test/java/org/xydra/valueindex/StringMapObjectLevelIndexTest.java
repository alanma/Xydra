package org.xydra.valueindex;

import org.xydra.core.model.XModel;


public class StringMapObjectLevelIndexTest extends XModelObjectLevelIndexTest {
	@Override
	public void initializeIndexes(XModel oldModel, XModel newModel, XValueIndexer oldIndexer,
	        XValueIndexer newIndexer) {
		this.oldIndex = new XModelObjectLevelIndex(oldModel, oldIndexer);
		this.newIndex = new XModelObjectLevelIndex(newModel, newIndexer);
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

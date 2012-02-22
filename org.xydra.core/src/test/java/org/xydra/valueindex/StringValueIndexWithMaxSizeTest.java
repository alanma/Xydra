package org.xydra.valueindex;

import org.xydra.core.model.XModel;


/**
 * Test for {@link StringValueIndex} with the "maxSize" variable set to 1 (=
 * every value will be stored as null).
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Maybe choose a value which is not as restrictive?
 */

public class StringValueIndexWithMaxSizeTest extends XModelObjectLevelIndexTest {
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
		
		StringValueIndex oldIndex = new StringValueIndex(oldMap, 1);
		StringValueIndex newIndex = new StringValueIndex(newMap, 1);
		
		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);
	}
}

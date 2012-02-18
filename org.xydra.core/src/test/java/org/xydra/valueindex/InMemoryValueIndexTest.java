package org.xydra.valueindex;

import org.xydra.core.model.XModel;


/**
 * Test for {@link MemoryMapSetIndex}.
 * 
 * @author Kaidel
 * 
 */

public class InMemoryValueIndexTest extends XModelObjectLevelIndexTest {
	
	@Override
	public void initializeIndexes(XModel oldModel, XModel newModel, XValueIndexer oldIndexer,
	        XValueIndexer newIndexer) {
		this.oldIndex = new XModelObjectLevelIndex(oldModel, oldIndexer);
		this.newIndex = new XModelObjectLevelIndex(newModel, newIndexer);
	}
	
	@Override
	public void initializeIndexers() {
		this.oldIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
	}
}

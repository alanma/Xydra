package org.xydra.valueindex;

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
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer);
	}
	
	@Override
	public void initializeIndexers() {
		this.oldIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
	}
}

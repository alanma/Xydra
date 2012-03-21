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
	public void initializeIndexers() {
		this.oldIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		
		this.oldExcludeAllIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
		this.newExcludeAllIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());
	}
}

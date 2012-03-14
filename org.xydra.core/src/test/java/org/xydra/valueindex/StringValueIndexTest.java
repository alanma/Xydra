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
	private HashSet<XID> emptySet = new HashSet<XID>();
	
	@Override
	public void initializeIndexes() {
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer, true,
		        this.emptySet, this.emptySet);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer, true,
		        this.emptySet, this.emptySet);
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

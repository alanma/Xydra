package org.xydra.valueindex;

import java.util.HashSet;

import org.xydra.base.XID;


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
	private HashSet<XID> emptySet = new HashSet<XID>();
	
	@Override
	public void initializeIndexes() {
		this.oldIndex = new XFieldLevelIndex(this.oldModel, this.oldIndexer, true,
		        this.emptySet, this.emptySet);
		this.newIndex = new XFieldLevelIndex(this.newModel, this.newIndexer, true,
		        this.emptySet, this.emptySet);
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

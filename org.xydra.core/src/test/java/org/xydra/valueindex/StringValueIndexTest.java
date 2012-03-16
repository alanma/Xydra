package org.xydra.valueindex;

import java.util.HashSet;

import org.xydra.base.XID;
import org.xydra.base.XX;


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
		this.excludedIds = new HashSet<XID>();
		for(int i = 0; i < 13; i++) {
			this.excludedIds.add(XX.createUniqueId());
		}
		
		HashSet<XID> emptySet = new HashSet<XID>();
		
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer, true, emptySet,
		        this.excludedIds);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer, true, emptySet,
		        this.excludedIds);
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

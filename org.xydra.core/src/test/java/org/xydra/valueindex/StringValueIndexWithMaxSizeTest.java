package org.xydra.valueindex;



/**
 * Implementation of {@link XFieldLevelIndexTest} for testing
 * {@link StringValueIndex} with the "maxSize" variable set to 1 (= no value
 * will be stored, only the field address).
 *
 * @author kaidel
 *
 */

/*
 * TODO Maybe choose a value which is not as restrictive?
 */

public class StringValueIndexWithMaxSizeTest extends XFieldLevelIndexTest {

	@Override
	public void initializeIndexers() {
		final StringMap oldMap = new MemoryStringMap();
		final StringMap newMap = new MemoryStringMap();

		final StringValueIndex oldIndex = new StringValueIndex(oldMap, 1);
		final StringValueIndex newIndex = new StringValueIndex(newMap, 1);

		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);

		final StringMap oldExcludeAllMap = new MemoryStringMap();
		final StringMap newExcludeAllMap = new MemoryStringMap();

		final StringValueIndex oldExcludeAllIndex = new StringValueIndex(oldExcludeAllMap, 1);
		final StringValueIndex newExcludeAllIndex = new StringValueIndex(newExcludeAllMap, 1);

		this.oldExcludeAllIndexer = new SimpleValueIndexer(oldExcludeAllIndex);
		this.newExcludeAllIndexer = new SimpleValueIndexer(newExcludeAllIndex);
	}
}

package org.xydra.valueindex;



/**
 * Implementation of {@link XFieldLevelIndexTest} for testing
 * {@link StringValueIndex}.
 *
 * @author kaidel
 *
 */

public class StringValueIndexTest extends XFieldLevelIndexTest {

	@Override
	public void initializeIndexers() {
		final StringMap oldMap = new MemoryStringMap();
		final StringMap newMap = new MemoryStringMap();

		final StringValueIndex oldIndex = new StringValueIndex(oldMap);
		final StringValueIndex newIndex = new StringValueIndex(newMap);

		this.oldIndexer = new SimpleValueIndexer(oldIndex);
		this.newIndexer = new SimpleValueIndexer(newIndex);

		final StringMap oldExcludeAllMap = new MemoryStringMap();
		final StringMap newExcludeAllMap = new MemoryStringMap();

		final StringValueIndex oldExcludeAllIndex = new StringValueIndex(oldExcludeAllMap);
		final StringValueIndex newExcludeAllIndex = new StringValueIndex(newExcludeAllMap);

		this.oldExcludeAllIndexer = new SimpleValueIndexer(oldExcludeAllIndex);
		this.newExcludeAllIndexer = new SimpleValueIndexer(newExcludeAllIndex);
	}
}

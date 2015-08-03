package org.xydra.valueindex;

import java.util.Set;

import org.xydra.base.XId;
import org.xydra.core.model.XModel;


/**
 * A factory for creating various instances of {@link XFieldLevelIndex}.
 *
 * @author kaidel
 */

public class XFieldLevelIndexFactory {

	/**
	 * Creates an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 * {@link MemoryStringMap}.
	 *
	 * @param model The {@link XModel} which is to be indexed.
	 * @return an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 *         {@link MemoryStringMap}.
	 */
	public XFieldLevelIndex createIndexWithMemoryStringMap(final XModel model) {
		return createIndexWithMemoryStringMap(model, true, null, null);
	}

	/**
	 * Creates an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 * {@link MemoryStringMap}, according to the given parameters.
	 *
	 * @param model The {@link XModel} which is to be indexed.
	 * @param defaultIncludeAll determines whether the created index indexes
	 *            every field on default or does not index every field on
	 *            default.
	 * @param specialFieldIds a set of {@link XId XIds}, determining which
	 *            fields will be indexed, depending on the value of
	 *            defaultIncludeAll. If defaultIncludeAll is set to true, this
	 *            set will determine which fields will not be indexed (i.e.
	 *            fields with an Id in this set will not be indexed). If
	 *            defaultIncludeAll is set to false, this set will determine
	 *            which fields will be indexed (i.e. fields with an Id in this
	 *            set will be indexed).
	 * @return an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 *         {@link MemoryStringMap}, according to the given parameters.
	 */
	public XFieldLevelIndex createIndexWithMemoryStringMap(final XModel model, final boolean defaultIncludeAll,
	        final Set<XId> specialFieldIds) {
		if(defaultIncludeAll) {
			return createIndexWithMemoryStringMap(model, true, null, specialFieldIds);
		} else {
			return createIndexWithMemoryStringMap(model, false, specialFieldIds, null);
		}
	}

	private static XFieldLevelIndex createIndexWithMemoryStringMap(final XModel model,
	        final boolean defaultIncludeAll, final Set<XId> includedFieldIds, final Set<XId> excludedFieldIds) {
		final StringMap map = new MemoryStringMap();
		final StringValueIndex valueIndex = new StringValueIndex(map);
		final SimpleValueIndexer valueIndexer = new SimpleValueIndexer(valueIndex);

		// oldModel, oldIndexer, newModel, newIndexer, excludedIds and
		// includedIds need to be set before calling all this!
		final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, valueIndexer, defaultIncludeAll,
		        includedFieldIds, excludedFieldIds);

		return fieldIndex;
	}

	/**
	 * Creates an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 * {@link MemoryMapSetIndex}.
	 *
	 * @param model The {@link XModel} which is to be indexed.
	 * @return an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 *         {@link MemoryMapSetIndex}.
	 */
	public XFieldLevelIndex createIndexWithMemoryMapSetIndex(final XModel model) {
		return createIndexWithMemoryMapSetIndex(model, true, null, null);
	}

	/**
	 * Creates an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 * {@link MemoryMapSetIndex}, according to the given parameters.
	 *
	 * @param model The {@link XModel} which is to be indexed.
	 * @param defaultIncludeAll determines whether the created index indexes
	 *            every field on default or does not index every field on
	 *            default.
	 * @param specialFieldIds a set of {@link XId XIds}, determining which
	 *            fields will be indexed, depending on the value of
	 *            defaultIncludeAll. If defaultIncludeAll is set to true, this
	 *            set will determine which fields will not be indexed (i.e.
	 *            fields with an Id in this set will not be indexed). If
	 *            defaultIncludeAll is set to false, this set will determine
	 *            which fields will be indexed (i.e. fields with an Id in this
	 *            set will be indexed).
	 * @return an {@link XFieldLevelIndex} for the given {@link XModel} using a
	 *         {@link MemoryMapSetIndex}, according to the given parameters.
	 */
	public XFieldLevelIndex createIndexWithMemoryMapSetIndex(final XModel model,
	        final boolean defaultIncludeAll, final Set<XId> specialFieldIds) {
		if(defaultIncludeAll) {
			return createIndexWithMemoryMapSetIndex(model, true, null, specialFieldIds);
		} else {
			return createIndexWithMemoryMapSetIndex(model, false, specialFieldIds, null);
		}
	}

	private static XFieldLevelIndex createIndexWithMemoryMapSetIndex(final XModel model,
	        final boolean defaultIncludeAll, final Set<XId> includedFieldIds, final Set<XId> excludedFieldIds) {
		final SimpleValueIndexer valueIndexer = new SimpleValueIndexer(new MemoryMapSetIndex());

		// oldModel, oldIndexer, newModel, newIndexer, excludedIds and
		// includedIds need to be set before calling all this!
		final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, valueIndexer, defaultIncludeAll,
		        includedFieldIds, excludedFieldIds);

		return fieldIndex;
	}
}

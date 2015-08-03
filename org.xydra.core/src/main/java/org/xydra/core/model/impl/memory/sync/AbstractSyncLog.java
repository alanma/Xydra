package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.base.change.XEvent;
import org.xydra.index.iterator.AbstractTransformingIterator;


/**
 * Implementation of {@link ISyncLog} methods that don't vary between
 * implementations.
 *
 * @author kahmann
 */
abstract public class AbstractSyncLog implements ISyncLog {

	private static final long serialVersionUID = -1916889722140082523L;

	class ExtractEventTransformer extends AbstractTransformingIterator<ISyncLogEntry,XEvent> {

		public ExtractEventTransformer(final Iterator<? extends ISyncLogEntry> base) {
			super(base);
		}

		@Override
		public XEvent transform(final ISyncLogEntry in) {
			if(in == null) {
				return null;
			}
			return in.getEvent();
		}

	}

	@Override
	public Iterator<XEvent> getEventsBetween(final long beginRevision, final long endRevision) {
		return new ExtractEventTransformer(getSyncLogEntriesBetween(beginRevision, endRevision));
	}

	@Override
	public Iterator<XEvent> getEventsSince(final long revisionNumber) {
		return new ExtractEventTransformer(getSyncLogEntriesSince(revisionNumber));
	}

	@Override
	public Iterator<XEvent> getEventsUntil(final long revisionNumber) {
		return new ExtractEventTransformer(getSyncLogEntriesUntil(revisionNumber));
	}

}

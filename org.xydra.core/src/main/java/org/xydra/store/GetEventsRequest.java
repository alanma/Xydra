package org.xydra.store;

import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * Helper class for batch operations to group an {@link XAddress}, a
 * beginRevision, and an endRevision.
 * 
 * This is a request to fetch all {@link XEvent XEvents} that occurred after
 * (and including) beginRevision and before (but not including) endRevision.
 * 
 * @author voelkel
 */
public class GetEventsRequest {
	
	/**
	 * @param addresses of {@link XModel} (repositoryId/modelId/-/-),
	 *            {@link XObject} (repositoryId/modelId/objectId/-), or
	 *            {@link XField} (repositoryId/modelId/objectId/fieldId) for
	 *            which to return change events. This address must not refer to
	 *            a repository.
	 * 
	 *            If the given address refers to a model, all events for
	 *            contained objects and fields are returned as well. If the
	 *            address refers to an object, the events for all contained
	 *            fields are returned as well. Events for creating and removing
	 *            the entity specified by the XAddress are also included.
	 * 
	 *            For objects and fields, the resulting events include all
	 *            {@link XTransaction transactions} that contain changes to the
	 *            given object or field. It is the responsibility of the client
	 *            to extract the relevant event(s) from within the transaction.
	 * 
	 * @param beginRevision the beginning revision number (inclusive) of the
	 *            interval from which all {@link XEvent XEvents} are to be
	 *            returned - can be zero to get all {@link XEvent XEvents} up to
	 *            endRevision.
	 * 
	 * @param endRevision the end revision number (inclusive) of the interval
	 *            from which all {@link XEvent XEvents} are to be returned - can
	 *            be greater than {@link #getCurrentRevisionNumber()} to get all
	 *            {@link XEvent XEvents} since beginRevision.
	 */
	public GetEventsRequest(XAddress address, long beginRevision, long endRevision) {
		super();
		this.address = address;
		this.beginRevision = beginRevision;
		this.endRevision = endRevision;
	}
	
	public final XAddress address;
	public final long beginRevision;
	public final long endRevision;
	
}

package org.xydra.store.impl.gae;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;

/**
 * Xydra allows transaction only within one model. The GAE implementation
 * maintains one change log per model. This class keeps all access to a model
 * within the datastore and memcache in one place.
 * 
 * If the datastore or memcache is called to read or write a certain model, that
 * access is triggered only from here.
 * 
 * FIXME TODO define clearly how tentatives are handled
 * 
 * @author xamde
 */
public interface IGaeModelPersistence {

	/**
	 * See {@link XydraPersistence#executeCommand(XId, XCommand)}
	 * 
	 * @param command
	 * @param actorId
	 * @return ..
	 */
	public long executeCommand(XCommand command, XId actorId);

	/**
	 * See {@link XydraPersistence#getEvents(XAddress, long, long)}
	 * 
	 * @param address
	 *            of model, object or field
	 * @param beginRevision
	 * @param endRevision
	 * @return ..
	 */
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision);

	/**
	 * See
	 * {@link XydraPersistence#getModelSnapshot(org.xydra.persistence.GetWithAddressRequest)}
	 * 
	 * @param includeTentative
	 * @return ..
	 */
	public XWritableModel getSnapshot(boolean includeTentative);

	/**
	 * See
	 * {@link XydraPersistence#getObjectSnapshot(org.xydra.persistence.GetWithAddressRequest)}
	 * 
	 * @param objectId
	 * @param includeTentative
	 * @return ..
	 */
	public XWritableObject getObjectSnapshot(XId objectId, boolean includeTentative);

	/**
	 * See
	 * {@link XydraPersistence#getModelRevision(org.xydra.persistence.GetWithAddressRequest)}
	 * 
	 * @param includeTentative
	 *            if true, then in addition to the stable model revision number
	 *            also the unstable tentative revision number is calculated.
	 * @return the current {@link ModelRevision} or null
	 */
	public ModelRevision getModelRevision(boolean includeTentative);

	/**
	 * See {@link XydraPersistence#hasManagedModel(XId)}
	 * 
	 * @return ..
	 */
	public boolean modelHasBeenManaged();

}

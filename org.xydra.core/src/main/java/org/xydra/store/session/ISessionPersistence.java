package org.xydra.store.session;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;


public interface ISessionPersistence {
	
	/**
	 * @param modelId
	 * @return {@link XCommand#NOCHANGE} if nothing changes,
	 *         {@link XCommand#FAILED} if failed or a positive number, if
	 *         successful. The implementation <em>may</em> return the resulting
	 *         event number of the underlying {@link XydraPersistence}, if one
	 *         is used here.
	 */
	long createModel(XId modelId);
	
	/**
	 * @param modelId
	 * @return {@link XCommand#NOCHANGE} if nothing changes,
	 *         {@link XCommand#FAILED} if failed or a positive number, if
	 *         successful. The implementation <em>may</em> return the resulting
	 *         event number of the underlying {@link XydraPersistence}, if one
	 *         is used here.
	 */
	long removeModel(XId modelId);
	
	/**
	 * Commits the changes encoded in the {@link SessionCachedModel} to the
	 * back-end. Empty models are not created. Non-empty models are created if
	 * they do not exist yet.
	 * 
	 * @param changedModel
	 * @param actorId
	 * @return {@link XCommand#NOCHANGE} if nothing changes,
	 *         {@link XCommand#FAILED} if failed or a positive number, if
	 *         successful. The implementation <em>may</em> return the resulting
	 *         event number of the underlying {@link XydraPersistence}, if one
	 *         is used here.
	 * @throws SessionException if model did not exist in back-end and could not
	 *             be created
	 */
	long applyChangesAsTxn(SessionCachedModel changedModel, XId actorId) throws SessionException;
	
	XId getRepositoryId();
	
	XReadableModel getModelSnapshot(GetWithAddressRequest modelRequest);
	
	XReadableObject getObjectSnapshot(GetWithAddressRequest objectAddressRequest);
	
	/**
	 * Great for tests.
	 */
	void deleteAllData();
}

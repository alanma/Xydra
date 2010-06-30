package org.xydra.client;

import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * An interface for interacting with the "/data" API on remote CXM servers.
 */
public interface XDataService {
	
	/**
	 * Get the model with the given ID.
	 * 
	 * @param callback Callback to receive the XModel, may not be null.
	 */
	void getModel(XID modelId, Callback<XModel> callback);
	
	/**
	 * Get the object with the given ID.
	 * 
	 * @param callback Callback to receive the XObject, may not be null.
	 */
	void getObject(XID modelId, XID objectId, Callback<XObject> callback);
	
	/**
	 * Get the field with the given ID.
	 * 
	 * @param callback Callback to receive the XField, may not be null.
	 */
	void getField(XID modelId, XID objectId, XID fieldId, Callback<XField> callback);
	
	/**
	 * Set the remote state of the model with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote model's
	 *            state was equal to the passed one.
	 */
	void setModel(XModel model, Callback<Boolean> callback);
	
	/**
	 * Set the remote state of the object with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote
	 *            object's state was equal to the passed one.
	 */
	void setObject(XID modelId, XObject object, Callback<Boolean> callback);
	
	/**
	 * Set the remote state of the field with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote field's
	 *            state was equal to the passed one.
	 */
	void setField(XID modelId, XID objectId, XField field, Callback<Boolean> callback);
	
	/**
	 * Remove the model with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteModel(XID modelId, Callback<Void> callback);
	
	/**
	 * Remove the object with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteObject(XID modelId, XID objectId, Callback<Void> callback);
	
	/**
	 * Remove the field with the given ID.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback);
	
}

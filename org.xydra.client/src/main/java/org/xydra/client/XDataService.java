package org.xydra.client;

import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XAccessException;


/**
 * An interface for interacting with the "/data" API on remote CXM servers.
 * 
 * Errors while executing the operation (except those caused by illegal
 * arguments) are passed to the callback's {@link Callback#onFailure(Throwable)}
 * method. With the exception of {@link XAccessException}, foreseeable modes of
 * failure should be mapped to a subclass of {@link ServiceException}.
 * 
 * Any unauthorized operation will result in a {@link XAccessException} being
 * passed to the callback's {@link Callback#onFailure(Throwable)}.
 * 
 * The callback may or may not be called before the method returns.
 * 
 * TODO merge this with {@link XChangesService} service?
 * 
 * TODO The server should implement this interface, too (and interface belongs
 * to server project)
 */
public interface XDataService {
	
	/**
	 * Get a snapshot of the model with the given ID.
	 * 
	 * The model that is passed to the callback's
	 * {@link Callback#onSuccess(Object)} method is a local copy and changing it
	 * will not affect the repository on the server.
	 * 
	 * If the {@link XModel} doesn't exist on the server, the call will fail and
	 * pass as {@link NotFoundException} to the callbacks
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * @param callback Callback to receive the XModel, may not be null.
	 */
	void getModel(XID modelId, Callback<XModel> callback);
	
	/**
	 * Get a snapshot of the object with the given ID.
	 * 
	 * The {@link XObject} that is passed to the callback's
	 * {@link Callback#onSuccess(Object)} method is a local copy and changing it
	 * will not affect the repository on the server.
	 * 
	 * If the model or object doesn't exist on the server, the call will fail
	 * and pass as {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * @param callback Callback to receive the XObject, may not be null.
	 */
	void getObject(XID modelId, XID objectId, Callback<XObject> callback);
	
	/**
	 * Get a snapshot of the field with the given ID.
	 * 
	 * The {@link XField} that is passed to the callback's
	 * {@link Callback#onSuccess(Object)} method is a local copy and changing it
	 * will not affect the repository on the server.
	 * 
	 * If the model, object or field doesn't exist on the server, the call will
	 * fail and pass as {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * @param callback Callback to receive the XField, may not be null.
	 */
	void getField(XID modelId, XID objectId, XID fieldId, Callback<XField> callback);
	
	/**
	 * Set the remote state of the model with the given ID.
	 * 
	 * If the model doesn't exist already it will be automatically created.
	 * 
	 * Changes are applied in a transaction.
	 * 
	 * FIXME cannot create model in a transaction
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote model's
	 *            state was equal to the passed one.
	 */
	void setModel(XBaseModel model, Callback<Boolean> callback);
	
	/**
	 * Set the remote state of the object with the given ID.
	 * 
	 * If the model doesn't exist on the server, the call will fail and pass as
	 * {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * If the object doesn't exist already it will be automatically created.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote
	 *            object's state was equal to the passed one.
	 */
	void setObject(XID modelId, XBaseObject object, Callback<Boolean> callback);
	
	/**
	 * Set the remote state of the field with the given ID.
	 * 
	 * If the model or object doesn't exist on the server, the call will fail
	 * and pass as {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * If the field doesn't exist already it will be automatically created.
	 * 
	 * TODO What is the expected behaviour if the remote model/object does not
	 * exist yet? Create it silently or fail?
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null. The callback's onSuccess() method's parameter signifies
	 *            if the action changed anything (true) or if the remote field's
	 *            state was equal to the passed one.
	 */
	void setField(XID modelId, XID objectId, XBaseField field, Callback<Boolean> callback);
	
	/**
	 * Remove the model with the given ID.
	 * 
	 * Fails silently if the model doesn't exist. Use
	 * {@link XChangesService#executeCommand()} with a non-forced command to
	 * detect this.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteModel(XID modelId, Callback<Void> callback);
	
	/**
	 * Remove the object with the given ID.
	 * 
	 * Fails silently if the object doesn't exist. Use
	 * {@link XChangesService#executeCommand()} with a non-forced command to
	 * detect this.
	 * 
	 * If the model doesn't exist on the server, the call will fail and pass as
	 * {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteObject(XID modelId, XID objectId, Callback<Void> callback);
	
	/**
	 * Remove the field with the given ID.
	 * 
	 * Fails silently if the model doesn't exist. Use
	 * {@link XChangesService#executeCommand()} with a non-forced command to
	 * detect this.
	 * 
	 * If the model or object doesn't exist on the server, the call will fail
	 * and pass as {@link NotFoundException} to the callback's
	 * {@link Callback#onFailure(Throwable)} method.
	 * 
	 * @param callback Callback to receive if the operation succeeded, may be
	 *            null.
	 */
	void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback);
	
}

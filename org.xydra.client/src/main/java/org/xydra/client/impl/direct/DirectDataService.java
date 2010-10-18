package org.xydra.client.impl.direct;

import org.xydra.client.Callback;
import org.xydra.client.XDataService;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


/**
 * Instead of makign any HTTP/REST request, this implementation directly
 * executes the request on a known Xydra {@link XRepository}.
 * 
 * @author voelkel
 */
public class DirectDataService implements XDataService {
	
	private static final XID ACTOR = X.getIDProvider().fromString(
	        DirectDataService.class.toString());
	
	private XRepository repo;
	
	public DirectDataService(XRepository repository) {
		this.repo = repository;
	}
	
	@Override
	public void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback) {
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			callback.onFailure(new IllegalArgumentException("There is no model with id '" + modelId
			        + "'"));
			return;
		}
		XObject xo = model.getObject(objectId);
		if(xo == null) {
			callback.onFailure(new IllegalArgumentException("There is no object with id '"
			        + objectId + "'"));
			return;
		}
		boolean success = xo.removeField(ACTOR, objectId);
		if(success) {
			callback.onSuccess(null);
			return;
		} else {
			callback.onFailure(new IllegalStateException("There was no field with id '" + fieldId
			        + "'"));
			return;
		}
	}
	
	@Override
	public void deleteModel(XID modelId, Callback<Void> callback) {
		boolean success = this.repo.removeModel(ACTOR, modelId);
		if(success) {
			callback.onSuccess(null);
			return;
		} else {
			callback.onFailure(new IllegalStateException("There was no model with id '" + modelId
			        + "'"));
			return;
		}
	}
	
	@Override
	public void deleteObject(XID modelId, XID objectId, Callback<Void> callback) {
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			callback.onFailure(new IllegalArgumentException("There is no model with id '" + modelId
			        + "'"));
			return;
		}
		boolean success = model.removeObject(ACTOR, objectId);
		if(success) {
			callback.onSuccess(null);
			return;
		} else {
			callback.onFailure(new IllegalStateException("There was no object with id '" + objectId
			        + "'"));
			return;
		}
	}
	
	@Override
	public void getField(XID modelId, XID objectId, XID fieldId, Callback<XField> callback) {
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			callback.onFailure(new IllegalArgumentException("There is no model with id '" + modelId
			        + "'"));
			return;
		}
		XObject xo = model.getObject(objectId);
		if(xo == null) {
			callback.onFailure(new IllegalArgumentException("There is no object with id '"
			        + objectId + "'"));
			return;
		}
		XField xfield = xo.getField(fieldId);
		if(xfield == null) {
			callback.onFailure(new IllegalArgumentException("There is no object with id '"
			        + objectId + "'"));
			return;
		}
		callback.onSuccess(xfield);
	}
	
	@Override
	public void getModel(XID modelId, Callback<XModel> callback) {
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			callback.onFailure(new IllegalArgumentException("There is no model with id '" + modelId
			        + "'"));
			return;
		}
		callback.onSuccess(model);
	}
	
	@Override
	public void getObject(XID modelId, XID objectId, Callback<XObject> callback) {
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			callback.onFailure(new IllegalArgumentException("There is no model with id '" + modelId
			        + "'"));
			return;
		}
		XObject xo = model.getObject(objectId);
		if(xo == null) {
			callback.onFailure(new IllegalArgumentException("There is no object with id '"
			        + objectId + "'"));
			return;
		}
		callback.onSuccess(xo);
	}
	
	@Override
	public void setModel(XModel model, Callback<Boolean> callback) {
		XModel currentModel = this.repo.createModel(ACTOR, model.getID());
		boolean changes = copy(model, currentModel);
		callback.onSuccess(changes);
	}
	
	@Override
	public void setObject(XID modelId, XObject object, Callback<Boolean> callback) {
		// TODO silently create - is this the expected behaviour?
		XModel model = this.repo.createModel(ACTOR, modelId);
		XObject currentObject = model.createObject(ACTOR, object.getID());
		boolean changes = copy(object, currentObject);
		callback.onSuccess(changes);
	}
	
	@Override
	public void setField(XID modelId, XID objectId, XField field, Callback<Boolean> callback) {
		// TODO silently create - is this the expected behaviour?
		XModel model = this.repo.createModel(ACTOR, modelId);
		// TODO silently create - is this the expected behaviour?
		XObject object = model.createObject(ACTOR, objectId);
		
		XField currentField = object.createField(ACTOR, field.getID());
		boolean changes = copy(field, currentField);
		callback.onSuccess(changes);
		
	}
	
	public static boolean copy(XModel sourceModel, XModel targetModel) {
		boolean changes = false;
		for(XID objectId : sourceModel) {
			XObject sourceObject = sourceModel.getObject(objectId);
			if(!targetModel.hasObject(objectId)) {
				changes = true;
			}
			XObject targetObject = targetModel.createObject(ACTOR, objectId);
			changes |= copy(sourceObject, targetObject);
		}
		return changes;
	}
	
	public static boolean copy(XObject sourceObject, XObject targetObject) {
		boolean changes = false;
		for(XID fieldId : sourceObject) {
			XField sourceField = sourceObject.getField(fieldId);
			if(!targetObject.hasField(fieldId)) {
				changes = true;
			}
			XField targetField = targetObject.createField(ACTOR, fieldId);
			changes |= copy(sourceField, targetField);
		}
		return changes;
	}
	
	public static boolean copy(XField sourceField, XField targetField) {
		XValue value = sourceField.getValue();
		if(targetField.getValue() != null && targetField.getValue().equals(value)) {
			return false;
		} else {
			targetField.setValue(ACTOR, value);
			return true;
		}
	}
	
}

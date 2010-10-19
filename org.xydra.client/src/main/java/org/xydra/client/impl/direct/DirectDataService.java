package org.xydra.client.impl.direct;

import org.xydra.client.Callback;
import org.xydra.client.NotFoundException;
import org.xydra.client.XDataService;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryFieldState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;


/**
 * Instead of makign any HTTP/REST request, this implementation directly
 * executes the request on a known Xydra {@link XRepository}.
 * 
 * @author voelkel
 */
public class DirectDataService implements XDataService {
	
	private XProtectedRepository repo;
	
	public DirectDataService(XProtectedRepository repository) {
		this.repo = repository;
	}
	
	@Override
	public void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback) {
		
		try {
			
			XProtectedModel model = this.repo.getModel(modelId);
			if(model == null) {
				XAddress modelAddr = XX.toAddress(null, modelId, null, null);
				callback.onFailure(new NotFoundException(modelAddr.toURI()));
				return;
			}
			
			XProtectedObject object = model.getObject(objectId);
			if(object == null) {
				XAddress objectAddr = XX.toAddress(null, modelId, objectId, null);
				callback.onFailure(new NotFoundException(objectAddr.toURI()));
				return;
			}
			
			object.removeField(fieldId);
			
			callback.onSuccess(null);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void deleteModel(XID modelId, Callback<Void> callback) {
		
		try {
			this.repo.removeModel(modelId);
			callback.onSuccess(null);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void deleteObject(XID modelId, XID objectId, Callback<Void> callback) {
		
		try {
			
			XProtectedModel model = this.repo.getModel(modelId);
			if(model == null) {
				XAddress modelAddr = XX.toAddress(null, modelId, null, null);
				callback.onFailure(new NotFoundException(modelAddr.toURI()));
				return;
			}
			
			model.removeObject(objectId);
			
			callback.onSuccess(null);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void getField(XID modelId, XID objectId, XID fieldId, Callback<XField> callback) {
		
		XAddress fieldAddr = XX.toAddress(null, modelId, objectId, fieldId);
		
		try {
			
			XProtectedModel model = this.repo.getModel(modelId);
			if(model == null) {
				XAddress modelAddr = XX.toAddress(null, modelId, null, null);
				callback.onFailure(new NotFoundException(modelAddr.toURI()));
				return;
			}
			
			XProtectedObject object = model.getObject(objectId);
			if(object == null) {
				XAddress objectAddr = XX.toAddress(null, modelId, objectId, null);
				callback.onFailure(new NotFoundException(objectAddr.toURI()));
				return;
			}
			
			XProtectedField field = object.getField(fieldId);
			if(field == null) {
				callback.onFailure(new NotFoundException(fieldAddr.toURI()));
				return;
			}
			
			XFieldState fieldState = new TemporaryFieldState(fieldAddr);
			
			XField fieldCopy = new MemoryField(fieldState);
			
			callback.onSuccess(fieldCopy);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void getModel(XID modelId, Callback<XModel> callback) {
		
		XAddress modelAddr = XX.toAddress(null, modelId, null, null);
		
		try {
			
			XProtectedModel model = this.repo.getModel(modelId);
			if(model == null) {
				callback.onFailure(new NotFoundException(modelAddr.toURI()));
				return;
			}
			
			XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr);
			XModelState modelState = new TemporaryModelState(modelAddr, changeLogState);
			
			XModel modelCopy = new MemoryModel(modelState);
			
			callback.onSuccess(modelCopy);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void getObject(XID modelId, XID objectId, Callback<XObject> callback) {
		
		XAddress objectAddr = XX.toAddress(null, modelId, objectId, null);
		
		try {
			
			XProtectedModel model = this.repo.getModel(modelId);
			if(model == null) {
				XAddress modelAddr = XX.toAddress(null, modelId, null, null);
				callback.onFailure(new NotFoundException(modelAddr.toURI()));
				return;
			}
			
			XProtectedObject object = model.getObject(objectId);
			if(object == null) {
				callback.onFailure(new NotFoundException(objectAddr.toURI()));
				return;
			}
			
			XChangeLogState changeLogState = new MemoryChangeLogState(objectAddr);
			XObjectState objectState = new TemporaryObjectState(objectAddr, changeLogState);
			
			XObject objectCopy = new MemoryObject(objectState);
			
			callback.onSuccess(objectCopy);
			
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
	}
	
	@Override
	public void setModel(XBaseModel model, Callback<Boolean> callback) {
		
		XProtectedModel oldModel = this.repo.createModel(model.getID());
		
		XTransactionBuilder tb = new XTransactionBuilder(oldModel.getAddress());
		tb.changeModel(oldModel, model);
		
		long result;
		try {
			result = oldModel.executeCommand(tb.buildCommand());
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
		
		callback.onSuccess(result != XCommand.FAILED && result != XCommand.NOCHANGE);
	}
	
	@Override
	public void setObject(XID modelId, XBaseObject object, Callback<Boolean> callback) {
		
		XAddress modelAddr = XX.resolveModel(this.repo.getAddress(), modelId);
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		tb.setObject(modelAddr, object);
		
		long result;
		try {
			result = this.repo.executeCommand(tb.buildCommand());
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
		
		if(result == XCommand.FAILED) {
			callback.onFailure(new NotFoundException(modelAddr.toURI()));
		} else {
			callback.onSuccess(result != XCommand.NOCHANGE);
		}
	}
	
	@Override
	public void setField(XID modelId, XID objectId, XBaseField field, Callback<Boolean> callback) {
		
		XAddress objectAddr = XX.resolveObject(this.repo.getAddress(), modelId, objectId);
		XTransactionBuilder tb = new XTransactionBuilder(objectAddr);
		tb.setField(objectAddr, field);
		
		long result;
		try {
			result = this.repo.executeCommand(tb.buildCommand());
		} catch(XAccessException ae) {
			callback.onFailure(ae);
			return;
		}
		
		if(result == XCommand.FAILED) {
			// cannot determine if the model existed or not
			// object definitely doesn't exist
			callback.onFailure(new NotFoundException(null));
		} else {
			callback.onSuccess(result != XCommand.NOCHANGE);
		}
	}
	
}

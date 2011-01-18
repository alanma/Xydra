package org.xydra.core;

import org.xydra.base.XReadableField;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XReadableRepository;
import org.xydra.base.XID;
import org.xydra.base.XWritableField;
import org.xydra.base.XWritableModel;
import org.xydra.base.XWritableObject;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.core.model.XModel;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;
import org.xydra.core.model.state.impl.memory.XStateUtils;
import org.xydra.store.base.SimpleModel;
import org.xydra.store.base.SimpleObject;


/**
 * A helper class containing methods for copying different kinds of Xydra
 * entities.
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class XCopyUtils {
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @param targetModel The {@link XHalfWritableModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyData(XReadableModel sourceModel, XHalfWritableModel targetModel) {
		// copy model to _model
		for(XID objectId : sourceModel) {
			XReadableObject sourceObject = sourceModel.getObject(objectId);
			XHalfWritableObject targetObject = targetModel.createObject(objectId);
			XCopyUtils.copyData(sourceObject, targetObject);
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceRepository The {@link XReadableRepository} which is to be copied
	 * @param targetRepository The {@link XHalfWritableRepository} in which the data
	 *            of sourceRepository is to be pasted.
	 */
	public static void copyData(XReadableRepository sourceRepository,
	        XHalfWritableRepository targetRepository) {
		// copy repository to _repository
		for(XID modelId : sourceRepository) {
			XReadableModel sourceModel = sourceRepository.getModel(modelId);
			XHalfWritableModel targetModel = targetRepository.createModel(modelId);
			copyData(sourceModel, targetModel);
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XReadableObject} which is to be copied
	 * @param targetObject The {@link XHalfWritableObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyData(XReadableObject sourceObject, XHalfWritableObject targetObject) {
		for(XID fieldId : sourceObject) {
			XReadableField sourceField = sourceObject.getField(fieldId);
			XHalfWritableField targetField = targetObject.createField(fieldId);
			targetField.setValue(sourceField.getValue());
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XReadableObject} which is to be copied
	 * @param targetObject The {@link SimpleObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyDataAndRevisions(XReadableObject sourceObject, XWritableObject targetObject) {
		targetObject.setRevisionNumber(sourceObject.getRevisionNumber());
		for(XID fieldId : sourceObject) {
			XReadableField sourceField = sourceObject.getField(fieldId);
			XWritableField targetField = targetObject.createField(fieldId);
			targetField.setRevisionNumber(sourceField.getRevisionNumber());
			targetField.setValue(sourceField.getValue());
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @param targetModel The {@link SimpleModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyDataAndRevisions(XReadableModel sourceModel, XWritableModel targetModel) {
		targetModel.setRevisionNumber(sourceModel.getRevisionNumber());
		for(XID objectId : sourceModel) {
			XReadableObject object = sourceModel.getObject(objectId);
			XWritableObject localObject = targetModel.createObject(object.getID());
			XCopyUtils.copyDataAndRevisions(object, localObject);
		}
	}
	
	/**
	 * Copy all state information from sourceModel.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @return the snapshot or null
	 */
	public static XWritableModel createSnapshot(XReadableModel sourceModel) {
		if(sourceModel == null) {
			return null;
		}
		XWritableModel targetModel = new SimpleModel(sourceModel.getAddress());
		copyDataAndRevisions(sourceModel, targetModel);
		return targetModel;
	}
	
	/**
	 * Copy all state information from sourceObject.
	 * 
	 * @param sourceModel The {@link XReadableObject} which is to be copied
	 */
	public static XWritableObject createSnapshot(XReadableObject sourceObject) {
		if(sourceObject == null) {
			return null;
		}
		XWritableObject targetObject = new SimpleObject(sourceObject.getAddress());
		copyDataAndRevisions(sourceObject, targetObject);
		return targetObject;
	}
	
	public static XModel copyModel(XID actor, String password, XReadableModel modelSnapshot) {
		XChangeLogState changeLogState = new MemoryChangeLogState(modelSnapshot.getAddress());
		changeLogState.setFirstRevisionNumber(modelSnapshot.getRevisionNumber() + 1);
		XModelState modelState = new TemporaryModelState(modelSnapshot.getAddress(), changeLogState);
		XStateUtils.copy(modelSnapshot, modelState);
		XModel model = new MemoryModel(actor, password, modelState);
		return model;
	}
	
}

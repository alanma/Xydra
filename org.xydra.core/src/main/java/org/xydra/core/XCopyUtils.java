package org.xydra.core;

import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.model.XWritableRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;
import org.xydra.core.model.state.impl.memory.XStateUtils;
import org.xydra.store.base.SimpleField;
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
	 * @param sourceModel The {@link XBaseModel} which is to be copied
	 * @param targetModel The {@link XWritableModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyData(XBaseModel sourceModel, XWritableModel targetModel) {
		// copy model to _model
		for(XID objectId : sourceModel) {
			XBaseObject sourceObject = sourceModel.getObject(objectId);
			XWritableObject targetObject = targetModel.createObject(objectId);
			XCopyUtils.copyData(sourceObject, targetObject);
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceRepository The {@link XBaseRepository} which is to be copied
	 * @param targetRepository The {@link XWritableRepository} in which the data
	 *            of sourceRepository is to be pasted.
	 */
	public static void copyData(XBaseRepository sourceRepository,
	        XWritableRepository targetRepository) {
		// copy repository to _repository
		for(XID modelId : sourceRepository) {
			XBaseModel sourceModel = sourceRepository.getModel(modelId);
			XWritableModel targetModel = targetRepository.createModel(modelId);
			copyData(sourceModel, targetModel);
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XBaseObject} which is to be copied
	 * @param targetObject The {@link XWritableObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyData(XBaseObject sourceObject, XWritableObject targetObject) {
		for(XID fieldId : sourceObject) {
			XBaseField sourceField = sourceObject.getField(fieldId);
			XWritableField targetField = targetObject.createField(fieldId);
			targetField.setValue(sourceField.getValue());
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XBaseObject} which is to be copied
	 * @param targetObject The {@link SimpleObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyDataAndRevisions(XBaseObject sourceObject, SimpleObject targetObject) {
		targetObject.setRevisionNumber(sourceObject.getRevisionNumber());
		for(XID fieldId : sourceObject) {
			XBaseField sourceField = sourceObject.getField(fieldId);
			SimpleField targetField = targetObject.createField(fieldId);
			targetField.setRevisionNumber(sourceField.getRevisionNumber());
			targetField.setValue(sourceField.getValue());
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceModel The {@link XBaseModel} which is to be copied
	 * @param targetModel The {@link SimpleModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyDataAndRevisions(XBaseModel sourceModel, SimpleModel targetModel) {
		targetModel.setRevisionNumber(sourceModel.getRevisionNumber());
		for(XID objectId : sourceModel) {
			XBaseObject object = sourceModel.getObject(objectId);
			SimpleObject localObject = targetModel.createObject(object.getID());
			XCopyUtils.copyDataAndRevisions(object, localObject);
		}
	}
	
	/**
	 * Copy all state information from sourceModel.
	 * 
	 * @param sourceModel The {@link XBaseModel} which is to be copied
	 */
	public static SimpleModel createSnapshot(XBaseModel sourceModel) {
		if(sourceModel == null) {
			return null;
		}
		SimpleModel targetModel = new SimpleModel(sourceModel.getAddress());
		copyDataAndRevisions(sourceModel, targetModel);
		return targetModel;
	}
	
	/**
	 * Copy all state information from sourceObject.
	 * 
	 * @param sourceModel The {@link XBaseObject} which is to be copied
	 */
	public static SimpleObject createSnapshot(XBaseObject sourceObject) {
		if(sourceObject == null) {
			return null;
		}
		SimpleObject targetObject = new SimpleObject(sourceObject.getAddress());
		copyDataAndRevisions(sourceObject, targetObject);
		return targetObject;
	}
	
	public static XModel copyModel(XID actor, String password, XBaseModel modelSnapshot) {
		XChangeLogState changeLogState = new MemoryChangeLogState(modelSnapshot.getAddress());
		changeLogState.setFirstRevisionNumber(modelSnapshot.getRevisionNumber() + 1);
		XModelState modelState = new TemporaryModelState(modelSnapshot.getAddress(), changeLogState);
		XStateUtils.copy(modelSnapshot, modelState);
		XModel model = new MemoryModel(actor, password, modelState);
		return model;
	}
	
}

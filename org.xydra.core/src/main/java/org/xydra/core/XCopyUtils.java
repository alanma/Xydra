package org.xydra.core;


import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;

/**
 * A helper class containing methods for copying different kinds of Xydra entities.
 * 
 * @author Kaidel
 *
 */

public class XCopyUtils {

	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceModel The {@link XBaseModel} which is to be copied
	 * @param targetModel The {@link XModel} in which the data of sourceModel is
	 *            to be pasted.
	 */
	public static void copy(XID actorID, XBaseModel sourceModel, XModel targetModel) {
		// copy model to _model
		for(XID objectID : sourceModel) {
			XBaseObject object = sourceModel.getObject(objectID);
			XObject localObject = targetModel.createObject(actorID, object.getID());
			XCopyUtils.copy(actorID, object, localObject);
		}
	}

	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceRepository The {@link XBaseRepository} which is to be copied
	 * @param targetRepository The {@link XRepository} in which the data of
	 *            sourceRepository is to be pasted.
	 */
	public static void copy(XID actorID, XBaseRepository sourceRepository,
	        XRepository targetRepository) {
		// copy repository to _repository
		for(XID modelID : sourceRepository) {
			XBaseModel model = sourceRepository.getModel(modelID);
			XModel localModel = targetRepository.createModel(actorID, model.getID());
			copy(actorID, model, localModel);
		}
	}

	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceObject The {@link XBaseObject} which is to be copied
	 * @param targetObject The {@link XObject} in which the data of sourceObject
	 *            is to be pasted.
	 */
	public static void copy(XID actorID, XBaseObject sourceObject, XObject targetObject) {
		for(XID fieldID : sourceObject) {
			XBaseField field = sourceObject.getField(fieldID);
			XField localField = targetObject.createField(actorID, fieldID);
			localField.setValue(actorID, field.getValue());
		}
	}

}

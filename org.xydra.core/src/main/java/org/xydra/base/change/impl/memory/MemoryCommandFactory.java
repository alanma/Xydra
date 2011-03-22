package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * A factory class providing helpful methods for creating various types of
 * {@link XCommand XCommands}.
 * 
 * @author Kaidel
 * 
 */

public class MemoryCommandFactory implements XCommandFactory {
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldId The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID objectId, XID fieldId) {
		return createAddFieldCommand(null, null, objectId, fieldId, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldId The {@link XID} for the new {@link XField}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID objectId, XID fieldId, boolean isForced) {
		return createAddFieldCommand(null, null, objectId, fieldId, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectId The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldId The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID modelId, XID objectId, XID fieldId) {
		return createAddFieldCommand(null, modelId, objectId, fieldId, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectId The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldId The {@link XID} for the new {@link XField}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID modelId, XID objectId, XID fieldId,
	        boolean isForced) {
		return createAddFieldCommand(null, modelId, objectId, fieldId, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} holding
	 *            the the {@link XModel} which is holding the {@link XObject} to
	 *            which the {@link XField} is to be added.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectId The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldId The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId) {
		return createAddFieldCommand(repositoryId, modelId, objectId, fieldId, false);
	}
	
	public XObjectCommand createAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, objectId, null);
		
		return MemoryObjectCommand.createAddCommand(target, revNr, fieldId);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the add-type for adding a new
	 * {@link XModel} to an {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} to which
	 *            the new {@link XModel} is to be added.
	 * @param modelId The {@link XID} for the new {@link XModel}.
	 * @return A new {@link XRepositoryCommand} of the add-type.
	 */
	public XRepositoryCommand createAddModelCommand(XID repositoryId, XID modelId) {
		return createAddModelCommand(repositoryId, modelId, false);
	}
	
	public XRepositoryCommand createAddModelCommand(XID repositoryId, XID modelId, boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, null, null, null);
		
		return MemoryRepositoryCommand.createAddCommand(target, revNr, modelId);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID modelId, XID objectId) {
		return createAddObjectCommand(null, modelId, objectId, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID modelId, XID objectId, boolean isForced) {
		return createAddObjectCommand(null, modelId, objectId, isForced);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} to which the new {@link XObject} is to be
	 *            added.
	 * @param modelId The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID repositoryId, XID modelId, XID objectId) {
		return createAddObjectCommand(repositoryId, modelId, objectId, false);
	}
	
	public XModelCommand createAddObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, null, null);
		
		return MemoryModelCommand.createAddCommand(target, revNr, objectId);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID fieldId, long fieldRevision, XValue value) {
		return createAddValueCommand(null, null, null, fieldId, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID fieldId, long fieldRevision, XValue value,
	        boolean isForced) {
		return createAddValueCommand(null, null, null, fieldId, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID objectId, XID fieldId, long fieldRevision,
	        XValue value) {
		return createAddValueCommand(null, null, objectId, fieldId, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID objectId, XID fieldId, long fieldRevision,
	        XValue value, boolean isForced) {
		return createAddValueCommand(null, null, objectId, fieldId, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, XValue value) {
		return createAddValueCommand(null, modelId, objectId, fieldId, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, XValue value, boolean isForced) {
		return createAddValueCommand(null, modelId, objectId, fieldId, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * 
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value) {
		return createAddValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
		        value, false);
	}
	
	public XFieldCommand createAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, objectId, fieldId);
		
		return MemoryFieldCommand.createAddCommand(target, revNr, value);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	
	public XFieldCommand createChangeValueCommand(XID fieldId, long fieldRevision, XValue value) {
		return createChangeValueCommand(null, null, null, fieldId, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID fieldId, long fieldRevision, XValue value,
	        boolean isForced) {
		return createChangeValueCommand(null, null, null, fieldId, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID objectId, XID fieldId, long fieldRevision,
	        XValue value) {
		return createChangeValueCommand(null, null, objectId, fieldId, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID objectId, XID fieldId, long fieldRevision,
	        XValue value, boolean isForced) {
		return createChangeValueCommand(null, null, objectId, fieldId, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, XValue value) {
		return createChangeValueCommand(null, modelId, objectId, fieldId, fieldRevision, value,
		        false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, XValue value, boolean isForced) {
		return createChangeValueCommand(null, modelId, objectId, fieldId, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldId The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value) {
		return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
		        value, false);
	}
	
	// TODO change API: remove isForced and document you need to set revision to
	// XCommand.FORCED
	public XFieldCommand createChangeValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, objectId, fieldId);
		
		return MemoryFieldCommand.createChangeCommand(target, revNr, value);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID objectId, XID fieldId, long fieldRevision) {
		return createRemoveFieldCommand(null, null, objectId, fieldId, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID objectId, XID fieldId, long fieldRevision,
	        boolean isForced) {
		return createRemoveFieldCommand(null, null, objectId, fieldId, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param modelId the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectId The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision) {
		return createRemoveFieldCommand(null, modelId, objectId, fieldId, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param modelId the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectId The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, boolean isForced) {
		return createRemoveFieldCommand(null, modelId, objectId, fieldId, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param repositoryId the {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} holding the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param modelId the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectId The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision) {
		return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
		        false);
	}
	
	public XObjectCommand createRemoveFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced) {
		
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, objectId, null);
		
		return MemoryObjectCommand.createRemoveCommand(target, revNr, fieldId);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the remove-type for removing
	 * an {@link XModel} from an {@link XRepository}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} from which
	 *            the {@link XModel} is to be removed.
	 * @param modelId The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision the current revision number of the {@link XModel}
	 *            which is to be removed
	 * @return A new {@link XRepositoryCommand} of the remove-type.
	 */
	public XRepositoryCommand createRemoveModelCommand(XID repositoryId, XID modelId,
	        long modelRevision) {
		return createRemoveModelCommand(repositoryId, modelId, modelRevision, false);
	}
	
	public XRepositoryCommand createRemoveModelCommand(XID repositoryId, XID modelId,
	        long modelRevision, boolean isForced) {
		long revNr = modelRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, null, null, null);
		
		return MemoryRepositoryCommand.createRemoveCommand(target, revNr, modelId);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID modelId, XID objectId, long objectRevision) {
		return createRemoveObjectCommand(null, modelId, objectId, objectRevision, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID modelId, XID objectId, long objectRevision,
	        boolean isForced) {
		return createRemoveObjectCommand(null, modelId, objectId, objectRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} from which the {@link XObject} is to be
	 *            removed.
	 * @param modelId The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        long objectRevision) {
		return createRemoveObjectCommand(repositoryId, modelId, objectId, objectRevision, false);
	}
	
	public XModelCommand createRemoveObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        long objectRevision, boolean isForced) {
		long revNr = objectRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, null, null);
		
		return MemoryModelCommand.createRemoveCommand(target, revNr, objectId);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID fieldId, long fieldRevision) {
		return createRemoveValueCommand(null, null, null, fieldId, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID fieldId, long fieldRevision, boolean isForced) {
		return createRemoveValueCommand(null, null, null, fieldId, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID objectId, XID fieldId, long fieldRevision) {
		return createRemoveValueCommand(null, null, objectId, fieldId, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID objectId, XID fieldId, long fieldRevision,
	        boolean isForced) {
		return createRemoveValueCommand(null, null, objectId, fieldId, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision) {
		return createRemoveValueCommand(null, modelId, objectId, fieldId, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID modelId, XID objectId, XID fieldId,
	        long fieldRevision, boolean isForced) {
		return createRemoveValueCommand(null, modelId, objectId, fieldId, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param repositoryId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param modelId The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectId The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldId The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision) {
		return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
		        false);
	}
	
	public XFieldCommand createRemoveValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = XX.toAddress(repositoryId, modelId, objectId, fieldId);
		
		return MemoryFieldCommand.createRemoveCommand(target, revNr);
	}
	
	@Override
	public XObjectCommand createAddFieldCommand(XAddress objectAddress, XID fieldId,
	        boolean isForced) {
		return createAddFieldCommand(objectAddress.getRepository(), objectAddress.getModel(),
		        objectAddress.getObject(), fieldId, isForced);
	}
	
	@Override
	public XModelCommand createAddObjectCommand(XAddress modelAddress, XID objectId,
	        boolean isForced) {
		return createAddObjectCommand(modelAddress.getRepository(), modelAddress.getModel(),
		        objectId, isForced);
	}
	
	@Override
	public XFieldCommand createAddValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value, boolean isForced) {
		return createAddValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
		        fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
	}
	
	@Override
	public XFieldCommand createChangeValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value, boolean isForced) {
		return createChangeValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
		        fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
	}
	
	@Override
	public XObjectCommand createRemoveFieldCommand(XAddress fieldAddress, long fieldRevision,
	        boolean isForced) {
		return createRemoveFieldCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
		        fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
	}
	
	@Override
	public XRepositoryCommand createRemoveModelCommand(XAddress modelAddress, long modelRevision,
	        boolean isForced) {
		return createRemoveModelCommand(modelAddress.getRepository(), modelAddress.getModel(),
		        modelRevision, isForced);
	}
	
	@Override
	public XModelCommand createRemoveObjectCommand(XAddress objectAddress, long objectRevision,
	        boolean isForced) {
		return createRemoveObjectCommand(objectAddress.getRepository(), objectAddress.getModel(),
		        objectAddress.getObject(), objectRevision, isForced);
	}
	
	@Override
	public XFieldCommand createRemoveValueCommand(XAddress fieldAddress, long fieldRevision,
	        boolean isForced) {
		return createRemoveValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
		        fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
	}
	
}

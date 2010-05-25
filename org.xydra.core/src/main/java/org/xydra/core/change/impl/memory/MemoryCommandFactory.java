package org.xydra.core.change.impl.memory;

import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


/**
 * A factory class for creating various types of XCommands.
 * 
 * @author Kaidel
 * 
 */

public class MemoryCommandFactory implements XCommandFactory {
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID objectID, XID fieldID) {
		return createAddFieldCommand(null, null, objectID, fieldID, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID objectID, XID fieldID, boolean isForced) {
		return createAddFieldCommand(null, null, objectID, fieldID, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID modelID, XID objectID, XID fieldID) {
		return createAddFieldCommand(null, modelID, objectID, fieldID, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID modelID, XID objectID, XID fieldID,
	        boolean isForced) {
		return createAddFieldCommand(null, modelID, objectID, fieldID, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the the {@link XModel} which is holding the {@link XObject} to
	 *            which the {@link XField} is to be added.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID) {
		return createAddFieldCommand(repositoryID, modelID, objectID, fieldID, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type for adding a new
	 * {@link XField} to an {@link XObject}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the the {@link XModel} which is holding the {@link XObject} to
	 *            which the {@link XField} is to be added.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} to which the {@link XField} is too be added.
	 * @param objectID The {@link XID} of the {@link XObject} to which the
	 *            {@link XField} is to be added.
	 * @param fieldID The {@link XID} for the new {@link XField}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the add-type
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, modelID, objectID, null);
		
		return MemoryObjectCommand.createAddCommand(target, revNr, fieldID);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the add-type for adding a new
	 * {@link XModel} to an {@link XRepository}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} to which
	 *            the new {@link XModel} is to be added.
	 * @param modelID The {@link XID} for the new {@link XModel}.
	 * @return A new {@link XRepositoryCommand} of the add-type.
	 */
	public XRepositoryCommand createAddModelCommand(XID repositoryID, XID modelID) {
		return createAddModelCommand(repositoryID, modelID, false);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the add-type for adding a new
	 * {@link XModel} to an {@link XRepository}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} to which
	 *            the new {@link XModel} is to be added.
	 * @param modelID The {@link XID} for the new {@link XModel}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XRepositoryCommand} of the add-type.
	 */
	public XRepositoryCommand createAddModelCommand(XID repositoryID, XID modelID, boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, null, null, null);
		
		return MemoryRepositoryCommand.createAddCommand(target, revNr, modelID);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectID The {@link XID} for the new {@link XObject}.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID modelID, XID objectID) {
		return createAddObjectCommand(null, modelID, objectID, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectID The {@link XID} for the new {@link XObject}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID modelID, XID objectID, boolean isForced) {
		return createAddObjectCommand(null, modelID, objectID, isForced);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} to which the new {@link XObject} is to be
	 *            added.
	 * @param modelID The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectID The {@link XID} for the new {@link XObject}.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID repositoryID, XID modelID, XID objectID) {
		return createAddObjectCommand(repositoryID, modelID, objectID, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type for adding a new
	 * {@link XObject} to an {@link XModel}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} to which the new {@link XObject} is to be
	 *            added.
	 * @param modelID The {@link XID} of the {@link XModel} to which the new
	 *            {@link XObject} is to be added.
	 * @param objectID The {@link XID} for the new {@link XObject}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the add-type.
	 */
	public XModelCommand createAddObjectCommand(XID repositoryID, XID modelID, XID objectID,
	        boolean isForced) {
		long revNr = XCommand.SAFE;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, modelID, null, null);
		
		return MemoryModelCommand.createAddCommand(target, revNr, objectID);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID objectID, XID fieldID, long fieldRevision) {
		return createRemoveFieldCommand(null, null, objectID, fieldID, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID objectID, XID fieldID, long fieldRevision,
	        boolean isForced) {
		return createRemoveFieldCommand(null, null, objectID, fieldID, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param modelID the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision) {
		return createRemoveFieldCommand(null, modelID, objectID, fieldID, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param modelID the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, boolean isForced) {
		return createRemoveFieldCommand(null, modelID, objectID, fieldID, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param repositoryID the {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} holding the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param modelID the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision) {
		return createRemoveFieldCommand(repositoryID, modelID, objectID, fieldID, fieldRevision,
		        false);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type for removing an
	 * {@link XField} from an {@link XObject}.
	 * 
	 * @param repositoryID the {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} holding the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param modelID the {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} from which the {@link XField} is to be
	 *            removed.
	 * @param objectID The {@link XID} of the {@link XObject} from which the
	 *            {@link XField} is to be removed.
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return a new {@link XObjectCommand} of the remove-type
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, boolean isForced) {
		
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, modelID, objectID, null);
		
		return MemoryObjectCommand.createRemoveCommand(target, revNr, fieldID);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the remove-type for removing
	 * an {@link XModel} from an {@link XRepository}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} from which
	 *            the {@link XModel} is to be removed.
	 * @param modelID The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision the current revision number of the {@link XModel}
	 *            which is to be removed
	 * @return A new {@link XRepositoryCommand} of the remove-type.
	 */
	public XRepositoryCommand createRemoveModelCommand(XID repositoryID, XID modelID,
	        long modelRevision) {
		return createRemoveModelCommand(repositoryID, modelID, modelRevision, false);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the remove-type for removing
	 * an {@link XModel} from an {@link XRepository}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} from which
	 *            the {@link XModel} is to be removed.
	 * @param modelID The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision the current revision number of the {@link XModel}
	 *            which is to be removed
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XRepositoryCommand} of the remove-type.
	 */
	public XRepositoryCommand createRemoveModelCommand(XID repositoryID, XID modelID,
	        long modelRevision, boolean isForced) {
		long revNr = modelRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, null, null, null);
		
		return MemoryRepositoryCommand.createRemoveCommand(target, revNr, modelID);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID modelID, XID objectID, long objectRevision) {
		return createRemoveObjectCommand(null, modelID, objectID, objectRevision, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID modelID, XID objectID, long objectRevision,
	        boolean isForced) {
		return createRemoveObjectCommand(null, modelID, objectID, objectRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} from which the {@link XObject} is to be
	 *            removed.
	 * @param modelID The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID repositoryID, XID modelID, XID objectID,
	        long objectRevision) {
		return createRemoveObjectCommand(repositoryID, modelID, objectID, objectRevision, false);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type for removing an
	 * {@link XObject} from an {@link XModel}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} holding
	 *            the {@link XModel} from which the {@link XObject} is to be
	 *            removed.
	 * @param modelID The {@link XID} of the {@link XModel} from which the
	 *            {@link XObject} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XModelCommand} of the remove-type.
	 */
	public XModelCommand createRemoveObjectCommand(XID repositoryID, XID modelID, XID objectID,
	        long objectRevision, boolean isForced) {
		long revNr = objectRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider().fromComponents(repositoryID, modelID, null, null);
		
		return MemoryModelCommand.createRemoveCommand(target, revNr, objectID);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID fieldID, long fieldRevision, XValue value) {
		return createAddValueCommand(null, null, null, fieldID, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID fieldID, long fieldRevision, XValue value,
	        boolean isForced) {
		return createAddValueCommand(null, null, null, fieldID, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID objectID, XID fieldID, long fieldRevision,
	        XValue value) {
		return createAddValueCommand(null, null, objectID, fieldID, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID objectID, XID fieldID, long fieldRevision,
	        XValue value, boolean isForced) {
		return createAddValueCommand(null, null, objectID, fieldID, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, XValue value) {
		return createAddValueCommand(null, modelID, objectID, fieldID, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, XValue value, boolean isForced) {
		return createAddValueCommand(null, modelID, objectID, fieldID, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * 
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value) {
		return createAddValueCommand(repositoryID, modelID, objectID, fieldID, fieldRevision,
		        value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type for adding an
	 * {@link XValue} to an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} to which the
	 *            {@link XValue} is to be added.
	 * @param fieldRevision The current revision number of the {@link XField} to
	 *            which the {@link XValue} is to be added.
	 * @param value the {@link XValue} which is to be added.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the add-type.
	 */
	public XFieldCommand createAddValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider()
		        .fromComponents(repositoryID, modelID, objectID, fieldID);
		
		return MemoryFieldCommand.createAddCommand(target, revNr, value);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID fieldID, long fieldRevision) {
		return createRemoveValueCommand(null, null, null, fieldID, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID fieldID, long fieldRevision, boolean isForced) {
		return createRemoveValueCommand(null, null, null, fieldID, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID objectID, XID fieldID, long fieldRevision) {
		return createRemoveValueCommand(null, null, objectID, fieldID, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID objectID, XID fieldID, long fieldRevision,
	        boolean isForced) {
		return createRemoveValueCommand(null, null, objectID, fieldID, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision) {
		return createRemoveValueCommand(null, modelID, objectID, fieldID, fieldRevision, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, boolean isForced) {
		return createRemoveValueCommand(null, modelID, objectID, fieldID, fieldRevision, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision) {
		return createRemoveValueCommand(repositoryID, modelID, objectID, fieldID, fieldRevision,
		        false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type for removing an
	 * {@link XValue} from an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} to which the {@link XValue} is to be added.
	 * @param fieldID The {@link XID} of the {@link XField} from which the
	 *            {@link XValue} is to be removed.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            from which the {@link XValue} is to be removed.
	 * @param value the {@link XValue} which is to be removed.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the remove-type.
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider()
		        .fromComponents(repositoryID, modelID, objectID, fieldID);
		
		return MemoryFieldCommand.createRemoveCommand(target, revNr);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	
	public XFieldCommand createChangeValueCommand(XID fieldID, long fieldRevision, XValue value) {
		return createChangeValueCommand(null, null, null, fieldID, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID fieldID, long fieldRevision, XValue value,
	        boolean isForced) {
		return createChangeValueCommand(null, null, null, fieldID, fieldRevision, value, isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID objectID, XID fieldID, long fieldRevision,
	        XValue value) {
		return createChangeValueCommand(null, null, objectID, fieldID, fieldRevision, value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID objectID, XID fieldID, long fieldRevision,
	        XValue value, boolean isForced) {
		return createChangeValueCommand(null, null, objectID, fieldID, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, XValue value) {
		return createChangeValueCommand(null, modelID, objectID, fieldID, fieldRevision, value,
		        false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID modelID, XID objectID, XID fieldID,
	        long fieldRevision, XValue value, boolean isForced) {
		return createChangeValueCommand(null, modelID, objectID, fieldID, fieldRevision, value,
		        isForced);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value) {
		return createChangeValueCommand(repositoryID, modelID, objectID, fieldID, fieldRevision,
		        value, false);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type for changing the
	 * {@link XValue} of an {@link XField}.
	 * 
	 * @param repositoryID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param modelID The {@link XID} of the {@link XModel} holding the
	 *            {@link XObject} holding the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param objectID The {@link XID} of the {@link XObject} holding the
	 *            {@link XField} of which the {@link XValue} is to be changed.
	 * @param fieldID The {@link XID} of the {@link XField} of which the
	 *            {@link XValue} is to be changed.
	 * @param fieldRevision The current revision number of the {@link XField} of
	 *            which the {@link XValue} is to be changed.
	 * @param value the new {@link XValue}.
	 * @param isForced this parameter determines if the new command will be a
	 *            forced command or not.
	 * @return A new {@link XFieldCommand} of the change-type.
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value, boolean isForced) {
		long revNr = fieldRevision;
		
		if(isForced) {
			revNr = XCommand.FORCED;
		}
		
		XAddress target = X.getIDProvider()
		        .fromComponents(repositoryID, modelID, objectID, fieldID);
		
		return MemoryFieldCommand.createChangeCommand(target, revNr, value);
	}
	
}

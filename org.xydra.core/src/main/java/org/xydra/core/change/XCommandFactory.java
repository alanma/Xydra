package org.xydra.core.change;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


/**
 * A factory for creating {@link XCommand}s of different kinds.
 * 
 * TODO Why don't the methods accept an {@link XAddress} for the target instead
 * of multiple {@link XCommand}s? ~Daniel
 * 
 * @author Kaidel
 * 
 */
public interface XCommandFactory {
	
	/**
	 * Creates an {@link XRepositoryCommand} that will add an {@link XModel} to
	 * the specified {@link XRepository}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be added to.
	 * @param modelID The {@link XID} for the new {@link XModel}.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createAddModelCommand(XID repositoryID, XID modelID, boolean isForced);
	
	/**
	 * Creates an {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be removed from.
	 * @param modelID The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision The revision number of the {@link XModel} that is to
	 *            be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createRemoveModelCommand(XID repositoryID, XID modelID,
	        long modelRevision, boolean isForced);
	
	/**
	 * Creates an {@link XModelCommand} that will add an {@link XObject} to the
	 * specified {@link XModel}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be added to.
	 * @param modelID The {@link XID} of the {@link XModel} to which the
	 *            {@link XObject} shall be added to.
	 * @param objectID The {@link XID} for the new {@link XObject}.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createAddObjectCommand(XID repositoryID, XID modelID, XID objectID,
	        boolean isForced);
	
	/**
	 * Creates an {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be removed
	 *            from.
	 * @param modelID The {@link XID} of the {@link XModel} that the
	 *            {@link XObject} shall be removed from.
	 * @param objectID The {@link XID} for the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The revision number of the {@link XObject} that is
	 *            to be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createRemoveObjectCommand(XID repositoryID, XID modelID, XID objectID,
	        long objectRevision, boolean isForced);
	
	/**
	 * Creates an {@link XObjectCommand} that will add an {@link XField} to the
	 * specified {@link XObject}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be added to.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectID The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, boolean isForced);
	
	/**
	 * Creates an {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be removed from.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectID The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param fieldRevision The revision number of the {@link XField} that is to
	 *            be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will add an {@link XValue} to the
	 * specified {@link XField}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} that the {@link XValue} shall be
	 *            added to.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} that the
	 *            {@link XValue} shall be added to.
	 * @param objectID The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} that the {@link XValue} is to be added to.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to.
	 * @param value The {@link XValue} which is to be added.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createAddValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value, boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will remove the specified
	 * {@link XValue} from the specified {@link XField}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} that the {@link XValue} shall be
	 *            removed from.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} that the
	 *            {@link XValue} shall be removed from.
	 * @param objectID The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} that the {@link XValue} is to be removed from.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be removed from.
	 * @param value The {@link XValue} which is to be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will change the {@link XValue} of
	 * the specified {@link XField}
	 * 
	 * @param repositoryID The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} which {@link XValue} shall be
	 *            changed.
	 * @param modelID The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} which
	 *            {@link XValue} shall be changed.
	 * @param objectID The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} which {@link XValue} is to be changed.
	 * @param fieldRevision The revision number of the {@link XField} which
	 *            {@link XValue} is to be changed.
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryID, XID modelID, XID objectID,
	        XID fieldID, long fieldRevision, XValue value, boolean isForced);
	
}

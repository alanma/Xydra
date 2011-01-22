package org.xydra.base.change;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


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
	 * Creates an {@link XObjectCommand} that will add an {@link XField} to the
	 * specified {@link XObject}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, boolean isForced);
	
	/**
	 * Creates an {@link XRepositoryCommand} that will add an {@link XModel} to
	 * the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be added to.
	 * @param modelId The {@link XID} for the new {@link XModel}.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createAddModelCommand(XID repositoryId, XID modelId, boolean isForced);
	
	/**
	 * Creates an {@link XModelCommand} that will add an {@link XObject} to the
	 * specified {@link XModel}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} to which the
	 *            {@link XObject} shall be added to.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createAddObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will add an {@link XValue} to the
	 * specified {@link XField}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} that the {@link XValue} shall be
	 *            added to.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} that the
	 *            {@link XValue} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} that the {@link XValue} is to be added to.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to.
	 * @param value The {@link XValue} which is to be added.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will change the {@link XValue} of
	 * the specified {@link XField}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} which {@link XValue} shall be
	 *            changed.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} which
	 *            {@link XValue} shall be changed.
	 * @param objectId The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} which {@link XValue} is to be changed.
	 * @param fieldRevision The revision number of the {@link XField} which
	 *            {@link XValue} is to be changed.
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced);
	
	/**
	 * Creates an {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be removed from.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param fieldRevision The revision number of the {@link XField} that is to
	 *            be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced);
	
	/**
	 * Creates an {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be removed from.
	 * @param modelId The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision The revision number of the {@link XModel} that is to
	 *            be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createRemoveModelCommand(XID repositoryId, XID modelId,
	        long modelRevision, boolean isForced);
	
	/**
	 * Creates an {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be removed
	 *            from.
	 * @param modelId The {@link XID} of the {@link XModel} that the
	 *            {@link XObject} shall be removed from.
	 * @param objectId The {@link XID} for the {@link XObject} which is to be
	 *            removed.
	 * @param objectRevision The revision number of the {@link XObject} that is
	 *            to be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 * 
	 *         TODO max: Why can I indicate FORCED via a special objectRevision
	 *         AND the boolean flag?
	 */
	public XModelCommand createRemoveObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        long objectRevision, boolean isForced);
	
	/**
	 * Creates an {@link XFieldCommand} that will remove the specified
	 * {@link XValue} from the specified {@link XField}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject}
	 *            containing the {@link XField} that the {@link XValue} shall be
	 *            removed from.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} containing the {@link XField} that the
	 *            {@link XValue} shall be removed from.
	 * @param objectId The {@link XID} of the {@link XObject} containing the
	 *            {@link XField} that the {@link XValue} is to be removed from.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be removed from.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced);
	
}

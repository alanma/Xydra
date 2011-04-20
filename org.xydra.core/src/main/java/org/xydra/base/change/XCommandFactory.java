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
 * TODO Specify the exception cases, i.e. if a wrong address is given.
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
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, boolean isForced);
	
	/**
	 * Creates a safe {@link XObjectCommand} that will add an {@link XField} to
	 * the specified {@link XObject}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createSafeAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId);
	
	/**
	 * Creates a forced {@link XObjectCommand} that will add an {@link XField}
	 * to the specified {@link XObject}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createForcedAddFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId);
	
	/**
	 * Creates an {@link XObjectCommand} that will add an {@link XField} to the
	 * specified {@link XObject}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} to which
	 *            the {@link XField} shall be added.
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createAddFieldCommand(XAddress objectAddress, XID fieldId,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XObjectCommand} that will add an {@link XField} to
	 * the specified {@link XObject}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} to which
	 *            the {@link XField} shall be added.
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createSafeAddFieldCommand(XAddress objectAddress, XID fieldId);
	
	/**
	 * Creates a forced {@link XObjectCommand} that will add an {@link XField}
	 * to the specified {@link XObject}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} to which
	 *            the {@link XField} shall be added.
	 * @param fieldId The {@link XID} of the new {@link XField}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createForcedAddFieldCommand(XAddress objectAddress, XID fieldId);
	
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
	 * Creates a safe {@link XRepositoryCommand} that will add an {@link XModel}
	 * to the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be added to.
	 * @param modelId The {@link XID} for the new {@link XModel}.
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createSafeAddModelCommand(XID repositoryId, XID modelId);
	
	/**
	 * Creates a forced {@link XRepositoryCommand} that will add an
	 * {@link XModel} to the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be added to.
	 * @param modelId The {@link XID} for the new {@link XModel}.
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createForcedAddModelCommand(XID repositoryId, XID modelId);
	
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
	 * Creates a safe {@link XModelCommand} that will add an {@link XObject} to
	 * the specified {@link XModel}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} to which the
	 *            {@link XObject} shall be added to.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createSafeAddObjectCommand(XID repositoryId, XID modelId, XID objectId);
	
	/**
	 * Creates a forced {@link XModelCommand} that will add an {@link XObject}
	 * to the specified {@link XModel}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be added to.
	 * @param modelId The {@link XID} of the {@link XModel} to which the
	 *            {@link XObject} shall be added to.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createForcedAddObjectCommand(XID repositoryId, XID modelId, XID objectId);
	
	/**
	 * Creates an {@link XModelCommand} that will add an {@link XObject} to the
	 * specified {@link XModel}
	 * 
	 * @param modelAddress The {@link XAddress} to which the {@link XModel} is
	 *            to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createAddObjectCommand(XAddress modelAddress, XID objectId,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XModelCommand} that will add an {@link XObject} to
	 * the specified {@link XModel}
	 * 
	 * @param modelAddress The {@link XAddress} to which the {@link XModel} is
	 *            to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createSafeAddObjectCommand(XAddress modelAddress, XID objectId);
	
	/**
	 * Creates a forced {@link XModelCommand} that will add an {@link XObject}
	 * to the specified {@link XModel}
	 * 
	 * @param modelAddress The {@link XAddress} to which the {@link XModel} is
	 *            to be added.
	 * @param objectId The {@link XID} for the new {@link XObject}.
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createForcedAddObjectCommand(XAddress modelAddress, XID objectId);
	
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
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param value The {@link XValue} which is to be added.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will add an {@link XValue} to
	 * the specified {@link XField}
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
	 *            {@link XValue} is to be added to. Since this method is used to
	 *            create safe commands, passing XCommand.FORCED as the revision
	 *            number will throw a {@link RuntimeException}
	 * 
	 * @param value The {@link XValue} which is to be added.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will add an {@link XValue} to
	 * the specified {@link XField}
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
	 * @param value The {@link XValue} which is to be added.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedAddValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, XValue value);
	
	/**
	 * Creates an {@link XFieldCommand} that will add an {@link XValue} to the
	 * specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} to which
	 *            the {@link XValue} is to be added.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to.
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param value The {@link XValue} which is to be added.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createAddValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value, boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will add an {@link XValue} to
	 * the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} to which
	 *            the {@link XValue} is to be added.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to. Since this method is used to
	 *            create safe commands, passing XCommand.FORCED as the revision
	 *            number will throw a {@link RuntimeException}
	 * @param value The {@link XValue} which is to be added.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeAddValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will add an {@link XValue} to
	 * the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} to which
	 *            the {@link XValue} is to be added.
	 * @param value The {@link XValue} which is to be added.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedAddValueCommand(XAddress fieldAddress, XValue value);
	
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
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createChangeValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value, boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will change the {@link XValue}
	 * of the specified {@link XField}
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
	 *            {@link XValue} is to be changed. Since this method is used to
	 *            create safe commands, passing XCommand.FORCED as the revision
	 *            number will throw a {@link RuntimeException}
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeChangeValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, XValue value);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will change the
	 * {@link XValue} of the specified {@link XField}
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
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedChangeValueCommand(XID repositoryId, XID modelId,
	        XID objectId, XID fieldId, XValue value);
	
	/**
	 * Creates an {@link XFieldCommand} that will change the {@link XValue} of
	 * the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} of which
	 *            the {@link XValue} is to be changed.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to.
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createChangeValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value, boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will change the {@link XValue}
	 * of the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} of which
	 *            the {@link XValue} is to be changed.
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be added to. Since this method is used to
	 *            create safe commands, passing XCommand.FORCED as the revision
	 *            number will throw a {@link RuntimeException}
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeChangeValueCommand(XAddress fieldAddress, long fieldRevision,
	        XValue value);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will change the
	 * {@link XValue} of the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} of which
	 *            the {@link XValue} is to be changed.
	 * @param value The {@link XValue} to which the current {@link XValue} of
	 *            the {@link XField} is to be changed to.
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedChangeValueCommand(XAddress fieldAddress, XValue value);
	
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
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createRemoveFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced);
	
	/**
	 * Creates a safe {@link XObjectCommand} that will remove the specified
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
	 *            be removed. Since this method is used to create safe commands,
	 *            passing XCommand.FORCED as the revision number will throw a
	 *            {@link RuntimeException}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createSafeRemoveFieldCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision);
	
	/**
	 * Creates a forced {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel}, which contains the {@link XObject} that
	 *            the {@link XField} shall be removed from.
	 * @param modelId The {@link XID} of the {@link XModel} containing the
	 *            {@link XObject} that the {@link XField} shall be added to.
	 * @param objectId The {@link XID} of the {@link XObject} that the
	 *            {@link XField} is to be added to.
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createForcedRemoveFieldCommand(XID repositoryId, XID modelId,
	        XID objectId, XID fieldId);
	
	/**
	 * Creates an {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed.
	 * @param fieldRevision The revision number of the {@link XField} that is to
	 *            be removed.
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createRemoveFieldCommand(XAddress fieldAddress, long fieldRevision,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed.
	 * @param fieldRevision The revision number of the {@link XField} that is to
	 *            be removed. Since this method is used to create safe commands,
	 *            passing XCommand.FORCED as the revision number will throw a
	 *            {@link RuntimeException}
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createSafeRemoveFieldCommand(XAddress fieldAddress, long fieldRevision);
	
	/**
	 * Creates a forced {@link XObjectCommand} that will remove the specified
	 * {@link XField} from the specified {@link XObject}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed.
	 * @return an {@link XObjectCommand} with the specified settings
	 */
	public XObjectCommand createForcedRemoveFieldCommand(XAddress fieldAddress);
	
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
	 * Creates a safe {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be removed from.
	 * @param modelId The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @param modelRevision The revision number of the {@link XModel} that is to
	 *            be removed. Since this method is used to create safe commands,
	 *            passing XCommand.FORCED as the revision number will throw a
	 *            {@link RuntimeException}
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createSafeRemoveModelCommand(XID repositoryId, XID modelId,
	        long modelRevision);
	
	/**
	 * Creates a safe {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} that the
	 *            {@link XModel} shall be removed from.
	 * @param modelId The {@link XID} of the {@link XModel} which is to be
	 *            removed.
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createForcedRemoveModelCommand(XID repositoryId, XID modelId);
	
	/**
	 * Creates an {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param modelAddress The {@link XAddress} of the {@link XModel} which is
	 *            to be removed
	 * @param modelRevision The revision number of the {@link XModel} that is to
	 *            be removed.
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createRemoveModelCommand(XAddress modelAddress, long modelRevision,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XRepositoryCommand} that will remove the specified
	 * {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param modelAddress The {@link XAddress} of the {@link XModel} which is
	 *            to be removed
	 * @param modelRevision The revision number of the {@link XModel} that is to
	 *            be removed. Since this method is used to create safe commands,
	 *            passing XCommand.FORCED as the revision number will throw a
	 *            {@link RuntimeException}
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createSafeRemoveModelCommand(XAddress modelAddress, long modelRevision);
	
	/**
	 * Creates a forced {@link XRepositoryCommand} that will remove the
	 * specified {@link XModel} from the specified {@link XRepository}
	 * 
	 * @param modelAddress The {@link XAddress} of the {@link XModel} which is
	 *            to be removed
	 * @return an {@link XRepositoryCommand} with the specified settings
	 */
	public XRepositoryCommand createForcedRemoveModelCommand(XAddress modelAddress);
	
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
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
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
	 * Creates a safe {@link XModelCommand} that will remove the specified
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
	 *            to be removed. Since this method is used to create safe
	 *            commands, passing XCommand.FORCED as the revision number will
	 *            throw a {@link RuntimeException}
	 * 
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createSafeRemoveObjectCommand(XID repositoryId, XID modelId, XID objectId,
	        long objectRevision);
	
	/**
	 * Creates a forced {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param repositoryId The {@link XID} of the {@link XRepository} containing
	 *            the {@link XModel} that the {@link XObject} shall be removed
	 *            from.
	 * @param modelId The {@link XID} of the {@link XModel} that the
	 *            {@link XObject} shall be removed from.
	 * @param objectId The {@link XID} for the {@link XObject} which is to be
	 *            removed.
	 * 
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createForcedRemoveObjectCommand(XID repositoryId, XID modelId, XID objectId);
	
	/**
	 * Creates an {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} which is
	 *            to be removed
	 * @param objectRevision The revision number of the {@link XObject} that is
	 *            to be removed.
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XModelCommand} with the specified settings
	 * 
	 *         TODO max: Why can I indicate FORCED via a special objectRevision
	 *         AND the boolean flag?
	 */
	public XModelCommand createRemoveObjectCommand(XAddress objectAddress, long objectRevision,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} which is
	 *            to be removed
	 * @param objectRevision The revision number of the {@link XObject} that is
	 *            to be removed. Since this method is used to create safe
	 *            commands, passing XCommand.FORCED as the revision number will
	 *            throw a {@link RuntimeException}
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createSafeRemoveObjectCommand(XAddress objectAddress, long objectRevision);
	
	/**
	 * Creates a forced {@link XModelCommand} that will remove the specified
	 * {@link XObject} from the specified {@link XModel}
	 * 
	 * @param objectAddress the {@link XAddress} of the {@link XObject} which is
	 *            to be removed
	 * @return an {@link XModelCommand} with the specified settings
	 */
	public XModelCommand createForcedRemoveObjectCommand(XAddress objectAddress);
	
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
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createRemoveValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision, boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will remove the specified
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
	 *            {@link XValue} is to be removed from. Since this method is
	 *            used to create safe commands, passing XCommand.FORCED as the
	 *            revision number will throw a {@link RuntimeException}
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeRemoveValueCommand(XID repositoryId, XID modelId, XID objectId,
	        XID fieldId, long fieldRevision);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will remove the specified
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
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedRemoveValueCommand(XID repositoryId, XID modelId,
	        XID objectId, XID fieldId);
	
	/**
	 * Creates an {@link XFieldCommand} that will remove the specified
	 * {@link XValue} from the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be removed from.
	 * 
	 *            TODO Is this value ignored if isForced = true? What value
	 *            should I use here if isForced = true? 0?
	 * @param isForced true, if this XCommand should be a forced command, false
	 *            otherwise
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createRemoveValueCommand(XAddress fieldAddress, long fieldRevision,
	        boolean isForced);
	
	/**
	 * Creates a safe {@link XFieldCommand} that will remove the specified
	 * {@link XValue} from the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed
	 * @param fieldRevision The revision number of the {@link XField} that the
	 *            {@link XValue} is to be removed from. Since this method is
	 *            used to create safe commands, passing XCommand.FORCED as the
	 *            revision number will throw a {@link RuntimeException}
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createSafeRemoveValueCommand(XAddress fieldAddress, long fieldRevision);
	
	/**
	 * Creates a forced {@link XFieldCommand} that will remove the specified
	 * {@link XValue} from the specified {@link XField}
	 * 
	 * @param fieldAddress the {@link XAddress} of the {@link XField} which is
	 *            to be removed
	 * @return an {@link XFieldCommand} with the specified settings
	 */
	public XFieldCommand createForcedRemoveValueCommand(XAddress fieldAddress);
}

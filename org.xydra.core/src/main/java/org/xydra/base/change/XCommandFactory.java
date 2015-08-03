package org.xydra.base.change;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.value.XValue;


/**
 * A factory for creating {@link XCommand}s of different kinds.
 *
 * TODO API Specify the exception cases, i.e. if a wrong address is given.
 *
 * @author kaidel
 *
 *         FIXME API max: method signatures in this class are inconsistent. E.g.
 *         removeField takes a fieldAddress, addField takes an objectAddress and
 *         a fieldId. Better use addresses everywhere.
 *
 */
public interface XCommandFactory {

    /**
     * Creates an {@link XObjectCommand} that will add an field to the specified
     * object
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeAddFieldCommand(XAddress, XId)} or
     * {@link #createForcedAddFieldCommand(XAddress, XId)} instead.
     *
     * @param objectAddress the {@link XAddress} of the object to which the
     *            field shall be added.
     * @param fieldId The {@link XId} of the new field
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createAddFieldCommand(XAddress objectAddress, XId fieldId,
            boolean isForced);

    /**
     * Creates an {@link XRepositoryCommand} that will add a model to the
     * specified repository
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeAddModelCommand(XId, XId)} or
     * {@link #createForcedAddModelCommand(XId, XId)} instead.
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be added to.
     * @param modelId The {@link XId} for the new model.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XRepositoryCommand} with the specified settings
     */
    public XRepositoryCommand createAddModelCommand(XId repositoryId, XId modelId, boolean isForced);

    /**
     * Creates an {@link XModelCommand} that will add an object to the specified
     * model
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeAddObjectCommand(XAddress, XId)} or
     * {@link #createForcedAddObjectCommand(XAddress, XId)} instead.
     *
     * @param modelAddress The {@link XAddress} to which the model is to be
     *            added.
     * @param objectId The {@link XId} for the new object.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createAddObjectCommand(XAddress modelAddress, XId objectId,
            boolean isForced);

    /**
     * Creates an {@link XFieldCommand} that will add an {@link XValue} to the
     * specified field
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeAddValueCommand(XAddress, long, XValue)} or
     * {@link #createForcedAddValueCommand(XAddress, XValue)} instead.
     *
     * @param fieldAddress the {@link XAddress} of the field to which the
     *            {@link XValue} is to be added.
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be added to.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param value The {@link XValue} which is to be added.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XFieldCommand createAddValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value, boolean isForced);

    /**
     * Creates an {@link XFieldCommand} that will change the {@link XValue} of
     * the specified field
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeChangeValueCommand(XAddress, long, XValue)} or
     * {@link #createForcedChangeValueCommand(XAddress, XValue)} instead.
     *
     * @param fieldAddress the {@link XAddress} of the field of which the
     *            {@link XValue} is to be changed.
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be added to.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param value The {@link XValue} to which the current {@link XValue} of
     *            the field is to be changed to.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XFieldCommand createChangeValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value, boolean isForced);

    /**
     * Creates a forced {@link XObjectCommand} that will add an field to the
     * specified object
     *
     * @param objectAddress the {@link XAddress} of the object to which the
     *            field shall be added.
     * @param fieldId The {@link XId} of the new field
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createForcedAddFieldCommand(XAddress objectAddress, XId fieldId);

    /**
     * Creates a forced {@link XObjectCommand} that will add an field to the
     * specified object
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object that the field shall be added
     *            to.
     * @param modelId The {@link XId} of the model containing the object that
     *            the field shall be added to.
     * @param objectId The {@link XId} of the object that the field is to be
     *            added to.
     * @param fieldId The {@link XId} of the new field
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createForcedAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId);

    /**
     * Creates a forced {@link XRepositoryCommand} that will add an model to the
     * specified repository
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be added to.
     * @param modelId The {@link XId} for the new model.
     * @return an {@link XRepositoryCommand} with the specified settings
     */
    public XRepositoryCommand createForcedAddModelCommand(XId repositoryId, XId modelId);

    /**
     * Creates a forced {@link XModelCommand} that will add an object to the
     * specified model
     *
     * @param modelAddress The {@link XAddress} to which the model is to be
     *            added.
     * @param objectId The {@link XId} for the new object.
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createForcedAddObjectCommand(XAddress modelAddress, XId objectId);

    /**
     * Creates a forced {@link XFieldCommand} that will add an {@link XValue} to
     * the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field to which the
     *            {@link XValue} is to be added.
     * @param value The {@link XValue} which is to be added.
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedAddValueCommand(XAddress fieldAddress, XValue value);

    /**
     * Creates a forced {@link XFieldCommand} that will add an {@link XValue} to
     * the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field that the
     *            {@link XValue} shall be added to.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field that the {@link XValue} shall be added
     *            to.
     * @param objectId The {@link XId} of the object containing the field that
     *            the {@link XValue} is to be added to.
     * @param fieldId
     * @param value The {@link XValue} which is to be added.
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, XValue value);

    /**
     * Creates a forced {@link XFieldCommand} that will change the
     * {@link XValue} of the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field of which the
     *            {@link XValue} is to be changed.
     * @param value The {@link XValue} to which the current {@link XValue} of
     *            the field is to be changed to.
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedChangeValueCommand(XAddress fieldAddress, XValue value);

    /**
     * Creates a forced {@link XFieldCommand} that will change the
     * {@link XValue} of the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field which
     *            {@link XValue} shall be changed.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field which {@link XValue} shall be changed.
     * @param objectId The {@link XId} of the object containing the field which
     *            {@link XValue} is to be changed.
     * @param fieldId
     * @param value The {@link XValue} to which the current {@link XValue} of
     *            the field is to be changed to.
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedChangeValueCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId, XValue value);

    /**
     * Creates a forced {@link XObjectCommand} that will remove the specified
     * field from the specified object
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed.
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createForcedRemoveFieldCommand(XAddress fieldAddress);

    /**
     * Creates a forced {@link XObjectCommand} that will remove the specified
     * field from the specified object
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object that the field shall be
     *            removed from.
     * @param modelId The {@link XId} of the model containing the object that
     *            the field shall be added to.
     * @param objectId The {@link XId} of the object that the field is to be
     *            added to.
     * @param fieldId
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createForcedRemoveFieldCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId);

    /**
     * Creates a forced {@link XRepositoryCommand} that will remove the
     * specified model from the specified repository
     *
     * @param modelAddress The {@link XAddress} of the model which is to be
     *            removed
     * @return an {@link XRepositoryCommand} with the specified settings
     */
    public XRepositoryCommand createForcedRemoveModelCommand(XAddress modelAddress);

    /**
     * Creates a safe {@link XRepositoryCommand} that will remove the specified
     * model from the specified repository
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be removed from.
     * @param modelId The {@link XId} of the model which is to be removed.
     * @return an {@link XRepositoryCommand} with the specified settings
     */
    public XRepositoryCommand createForcedRemoveModelCommand(XId repositoryId, XId modelId);

    /**
     * Creates a forced {@link XModelCommand} that will remove the specified
     * object from the specified model
     *
     * @param objectAddress the {@link XAddress} of the object which is to be
     *            removed
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createForcedRemoveObjectCommand(XAddress objectAddress);

    /**
     * Creates a forced {@link XModelCommand} that will remove the specified
     * object from the specified model
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model that the object shall be removed from.
     * @param modelId The {@link XId} of the model that the object shall be
     *            removed from.
     * @param objectId The {@link XId} for the object which is to be removed.
     *
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createForcedRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId);

    /**
     * Creates a forced {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedRemoveValueCommand(XAddress fieldAddress);

    /**
     * Creates a forced {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field that the
     *            {@link XValue} shall be removed from.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field that the {@link XValue} shall be removed
     *            from.
     * @param objectId The {@link XId} of the object containing the field that
     *            the {@link XValue} is to be removed from.
     * @param fieldId
     * @return an {@link XFieldCommand} with the specified settings
     */
    public XFieldCommand createForcedRemoveValueCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId);

    /**
     * Creates an {@link XObjectCommand} that will remove the specified field
     * from the specified object
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveFieldCommand(XAddress, long)} or
     * {@link #createForcedRemoveFieldCommand(XAddress)} instead.
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed.
     * @param fieldRevision The revision number of the field that is to be
     *            removed.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XObjectCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XObjectCommand createRemoveFieldCommand(XAddress fieldAddress, long fieldRevision,
            boolean isForced);

    /**
     * Creates an {@link XObjectCommand} that will remove the specified field
     * from the specified object
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveFieldCommand(XId, XId, XId, XId, long)} or
     * {@link #createForcedRemoveFieldCommand(XId, XId, XId, XId)} instead.
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object that the field shall be
     *            removed from.
     * @param modelId The {@link XId} of the model containing the object that
     *            the field shall be added to.
     * @param objectId The {@link XId} of the object that the field is to be
     *            added to.
     * @param fieldId
     * @param fieldRevision The revision number of the field that is to be
     *            removed.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XObjectCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XObjectCommand createRemoveFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, boolean isForced);

    /**
     * Creates an {@link XRepositoryCommand} that will remove the specified
     * model from the specified repository
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveModelCommand(XAddress, long)} or
     * {@link #createForcedRemoveModelCommand(XAddress)} instead.
     *
     * @param modelAddress The {@link XAddress} of the model which is to be
     *            removed
     * @param modelRevision The revision number of the model that is to be
     *            removed.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @return an {@link XRepositoryCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#modelRevision} and {#isForced} is set to false.
     */
    public XRepositoryCommand createRemoveModelCommand(XAddress modelAddress, long modelRevision,
            boolean isForced);

    /**
     * Creates an {@link XRepositoryCommand} that will remove the specified
     * model from the specified repository
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveModelCommand(XId, XId, long)} or
     * {@link #createForcedRemoveModelCommand(XId, XId)} instead.
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be removed from.
     * @param modelId The {@link XId} of the model which is to be removed.
     * @param modelRevision The revision number of the model that is to be
     *            removed .
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @return an {@link XRepositoryCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#modelRevision} and {#isForced} is set to false.
     */
    public XRepositoryCommand createRemoveModelCommand(XId repositoryId, XId modelId,
            long modelRevision, boolean isForced);

    /**
     * Creates an {@link XModelCommand} that will remove the specified object
     * from the specified model
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveObjectCommand(XAddress, long)} or
     * {@link #createForcedRemoveObjectCommand(XAddress)} instead.
     *
     * @param objectAddress the {@link XAddress} of the object which is to be
     *            removed
     * @param objectRevision The revision number of the object that is to be
     *            removed.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XModelCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#objectRevision} and {#isForced} is set to false.
     */
    public XModelCommand createRemoveObjectCommand(XAddress objectAddress, long objectRevision,
            boolean isForced);

    /**
     * Creates an {@link XModelCommand} that will remove the specified object
     * from the specified model
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveObjectCommand(XId, XId, XId, long)} or
     * {@link #createForcedRemoveObjectCommand(XId, XId, XId)} instead.
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model that the object shall be removed from.
     * @param modelId The {@link XId} of the model that the object shall be
     *            removed from.
     * @param objectId The {@link XId} for the object which is to be removed.
     * @param objectRevision The revision number of the object that is to be
     *            removed.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XModelCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#objectRevision} and {#isForced} is set to false.
     */
    public XModelCommand createRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId,
            long objectRevision, boolean isForced);

    /**
     * Creates an {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveValueCommand(XAddress, long)} or
     * {@link #createForcedRemoveValueCommand(XAddress)} instead.
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be removed from.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XFieldCommand createRemoveValueCommand(XAddress fieldAddress, long fieldRevision,
            boolean isForced);

    /**
     * Creates an {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * This method is for advanced users only. We recommend using
     * {@link #createSafeRemoveValueCommand(XId, XId, XId, XId, long)} or
     * {@link #createForcedRemoveValueCommand(XId, XId, XId, XId)} instead.
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field that the
     *            {@link XValue} shall be removed from.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field that the {@link XValue} shall be removed
     *            from.
     * @param objectId The {@link XId} of the object containing the field that
     *            the {@link XValue} is to be removed from.
     * @param fieldId
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be removed from.
     *
     *            Passing {@link XCommand#FORCED} when {#isForced} is set to
     *            false will throw a {@link IllegalArgumentException}. If
     *            {#isForced} is set to true, the given long value will be
     *            ignored.
     * @param isForced true, if this XCommand should be a forced command, false
     *            otherwise
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision} and {#isForced} is set to false.
     */
    public XFieldCommand createRemoveValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, boolean isForced);

    /**
     * Creates a safe state-bound {@link XObjectCommand} that will add an field
     * to the specified object
     *
     * @param objectAddress the {@link XAddress} of the object to which the
     *            field shall be added.
     * @param fieldId The {@link XId} of the new field
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createSafeAddFieldCommand(XAddress objectAddress, XId fieldId);

    /**
     * Creates a safe state-bound {@link XObjectCommand} that will add an field
     * to the specified object
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object that the field shall be added
     *            to.
     * @param modelId The {@link XId} of the model containing the object that
     *            the field shall be added to.
     * @param objectId The {@link XId} of the object that the field is to be
     *            added to.
     * @param fieldId The {@link XId} of the new field
     * @return an {@link XObjectCommand} with the specified settings
     */
    public XObjectCommand createSafeAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId);

    /**
     * Creates a safe state-bound {@link XRepositoryCommand} that will add a
     * model to the specified repository
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be added to.
     * @param modelId The {@link XId} for the new model.
     * @return an {@link XRepositoryCommand} with the specified settings
     */
    public XRepositoryCommand createSafeAddModelCommand(XId repositoryId, XId modelId);

    /**
     * Creates a safe state-bound {@link XModelCommand} that will add an object
     * to the specified model
     *
     * @param modelAddress The {@link XAddress} to which the model is to be
     *            added.
     * @param objectId The {@link XId} for the new object.
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createSafeAddObjectCommand(XAddress modelAddress, XId objectId);

    /**
     * Creates a safe {@link XModelCommand} that will add an object to the
     * specified model
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model that the object shall be added to.
     * @param modelId The {@link XId} of the model to which the object shall be
     *            added to.
     * @param objectId The {@link XId} for the new object.
     * @return an {@link XModelCommand} with the specified settings
     */
    public XModelCommand createSafeAddObjectCommand(XId repositoryId, XId modelId, XId objectId);

    /**
     * Creates a safe {@link XFieldCommand} that will add an {@link XValue} to
     * the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field to which the
     *            {@link XValue} is to be added.
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be added to. Since this method is used to
     *            create safe commands, passing XCommand.FORCED as the revision
     *            number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     * @param value The {@link XValue} which is to be added.
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeAddValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value);

    /**
     * Creates a safe {@link XFieldCommand} that will add an {@link XValue} to
     * the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field that the
     *            {@link XValue} shall be added to.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field that the {@link XValue} shall be added
     *            to.
     * @param objectId The {@link XId} of the object containing the field that
     *            the {@link XValue} is to be added to.
     * @param fieldId
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be added to. Since this method is used to
     *            create safe commands, passing XCommand.FORCED as the revision
     *            number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     *
     * @param value The {@link XValue} which is to be added.
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value);

    /**
     * Creates a safe {@link XFieldCommand} that will change the {@link XValue}
     * of the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field of which the
     *            {@link XValue} is to be changed.
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be added to. Since this method is used to
     *            create safe commands, passing XCommand.FORCED as the revision
     *            number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     * @param value The {@link XValue} to which the current {@link XValue} of
     *            the field is to be changed to.
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeChangeValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value);

    /**
     * Creates a safe {@link XFieldCommand} that will change the {@link XValue}
     * of the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field which
     *            {@link XValue} shall be changed.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field which {@link XValue} shall be changed.
     * @param objectId The {@link XId} of the object containing the field which
     *            {@link XValue} is to be changed.
     * @param fieldId
     * @param fieldRevision The revision number of the field which
     *            {@link XValue} is to be changed. Since this method is used to
     *            create safe commands, passing XCommand.FORCED as the revision
     *            number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     * @param value The {@link XValue} to which the current {@link XValue} of
     *            the field is to be changed to.
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeChangeValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value);

    /**
     * Creates a safe {@link XObjectCommand} that will remove the specified
     * field from the specified object
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed.
     * @param fieldRevision The revision number of the field that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     * @return an {@link XObjectCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XObjectCommand createSafeRemoveFieldCommand(XAddress fieldAddress, long fieldRevision);

    /**
     * Creates a safe {@link XObjectCommand} that will remove the specified
     * field from the specified object
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object that the field shall be
     *            removed from.
     * @param modelId The {@link XId} of the model containing the object that
     *            the field shall be added to.
     * @param objectId The {@link XId} of the object that the field is to be
     *            added to.
     * @param fieldId
     * @param fieldRevision The revision number of the field that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     * @return an {@link XObjectCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     *
     */
    public XObjectCommand createSafeRemoveFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision);

    /**
     * Creates a safe {@link XRepositoryCommand} that will remove the specified
     * model from the specified repository
     *
     * @param modelAddress The {@link XAddress} of the model which is to be
     *            removed
     * @param modelRevision The revision number of the model that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     * @return an {@link XRepositoryCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#modelRevision}.
     */
    public XRepositoryCommand createSafeRemoveModelCommand(XAddress modelAddress, long modelRevision);

    /**
     * Creates a safe {@link XRepositoryCommand} that will remove the specified
     * model from the specified repository
     *
     * @param repositoryId The {@link XId} of the repository that the model
     *            shall be removed from.
     * @param modelId The {@link XId} of the model which is to be removed.
     * @param modelRevision The revision number of the model that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     * @return an {@link XRepositoryCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#modelRevision}.
     */
    public XRepositoryCommand createSafeRemoveModelCommand(XId repositoryId, XId modelId,
            long modelRevision);

    /**
     * Creates a safe {@link XModelCommand} that will remove the specified
     * object from the specified model
     *
     * @param objectAddress the {@link XAddress} of the object which is to be
     *            removed
     * @param objectRevision The revision number of the object that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     * @return an {@link XModelCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#objectRevision}.
     */
    public XModelCommand createSafeRemoveObjectCommand(XAddress objectAddress, long objectRevision);

    /**
     * Creates a safe {@link XModelCommand} that will remove the specified
     * object from the specified model
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model that the object shall be removed from.
     * @param modelId The {@link XId} of the model that the object shall be
     *            removed from.
     * @param objectId The {@link XId} for the object which is to be removed.
     * @param objectRevision The revision number of the object that is to be
     *            removed. Since this method is used to create safe commands,
     *            passing XCommand.FORCED as the revision number will throw a
     *            {@link IllegalArgumentException}. {@link RevisionConstants}
     *            .COMMAND_INTENT_SAFE_STATE_BOUND will result in a state-bound
     *            safe command
     *
     * @return an {@link XModelCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#objectRevision}.
     */
    public XModelCommand createSafeRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId,
            long objectRevision);

    /**
     * Creates a safe {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * @param fieldAddress the {@link XAddress} of the field which is to be
     *            removed
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be removed from. Since this method is
     *            used to create safe commands, passing XCommand.FORCED as the
     *            revision number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeRemoveValueCommand(XAddress fieldAddress, long fieldRevision);

    /**
     * Creates a safe {@link XFieldCommand} that will remove the specified
     * {@link XValue} from the specified field
     *
     * @param repositoryId The {@link XId} of the repository containing the
     *            model, which contains the object containing the field that the
     *            {@link XValue} shall be removed from.
     * @param modelId The {@link XId} of the model containing the object
     *            containing the field that the {@link XValue} shall be removed
     *            from.
     * @param objectId The {@link XId} of the object containing the field that
     *            the {@link XValue} is to be removed from.
     * @param fieldId
     * @param fieldRevision The revision number of the field that the
     *            {@link XValue} is to be removed from. Since this method is
     *            used to create safe commands, passing XCommand.FORCED as the
     *            revision number will throw a {@link IllegalArgumentException}.
     *            {@link RevisionConstants} .COMMAND_INTENT_SAFE_STATE_BOUND
     *            will result in a state-bound safe command
     * @return an {@link XFieldCommand} with the specified settings
     * @throws IllegalArgumentException if {@link XCommand#FORCED} is passed as
     *             {#fieldRevision}.
     */
    public XFieldCommand createSafeRemoveValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision);
}

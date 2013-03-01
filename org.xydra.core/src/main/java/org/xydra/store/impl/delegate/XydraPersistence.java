package org.xydra.store.impl.delegate;

import java.util.List;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.ModelRevision;
import org.xydra.store.XydraStore;


/**
 * A persistence SPI for {@link XydraStore} implementations.
 * 
 * Differences to {@link XydraStore} interface:
 * <ol>
 * <li>No batch operations</li>
 * <li>Synchronous (blocking)</li>
 * <li>No authorisation</li>
 * <li>No access control</li>
 * </ol>
 * 
 * @author voelkel
 */
@RunsInGWT(true)
public interface XydraPersistence {
    
    /**
     * Delete <em>all</em> data. This method should not be exposed over REST or
     * other network protocols. This method is intended to be used in unit
     * tests.
     */
    void clear();
    
    /**
     * Execute a command.
     * 
     * (Copied from {@link XydraStore}:) A non-negative number indicates the
     * resulting revision number of the changed entity.
     * 
     * For successful commands that changed something, the return value is
     * always a revision number that can be used to retrieve the corresponding
     * event using {@link #getEvents(XAddress, long, long)}
     * 
     * Like any other {@link XCommand}, {@link XTransaction}s only "take up" a
     * single revision, which is the one passed to the callback. For
     * {@link XTransaction}s as well as {@link XRepositoryCommand}s,
     * {@link XModelCommand}s and {@link XObjectCommand}s of type remove, the
     * event saved in the change log may be either a {@link XTransactionEvent}
     * or an {@link XAtomicEvent}, depending on whether there are actually
     * multiple changes.
     * 
     * Negative numbers indicate a special result: {@link XCommand#FAILED}
     * signals a failure, {@link XCommand#NOCHANGE} signals that the command did
     * not change anything.
     * 
     * Commands may still "take up" a revision number, even if they failed or
     * didn't change anything, causing the next command to skip a revision
     * number. This means that there can be revision numbers without any
     * associated events. The revision of the model however is only updated if
     * anything actually changed.
     * 
     * @param actorId only used for event logging, not for access control.
     * @param command the command to be executed, which can also be a
     *            {@link XTransaction}.
     * @return a number indicating the result of executing the command.
     */
    long executeCommand(@NeverNull XId actorId, @NeverNull XCommand command);
    
    /**
     * (Documentation copied from {@link GetEventsRequest})
     * 
     * @param address of {@link XModel} (repositoryId/modelId/-/-),
     *            {@link XObject} (repositoryId/modelId/objectId/-), or
     *            {@link XField} (repositoryId/modelId/objectId/fieldId) for
     *            which to return change events. This address must not refer to
     *            a repository.
     * 
     *            If the given address refers to a model, all events for
     *            contained objects and fields are returned as well. If the
     *            address refers to an object, the events for all contained
     *            fields are returned as well. Events for creating and removing
     *            the entity specified by the XAddress are also included.
     * 
     *            For objects and fields, the resulting events include all
     *            {@link XTransaction transactions} that contain changes to the
     *            given object or field. It is the responsibility of the client
     *            to extract the relevant event(s) from within the transaction.
     * @param beginRevision the beginning revision number (inclusive) of the
     *            interval from which all {@link XEvent XEvents} are to be
     *            returned - can be zero to get all {@link XEvent XEvents} up to
     *            endRevision.
     * @param endRevision the end revision number (inclusive) of the interval
     *            from which all {@link XEvent XEvents} are to be returned - can
     *            be greater than current revision number of the addressed
     *            entity to get all {@link XEvent XEvents} since beginRevision.
     * @return all events that occurred in the entity addressed with 'address'
     *         between beginRevision and endRevision. Contains
     *         {@link XRepositoryEvent}, {@link XModelEvent},
     *         {@link XObjectEvent} and {@link XFieldEvent}. Since 0.1.6:
     *         Contains nulls for in-progress events (if endRev > currentRev)
     */
    @NeverNull
    List<XEvent> getEvents(@NeverNull XAddress address, long beginRevision, long endRevision);
    
    /**
     * @return a {@link Set} containing all XIds of {@link XModel XModels} in
     *         this {@link XydraPersistence}. The models do not necessarily
     *         exist right now, i.e. they might have been deleted already.
     */
    @NeverNull
    Set<XId> getManagedModelIds();
    
    /**
     * @param addressRequest of an {@link XModel} plus a flag whether to include
     *            the tentative revision. The tentative model revision number is
     *            the highest revision number for which an event exists. The
     *            state of the model (exists or not) is accurate.
     * 
     *            The result can change when this method is called multiple
     *            times. This method exists to allow read-your-own write access
     *            to data after a successful command execution.
     * 
     *            The result might be higher than the standard current rev but
     *            in the worst case its just the same.
     * 
     * @return The current revision number of the addressed {@link XModel}. And
     *         if the model currently exists or not. May return null only if not
     *         known.
     */
    @CanBeNull
    ModelRevision getModelRevision(@NeverNull GetWithAddressRequest addressRequest);
    
    /**
     * @param addressRequest of an {@link XModel} plus a flag whether to return
     *            the tentative revision. The tentative model revision number is
     *            the highest revision number for which an event exists. The
     *            tentative snapshot includes all changes up to this point and
     *            may vary for subsequent requests. This for tentative
     *            snapshots, the same revision number can (and often is)
     *            returned for different states.
     * 
     * @return the current snapshot of the addressed {@link XModel} or null if
     *         none found.
     */
    @CanBeNull
    XWritableModel getModelSnapshot(@NeverNull GetWithAddressRequest addressRequest);
    
    /**
     * @param addressRequest of an {@link XObject} plus a flag whether to return
     *            the tentative revision. The tentative object revision number
     *            is the highest revision number for which an event exists. The
     *            tentative snapshot includes all changes up to this point and
     *            may vary for subsequent requests. This for tentative
     *            snapshots, the same revision number can (and often is)
     *            returned for different states.
     * @return the current snapshot of the {@link XObject} addressed with
     *         'address' or null, if object does not exist.
     */
    @CanBeNull
    XWritableObject getObjectSnapshot(@NeverNull GetWithAddressRequest addressRequest);
    
    /**
     * @return the XId of this {@link XydraPersistence}. This helps a client to
     *         differentiate among several {@link XydraPersistence}
     *         implementations, e.g. when synchronising between several of them.
     */
    @NeverNull
    XId getRepositoryId();
    
    /**
     * @param modelId
     * @return true if this persistence has ever managed the given modelId. This
     *         does not imply that the model currently exists.
     */
    boolean hasManagedModel(XId modelId);
    
}

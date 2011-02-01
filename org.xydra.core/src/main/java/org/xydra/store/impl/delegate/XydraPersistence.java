package org.xydra.store.impl.delegate;

import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.store.GetEventsRequest;
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
	long executeCommand(XID actorId, XCommand command);
	
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
	 *         between beginRevision and endRevision.
	 */
	List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision);
	
	/**
	 * @return a {@link Set} containing all XIDs of {@link XModel XModels} in
	 *         this {@link XydraPersistence}.
	 */
	Set<XID> getModelIds();
	
	/**
	 * @param address of an {@link XModel}
	 * @return the current revision number of the addressed {@link XModel}.
	 */
	long getModelRevision(XAddress address);
	
	/**
	 * @param address of an {@link XModel}
	 * @return the current snapshot of the addressed {@link XModel} or null if
	 *         none found.
	 */
	XWritableModel getModelSnapshot(XAddress address);
	
	/**
	 * @param address of an {@link XObject}
	 * @return the current snapshot of the {@link XObject} addressed with
	 *         'address'.
	 */
	XWritableObject getObjectSnapshot(XAddress address);
	
	/**
	 * @return the XID of this {@link XydraPersistence}. This helps a client to
	 *         differentiate among several {@link XydraPersistence}
	 *         implementations, e.g. when synchronising between several ov them.
	 */
	XID getRepositoryId();
	
	/**
	 * @param id
	 * @return true if this persistence contains the given modelId.
	 */
	boolean hasModel(XID modelId);
	
}

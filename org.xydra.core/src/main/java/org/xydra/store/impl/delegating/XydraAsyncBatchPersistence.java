package org.xydra.store.impl.delegating;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;


/**
 * An interface that helps to implement {@link XydraStore}.
 * 
 * Comparison:
 * 
 * <table>
 * <tr>
 * <th>Interface</th>
 * <th>Synchronous?</th>
 * <th>Access Rights</th>
 * <th>Batch operations</th>
 * </tr>
 * <tr>
 * <td>{@link XydraStore}</td>
 * <td>Asynchronous</td>
 * <td>Yes</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>{@link XydraAsyncBatchPersistence}</td>
 * <td>Asynchronous</td>
 * <td>No</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>{@link XydraBlockingPersistence}</td>
 * <td>Synchronous</td>
 * <td>No</td>
 * <td>No</td>
 * </tr>
 * </table>
 * 
 * A variant of the {@link XydraStore} without access rights parameters.
 * 
 * @author voelkel
 */
public interface XydraAsyncBatchPersistence {
	
	/**
	 * @param actorId
	 * @param commands
	 * @param callback
	 */
	void executeCommands(XID actorId, XCommand[] commands, Callback<BatchedResult<Long>[]> callback);
	
	/**
	 * @param actorId
	 * @param commands
	 * @param getEventsRequests
	 * @param callback
	 */
	void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        GetEventsRequest[] getEventsRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback);
	
	/**
	 * @param getEventsRequests
	 * @param callback
	 */
	void getEvents(GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback);
	
	/**
	 * @param callback
	 */
	void getModelIds(Callback<Set<XID>> callback);
	
	/**
	 * @param modelAddresses
	 * @param callback
	 */
	void getModelRevisions(XAddress[] modelAddresses, Callback<BatchedResult<Long>[]> callback);
	
	/**
	 * @param modelAddresses
	 * @param callback
	 */
	void getModelSnapshots(XAddress[] modelAddresses, Callback<BatchedResult<XBaseModel>[]> callback);
	
	/**
	 * @param objectAddresses
	 * @param callback
	 */
	void getObjectSnapshots(XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback);
	
	/**
	 * @param callback
	 */
	void getRepositoryId(Callback<XID> callback);
	
	/**
	 * Delete <em>all</em> data. This method should not be exposed over REST or
	 * other network protocols. This method is intended to be used in unit
	 * tests.
	 */
	void clear();
	
}

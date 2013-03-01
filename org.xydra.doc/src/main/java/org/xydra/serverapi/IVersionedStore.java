package org.xydra.serverapi;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * Supports persistence, commands, transactions, versioning, access rights.
 * 
 * @author voelkel
 * 
 */
public interface IVersionedStore {
	
	/**
	 * @param actor
	 * @param modelAddress
	 * @param rev
	 * @return an immutable {@link IObjectSnapshot} if the given actor has the
	 *         right to see it
	 */
	IObjectSnapshot getObjectSnapshot(XId actor, XAddress modelAddress, long rev);
	
	/**
	 * @param actor
	 * @param objectAddress
	 * @param command which may also be a transaction
	 */
	void executeObjectCommand(XId actor, XAddress objectAddress, ICommand command);
	
	/**
	 * @param actor
	 * @param address
	 * @param rev
	 * @return all events that happened after rev on the XEntity with the given
	 *         address
	 */
	List<IEvent> getEventsSince(XId actor, XAddress address, long rev);
	
}

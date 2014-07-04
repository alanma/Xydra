package org.xydra.core.model.impl.memory.sync;

import java.util.List;

import org.xydra.base.change.XEvent;
import org.xydra.index.query.Pair;


/**
 * @author xamde
 */
public interface IEventMapper {
	
	/**
	 * Maps server events to local events and gives back the result.
	 * 
	 * @param localSyncLog
	 * @param remoteEvents Array of events from the server
	 * @return all mapped and non-mapped events
	 */
	IMappingResult mapEvents(ISyncLog localSyncLog, XEvent[] remoteEvents);
	
	/**
	 * Contains the results of the mapping.
	 * 
	 * @author Andi_Ka
	 * 
	 */
	public static interface IMappingResult {
		
		/**
		 * @return List of true remote events = not seen yet on client
		 */
		List<XEvent> getUnmappedRemoteEvents();
		
		/**
		 * @return List of local events that were not mapped = not executed on
		 *         server
		 */
		List<ISyncLogEntry> getUnmappedLocalEvents();
		
		/**
		 * @return Mapping between local changes and remove events = events
		 *         originated locally and successfully executed on server
		 */
		List<Pair<XEvent,ISyncLogEntry>> getMapped();
		
	}
	
}

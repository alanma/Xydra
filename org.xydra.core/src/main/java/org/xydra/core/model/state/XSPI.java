package org.xydra.core.model.state;

import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Service Provider Interface (SPI)
 * 
 * @author voelkel
 */
public class XSPI {
	
	final static private Logger log = LoggerFactory.getLogger(XSPI.class);
	
	private static XStateStore memoryStateStore_;
	
	private static XStateStore stateStore_;
	
	/**
	 * Note: This method is called internally and should normally not be called
	 * by XModel API users.
	 * 
	 * @return the configured {@link XStateStore} used for creating the inner
	 *         state-entities.
	 */
	public static XStateStore getMemoryStateStore() {
		if(memoryStateStore_ == null) {
			memoryStateStore_ = new MemoryStateStore();
		}
		return memoryStateStore_;
	}
	
	/**
	 * Note: This method is called internally and should normally not be called
	 * by XModel API users.
	 * 
	 * @return the configured {@link XStateStore} used for creating the inner
	 *         state-entities.
	 */
	public static XStateStore getStateStore() {
		if(stateStore_ == null) {
			
			log
			        .warn("No XStateStore has been set, defaulting to in-memory persistence. YOUR CHANGES WILL NOT BE PERSISTED ON DISK/CLOUD.");
			
			stateStore_ = getMemoryStateStore();
		}
		return stateStore_;
	}
	
	/**
	 * Set the {@link XStateStore} to be used by the backend. Must be set prior
	 * to calling {@link #getStateStore()}.
	 * 
	 * @param stateStore The new state backend to use.
	 */
	public static void setStateStore(XStateStore stateStore) {
		stateStore_ = stateStore;
	}
}

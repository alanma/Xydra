package org.xydra.store.impl.gae.ng;

import org.xydra.base.XAddress;
import org.xydra.base.rmof.XReadableModel;


/**
 * Stores redundant snapshots. Only used by executeCommands method to decide
 * quicker, if a change is legal or not,
 */
public interface ITentativeSnapshotManager {
	
	public XReadableModel getModelSnapshot();
	
	public TentativeObjectSnapshot getTentativeObjectSnapshot(XAddress objectAddress);
	
	public void saveTentativeObjectSnapshot(TentativeObjectSnapshot tentativeObjectSnapshot);
	
	public long getModelRevision();
	
}

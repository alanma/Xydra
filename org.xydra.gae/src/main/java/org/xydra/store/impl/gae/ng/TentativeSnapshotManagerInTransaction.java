package org.xydra.store.impl.gae.ng;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.sharedutils.XyAssert;


/**
 * Caching facade.
 * 
 * This construct is required during transactions to give all participating
 * methods a view on the in-progress changes without having them actually
 * committed to any persistence.
 * 
 * 
 * @author xamde
 */
public class TentativeSnapshotManagerInTransaction implements ITentativeSnapshotManager {
	
	/** also caches nulls when reading and there was no TOS */
	private Map<XID,TentativeObjectSnapshot> cachedObjects = new HashMap<XID,TentativeObjectSnapshot>();
	
	private ITentativeSnapshotManager tsm;
	
	private RevisionManager txnLocalRevManager;
	
	public TentativeSnapshotManagerInTransaction(ITentativeSnapshotManager baseManager,
	        RevisionManager txnLocalRevManager) {
		super();
		this.tsm = baseManager;
		this.txnLocalRevManager = txnLocalRevManager;
	}
	
	public void clearCaches() {
		this.cachedObjects.clear();
	}
	
	@Override
	public XReadableModel getModelSnapshot(GaeModelRevInfo info) {
		return this.tsm.getModelSnapshot(info);
	}
	
	@Override
	public TentativeObjectSnapshot getTentativeObjectSnapshot(GaeModelRevInfo info,
	        XAddress objectAddress) {
		TentativeObjectSnapshot tos = this.cachedObjects.get(objectAddress.getObject());
		if(tos == null) {
			tos = this.tsm.getTentativeObjectSnapshot(info, objectAddress);
			// unlink
			tos = tos.copy();
			this.cachedObjects.put(objectAddress.getObject(), tos);
		}
		return tos;
	}
	
	@Override
	public void saveTentativeObjectSnapshot(org.xydra.store.impl.gae.ng.TentativeObjectSnapshot tos) {
		XyAssert.xyAssert(tos != null);
		assert tos != null;
		this.cachedObjects.put(tos.getId(), tos);
	}
	
	public void saveTentativeObjectSnapshots() {
		for(Entry<XID,TentativeObjectSnapshot> e : this.cachedObjects.entrySet()) {
			TentativeObjectSnapshot tos = e.getValue();
			if(tos != null) {
				this.tsm.saveTentativeObjectSnapshot(e.getValue());
			}
		}
	}
	
	@Override
	public long getModelRevision(GaeModelRevInfo info) {
		return this.tsm.getModelRevision(info);
	}
	
	@Override
	public GaeModelRevInfo getInfo() {
		return this.txnLocalRevManager.getInfo();
	}
	
}

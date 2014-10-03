/**
 * 
 */
package org.xydra.store.impl.gae.execute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.gae.changes.Utils;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;

/**
 * Internal helper class used by {@link IGaeChangesService} to access the
 * current model state.
 * 
 * Shared functionality between {@link InternalGaeModel} and
 * {@link InternalGaeObject}.
 * 
 * @author dscharrer
 * 
 * @param <C>
 *            type of child entities
 */
abstract class InternalGaeContainerXEntity<C> extends InternalGaeXEntity {

	private final IGaeChangesService changesService;
	private final Map<XId, C> cachedChildren = new HashMap<XId, C>();
	private final XAddress addr;
	private Set<XId> cachedIds;
	private final Set<XId> cachedMisses = new HashSet<XId>();
	private final GaeLocks locks;
	private final long rev;

	protected InternalGaeContainerXEntity(IGaeChangesService changesService, XAddress addr,
			long rev, GaeLocks locks) {
		assert rev >= 0
				|| (rev == XEvent.REVISION_NOT_AVAILABLE && addr.getAddressedType() == XType.XOBJECT) : "rev="
				+ rev + " adressedType=" + addr.getAddressedType();
		this.changesService = changesService;
		assert addr.getAddressedType() == XType.XMODEL || addr.getAddressedType() == XType.XOBJECT;
		assert locks.canRead(addr);
		this.addr = addr;
		this.locks = locks;
		this.rev = rev;
	}

	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public XAddress getAddress() {
		return this.addr;
	}

	protected abstract XAddress resolveChild(XAddress addr, XId childId);

	protected abstract C loadChild(XAddress childAddr, SEntity childEntity);

	public C getChild(XId fieldId) {

		// don't look in this.cachedIds, as this might contain outdated
		// information due to being based on GAE queries
		if (this.cachedMisses.contains(fieldId)) {
			return null;
		}

		C gf = this.cachedChildren.get(fieldId);
		if (gf != null) {
			return gf;
		}

		XAddress childAddr = resolveChild(this.addr, fieldId);
		assert this.locks.canRead(childAddr);

		SEntity e = XGae.get().datastore().sync()
				.getEntity(KeyStructure.createEntityKey(childAddr));
		if (e == null) {
			this.cachedMisses.add(fieldId);
			return null;
		}

		gf = loadChild(childAddr, e);
		this.cachedChildren.put(fieldId, gf);
		return gf;
	}

	public boolean hasChild(XId fieldId) {
		return this.cachedIds != null ? this.cachedIds.contains(fieldId)
				: getChild(fieldId) != null;
	}

	public Iterator<XId> iterator() {

		if (this.cachedIds == null) {

			assert this.locks.canWrite(this.addr);

			this.cachedIds = Utils.findChildren(this.addr);
		}
		return this.cachedIds.iterator();
	}

	protected GaeLocks getLocks() {
		return this.locks;
	}

	protected IGaeChangesService getChangesService() {
		return this.changesService;
	}

	public long getRevisionNumber() {
		return this.rev;
	}

}

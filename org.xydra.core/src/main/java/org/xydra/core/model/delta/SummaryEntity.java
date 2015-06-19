package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;

/**
 * Bases class for MOFV.
 * 
 * Role: Determine the state at the end of a transaction: Which elements have
 * been removed, changed (= children added/removed), or added?
 * 
 * @author xamde
 */
abstract class SummaryEntity implements IHasXId {

	protected Change change = new Change();

	private XId id;

	/** for debugging */
	protected List<Long> appliedEvents = new ArrayList<Long>();

	public SummaryEntity(XId id) {
		this.id = id;
	}

	/**
	 * @param ae
	 */
	public void apply(XAtomicEvent ae) {
		this.change.apply(ae.getChangeType());
		this.change.lastRev = ae.getRevisionNumber();
		this.appliedEvents.add(ae.getRevisionNumber());
	}

	/**
	 * @return
	 */
	public AtomicChangeType getAtomichChangeType() {
		return this.change.getAtomicChangeType();
	}

	/**
	 * @return
	 */
	public Change getChange() {
		return this.change;
	}

	@Override
	public XId getId() {
		return this.id;
	}

}
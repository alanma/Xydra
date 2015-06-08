package org.xydra.core.model.delta;

import org.xydra.base.change.XAtomicEvent;

abstract class SummaryEntity {
	protected Change change = new Change();

	public void apply(XAtomicEvent ae) {
		this.change.apply(ae.getChangeType());
	}

}
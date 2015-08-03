package org.xydra.oo.runtime.client;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.shared.SharedProxy;

public class GwtXydraMapped implements IHasXId {

	/** object-oriented proxy */
	protected SharedProxy oop;

	/** For GWT.create only */
	public GwtXydraMapped() {
	}

	public void init(final XWritableModel model, final XId id) {
		this.oop = new SharedProxy(model, id);
	}

	public GwtXydraMapped(final XWritableModel model, final XId id) {
		this.oop = new SharedProxy(model, id);
	}

	@Override
	public XId getId() {
		return this.oop.getId();
	}

}

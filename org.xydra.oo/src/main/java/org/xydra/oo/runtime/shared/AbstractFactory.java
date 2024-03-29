package org.xydra.oo.runtime.shared;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;

/**
 * Factory inherited by JavaFactory and GwtFactory
 *
 * @author xamde
 */
@RunsInGWT(true)
public abstract class AbstractFactory {

	protected XWritableModel model;

	public AbstractFactory(final XWritableModel model) {
		this.model = model;
	}

	protected void createXObject(final XId id) {
		this.model.createObject(id);
	}

	protected boolean hasXObject(final XId id) {
		return this.model.hasObject(id);
	}

}

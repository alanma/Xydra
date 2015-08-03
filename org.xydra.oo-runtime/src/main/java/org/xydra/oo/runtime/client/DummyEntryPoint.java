package org.xydra.oo.runtime.client;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.core.X;

import com.google.gwt.core.client.EntryPoint;

/**
 * This package allows to test all the other packages for GWT-compatibility
 * without requiring each of them to have a separate entry point.
 *
 * @author dscharrer
 *
 */
public class DummyEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
		// TODO do something
		final XId actorId = Base.toId("gwt-moduleload");
		X.createMemoryRepository(actorId).createModel(Base.createUniqueId())
				.createObject(Base.toId("hello world"));
	}

}

package org.xydra.core.gwt;

import org.xydra.core.X;
import org.xydra.core.XX;

import com.google.gwt.core.client.EntryPoint;


/**
 * This package allows to test all the other packages for GWT-compatibility
 * without requiring each of them to have a separate entry point.
 * 
 * @author dscharrer
 * 
 */
public class DummyEntryPoint implements EntryPoint {
	
	public void onModuleLoad() {
		X.createMemoryRepository().createModel(null, XX.createUniqueID()).createObject(null,
		        XX.toId("hello world"));
	}
	
}

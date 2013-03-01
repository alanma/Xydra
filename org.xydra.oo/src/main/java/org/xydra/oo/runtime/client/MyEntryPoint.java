package org.xydra.oo.runtime.client;

import org.xydra.base.X;
import org.xydra.base.XId;
import org.xydra.base.XX;

import com.google.gwt.core.client.EntryPoint;


/**
 * This package allows to test all the other packages for GWT-compatibility
 * without requiring each of them to have a separate entry point.
 * 
 * @author dscharrer
 * 
 */
public class MyEntryPoint implements EntryPoint {
    
    @Override
    public void onModuleLoad() {
        // TODO do something
        XId actorId = XX.toId("gwt-moduleload");
        X.createMemoryRepository(actorId).createModel(XX.createUniqueId())
                .createObject(XX.toId("hello world"));
    }
    
}

package org.xydra.oo.testgen.alltypes.client;

import org.xydra.base.XId;
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
    
    @Override
    public void onModuleLoad() {
        // TODO do something
        XId actorId = XX.toId("gwt-moduleload");
        X.createMemoryRepository(actorId).createModel(XX.createUniqueId())
                .createObject(XX.toId("hello world"));
    }
    
}

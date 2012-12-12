package org.xydra.webadmin.gwt.client;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;


public class XyAdminEntryPoint implements EntryPoint {
    
    private static Logger log = LoggerFactory.getLogger(XyAdminEntryPoint.class);
    
    @Override
    public void onModuleLoad() {
        log.info("Starting XyAdminEntryPoint");
        
        // Document.get().getBody().setInnerHTML("");
        
        Panel main = RootPanel.get("main");
        main.add(new XyAdmin());
        
        Panel loading = RootPanel.get("loading");
        loading.setVisible(false);
    }
    
}

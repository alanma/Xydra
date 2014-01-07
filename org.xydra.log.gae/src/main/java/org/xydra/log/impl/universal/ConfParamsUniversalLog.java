package org.xydra.log.impl.universal;

import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;


public class ConfParamsUniversalLog implements IConfigProvider {
    
    @ConfDoc("Result of com.google.gwt.core.shared.GWT.isScript()")
    @ConfType(Boolean.class)
    public static final String GWT_IN_PRODUCTION = "gwtInProduction";
    
    @ConfDoc("com.google.appengine.api.utils.SystemProperty.environment equals inProduction")
    @ConfType(Boolean.class)
    public static final String GAE_IN_PRODUCTION = "gaeInProduction";
    
    @Override
    public void configure(IConfig conf) {
        conf.setDefault(GWT_IN_PRODUCTION, false, true);
        conf.setDefault(GAE_IN_PRODUCTION, false, true);
    }
    
}

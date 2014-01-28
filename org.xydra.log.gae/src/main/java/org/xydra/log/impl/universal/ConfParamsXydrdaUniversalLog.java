package org.xydra.log.impl.universal;

import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;
import org.xydra.log.spi.ILoggerFactorySPI;


public class ConfParamsXydrdaUniversalLog implements IConfigProvider {
    
    @ConfDoc("Result of com.google.gwt.core.shared.GWT.isScript()")
    @ConfType(Boolean.class)
    public static final String GWT_IN_PRODUCTION = "gwtInProduction";
    
    @ConfDoc("com.google.appengine.api.utils.SystemProperty.environment equals inProduction")
    @ConfType(Boolean.class)
    public static final String GAE_IN_PRODUCTION = "gaeInProduction";
    
    @ConfDoc("If defined, this ILoggerFactorySPI should be used")
    @ConfType(ILoggerFactorySPI.class)
    public static final String LOGGER_FACTORY_SPI = ILoggerFactorySPI.class.getName();
    
    @Override
    public void configure(IConfig conf) {
        conf.setDefault(GWT_IN_PRODUCTION, false, true);
        conf.setDefault(GAE_IN_PRODUCTION, false, true);
    }
    
}

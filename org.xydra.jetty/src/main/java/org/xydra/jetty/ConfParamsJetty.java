package org.xydra.jetty;

import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;


public class ConfParamsJetty implements IConfigProvider {
    
    @ConfType(int.class)
    @ConfDoc("Port on which Jetty webserver runs")
    public static final String PORT = "jetty-port";
    
    @ConfType(String.class)
    @ConfDoc("Web-app path, needs only to be set if multiple webapps run on the same server")
    public static final String CONTEXT_PATH = "jetty-contextpath";
    
    @ConfType(String.class)
    @ConfDoc("suitable for putting in a File() object. Jetty will look for docRoot + 'WEB-INF/web.xml'")
    public static final String DOC_ROOT = "jetty-docroot";
    
    /** TODO this is really a Restless setting and should migrate there */
    public static final String USE_DEFAULT_SERVLET = "restless-useDefaultServlet";
    
    @Override
    public void configure(IConfig conf) {
        conf.setDefault(PORT, 8888, true);
        conf.setDefault(CONTEXT_PATH, "", true);
        // set no default for DOC_ROOT
    }
}

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
    @ConfDoc("Web-app path, needs only to be set if multiple webapps run on the same server. Must start with '/'.")
    public static final String CONTEXT_PATH = "jetty-contextpath";
    
    @ConfType(String.class)
    @ConfDoc("URL syntax or file path. Jetty will look for webapp in docRoot + 'WEB-INF/web.xml'")
    public static final String DOC_ROOT = "jetty-docroot";
    
    /** TODO this is really a Restless setting and should migrate there */
    public static final String USE_DEFAULT_SERVLET = "restless-useDefaultServlet";
    
    /**
     * A marker file with the given name is used to auto-discover web-root on
     * the classpath
     */
    public static String _WEBROOT_MARKER_FILE = "webroot.marker";
    
    @Override
    public void configure(IConfig conf) {
        conf.setDefault(PORT, 8888, true);
        conf.setDefault(CONTEXT_PATH, "/", true);
        // set no default for DOC_ROOT
    }
}

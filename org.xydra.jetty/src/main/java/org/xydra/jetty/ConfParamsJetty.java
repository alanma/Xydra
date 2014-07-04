package org.xydra.jetty;

import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;


public class ConfParamsJetty implements IConfigProvider {
    
    @ConfType(int.class)
    public static final String PORT = "jetty.port";
    
    @ConfType(String.class)
    public static final String CONTEXT_PATH = "jetty.contextpath";
    
    @ConfType(String.class)
    @ConfDoc("suitable for putting in a File() object")
    public static final String DOC_ROOT = "jetty.docroot";
    
    /** this is really a Restless setting and should migrate there */
    public static final String USE_DEFAULT_SERVLET = "restless.useDefaultServlet";
    
    @Override
    public void configure(IConfig conf) {
        conf.setDefault(PORT, 8888, true);
        conf.setDefault(CONTEXT_PATH, "", true);
        // set no default for DOC_ROOT
    }
}

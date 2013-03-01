package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XId;
import org.xydra.gae.AboutAppEngine;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;


public class Utils {
    
    private static Map<XId,XydraPersistence> pmap = new HashMap<XId,XydraPersistence>();
    
    public static synchronized XydraPersistence createPersistence(XId repoId) {
        XydraPersistence p = pmap.get(repoId);
        if(p == null) {
            p = new GaePersistence(repoId);
            pmap.put(repoId, p);
        }
        return p;
    }
    
    /**
     * Suggest an appropriate filename suffixed with the current time.
     * 
     * @param name ..
     * @return name + '-yyyy-MM-dd-HH-mm-ss' (as of now)
     */
    public static String filenameOfNow(String name) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return name + "-" + sdf.format(now);
    }
    
    public static Writer startPage(HttpServletResponse res, String page, String part)
            throws IOException {
        Writer w = HtmlUtils.startHtmlPage(res, AppConstants.NAME + ":: " + page + " " + part,
                new HeadLinkStyle(AppConstants.CSS_DEBUG_PATH));
        w.write("<div class='xydebug' >");
        w.write(HtmlUtils.link(".", "Back"));
        w.write("Instance: " + AboutAppEngine.getInstanceId() + " Thread: "
                + AboutAppEngine.getThreadInfo() + "<br/>\n");
        w.write("<h3>" + page + " " + part + "</h3>\n");
        return w;
    }
    
    public static void endPage(Writer w) throws IOException {
        w.write("</div>");
        HtmlUtils.endHtmlPage(w);
    }
    
}

package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.store.impl.delegate.XydraPersistence;


public class ObjectResource {
	
	public static final Logger log = LoggerFactory.getLogger(ObjectResource.class);
	
	public static void restless(Restless restless, String prefix) {
		
		restless.addMethod(prefix + "/{repoId}/{modelId}/{objectId}/", "GET", ObjectResource.class,
		        "index", true,

		        new RestlessParameter("repoId"), new RestlessParameter("modelId"),
		        new RestlessParameter("objectId"), new RestlessParameter("style", "default")

		);
		
	}
	
	public static void index(String repoIdStr, String modelIdStr, String objectIdStr, String style,
	        HttpServletResponse res) throws IOException {
		XAddress objectAddress = XX.toAddress(XX.toId(repoIdStr), XX.toId(modelIdStr),
		        XX.toId(objectIdStr), null);
		Writer w = Utils.writeHeader(res, "Object", objectAddress);
		render(w, objectAddress, style);
		w.flush();
		w.close();
	}
	
	public static void render(Writer w, XAddress objectAddress, String style) throws IOException {
		XydraPersistence p = Utils.getPersistence(objectAddress.getRepository());
		XWritableObject obj = p.getObjectSnapshot(objectAddress);
		w.write("rev=" + obj.getRevisionNumber() + "<br/>\n");
		w.write(XydraHtmlUtils.toHtml(obj));
		w.flush();
	}
	
}

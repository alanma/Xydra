package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class ObjectResource {

	public static final Logger log = LoggerFactory.getLogger(ObjectResource.class);

	public static final String PAGE_NAME = "Object";

	public static void restless(final Restless restless, final String prefix) {

		restless.addMethod(prefix + "/{repoId}/{modelId}/{objectId}/", "GET", ObjectResource.class,
				"index", true,

				new RestlessParameter("repoId"), new RestlessParameter("modelId"),
				new RestlessParameter("objectId"), new RestlessParameter("style", "default")

		);

	}

	public static void index(final String repoIdStr, final String modelIdStr, final String objectIdStr, final String style,
			final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final XAddress objectAddress = Base.toAddress(Base.toId(repoIdStr), Base.toId(modelIdStr),
				Base.toId(objectIdStr), null);

		final Writer w = Utils.startPage(res, PAGE_NAME, objectAddress.toString());
		render(w, objectAddress, style);
		Utils.endPage(w);
	}

	public static void render(final Writer w, final XAddress objectAddress, final String style) throws IOException {
		final XydraPersistence p = Utils.createPersistence(objectAddress.getRepository());
		final XWritableObject obj = p.getObjectSnapshot(new GetWithAddressRequest(objectAddress,
				ModelResource.INCLUDE_TENTATIVE));
		if (obj != null) {
			w.write("rev=" + obj.getRevisionNumber() + "<br/>\n");
			w.write(XydraHtmlUtils.toHtml(obj));
		}
		w.flush();
	}

}

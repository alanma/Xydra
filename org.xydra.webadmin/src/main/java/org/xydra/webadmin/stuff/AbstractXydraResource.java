package org.xydra.webadmin.stuff;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XValue;
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeTestfixer;


/**
 * Without using templates.
 * 
 * @author voelkel
 */
public abstract class AbstractXydraResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractXydraResource.class);
	
	protected static final XID ACTOR = XX.toId("_XydraResource");
	
	protected abstract XWritableRepository repository();
	
	protected abstract XWritableModel model(XID modelXid);
	
	/**
	 * Admin only. Dump Xydra content to HTML.
	 * 
	 * /admin/xydra/repository
	 * 
	 * /admin/xydra/model?model={id}
	 * 
	 * /admin/xydra/object?model={id}&object={id}
	 * 
	 * /admin/xydra/objectByAddress?objectAddress={address}
	 * 
	 * @param restless ..
	 * @param prefix ..
	 */
	public static void configureRestless(Restless restless, String prefix, Class<?> subClass) {
		restless.addMethod("/xydra/repository", "GET", subClass, "getRepository", true);
		
		restless.addMethod("/xydra/model", "GET", subClass, "getModel", true,
		
		new RestlessParameter("model", null)
		
		);
		
		restless.addMethod("/xydra/object", "GET", subClass, "getObject", true,
		
		new RestlessParameter("model", null),
		
		new RestlessParameter("object", null)
		
		);
		
		restless.addMethod("/xydra/objectByAddress", "GET", subClass, "getObjectByAddress", true,
		
		new RestlessParameter("objectAddress", null)
		
		);
		
	}
	
	public void getRepository(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Repository");
		generalInfo(w);
		w.write("<ol>");
		for(XID modelId : repository()) {
			w.write("<li>");
			w.write(HtmlUtils.link("/admin/xydra/model?model=" + modelId, modelId.toString()));
			w.write("</li>");
		}
		w.write("</ol>");
		
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
		w.close();
	}
	
	public void getModel(HttpServletRequest req, String modelId, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = HtmlUtils.startHtmlPage(res, "Model " + modelId);
		assert w != null;
		generalInfo(w);
		XID modelXid = X.getIDProvider().fromString(modelId);
		XWritableModel model = model(modelXid);
		if(model != null) {
			writeModel(w, model);
		} else {
			w.write("no model with id '" + modelId + "'");
		}
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
		w.close();
	}
	
	public void getObject(HttpServletRequest req, String modelId, String objectId,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = HtmlUtils.startHtmlPage(res, "Object " + modelId);
		generalInfo(w);
		XID modelXid = X.getIDProvider().fromString(modelId);
		XWritableModel model = model(modelXid);
		XID objectXid = X.getIDProvider().fromString(objectId);
		XWritableObject object = model.getObject(objectXid);
		writeObject(w, object);
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
		w.close();
	}
	
	public void getObjectByAddress(HttpServletRequest req, String objectAddress,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = HtmlUtils.startHtmlPage(res, "Object " + objectAddress);
		generalInfo(w);
		XAddress objectXaddress = X.getIDProvider().fromAddress(objectAddress);
		XWritableModel model = model(objectXaddress.getModel());
		XWritableObject object = model.getObject(objectXaddress.getObject());
		writeObject(w, object);
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
		w.close();
	}
	
	private static void generalInfo(Writer w) throws IOException {
		w.write("Instance: " + XydraRuntime.getInstanceId() + "; Thread: "
		        + AboutAppEngine.getThreadInfo() + "<br />\n");
	}
	
	public static void writeModel(Writer w, XWritableModel model) throws IOException {
		assert w != null;
		assert model != null;
		w.write("<h3>Model " + model.getID() + " [" + model.getRevisionNumber() + "]</h3>");
		w.flush();
		for(XID objectId : model) {
			writeObject(w, model.getObject(objectId));
			w.flush();
		}
	}
	
	/**
	 * @param w ..
	 * @param object may be null
	 * @throws IOException ...
	 */
	public static void writeObject(Writer w, XWritableObject object) throws IOException {
		if(object == null) {
			w.write("<p>Object is null</p>");
			return;
		}
		// else
		w.write("<p>Object " + object.getID() + " [" + object.getRevisionNumber() + "]</p>");
		w.write("<ol>");
		for(XID fieldId : object) {
			w.write("<li>");
			writeField(w, object.getField(fieldId));
			w.write("</li>");
		}
		w.write("</ol>");
	}
	
	public static void writeField(Writer w, XWritableField field) throws IOException {
		XValue v = field.getValue();
		w.write("<span>Field " + field.getID() + " [" + field.getRevisionNumber() + "] = " + v);
		// linking
		w.write(" " + HtmlUtils.link("/admin/xydra/model?model=" + v, "model ID"));
		w.write(" "
		        + HtmlUtils.link("/admin/xydra/object?model=" + field.getAddress().getModel()
		                + "&object=" + v, "object ID"));
		w.write(" " + HtmlUtils.link("/admin/xydra/objectByAddress?address=" + v, "address"));
		w.write("</span>");
	}
	
}

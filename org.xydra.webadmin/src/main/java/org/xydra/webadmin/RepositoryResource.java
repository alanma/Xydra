package org.xydra.webadmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.Clock;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.webadmin.ModelResource.SetStateResult;


public class RepositoryResource {
	
	public static final Logger log = LoggerFactory.getLogger(RepositoryResource.class);
	
	public static void restless(Restless restless, String prefix) {
		
		restless.addMethod(prefix + "/{repoId}/", "GET", RepositoryResource.class, "index", true,
		        new RestlessParameter("repoId"), new RestlessParameter("style", RStyle.html.name()));
		
		restless.addMethod(prefix + "/{repoId}/", "POST", RepositoryResource.class, "update", true,
		        new RestlessParameter("repoId"));
	}
	
	public static enum RStyle {
		html, xmlzip
	}
	
	/**
	 * @param repoIdStr never null
	 * @param styleStr see {@link RStyle}
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void index(String repoIdStr, String styleStr, HttpServletResponse res)
	        throws IOException {
		Clock c = new Clock().start();
		XAddress repoAddress = XX.resolveRepository(XX.toId(repoIdStr));
		RStyle style = RStyle.valueOf(styleStr);
		XID repoId = XX.toId(repoIdStr);
		XydraPersistence p = Utils.getPersistence(repoId);
		if(style == RStyle.xmlzip) {
			String archivename = Utils.filenameOfNow("repo-" + repoId);
			ZipOutputStream zos = Utils.toZipFileDownload(res, archivename);
			for(XID modelId : p.getModelIds()) {
				XAddress modelAddress = XX.resolveModel(repoId, modelId);
				XWritableModel model = p.getModelSnapshot(modelAddress);
				ModelResource.writeToZipstream(model, zos, MStyle.xml);
			}
			zos.finish();
		} else {
			// style HTML
			Writer w = Utils.writeHeader(res, "Repo", repoAddress);
			w.write(HtmlUtils.link(link(repoId) + "?style=" + RStyle.xmlzip.name(),
			        "Download as xml.zip") + "<br/>\n");
			
			// upload form
			w.write(HtmlUtils.form(METHOD.POST, link(repoId)).withInputFile("backupfile")
			        .withInputSubmit("Upload and set as current state").toString());
			
			for(XID modelId : p.getModelIds()) {
				w.write("<h2>Model: " + modelId + "</h2>\n");
				w.flush();
				XAddress modelAddress = XX.toAddress(repoId, modelId, null, null);
				ModelResource.render(w, modelAddress, MStyle.link);
			}
			w.flush();
			w.close();
		}
		log.info(c.stop("index").getStats());
	}
	
	public static void update(String repoIdStr, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		Clock c = new Clock().start();
		XID repoId = XX.toId(repoIdStr);
		
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Webadmin :: Restore Repository",
		        new HeadLinkStyle("/s/xyadmin.css"));
		w.write("Processing upload...</br>");
		w.flush();
		
		InputStream fis = Utils.getMultiPartContentFileAsInputStream(w, req);
		assert fis != null;
		
		ZipInputStream zis = new ZipInputStream(fis);
		w.write("... open zip stream ...</br>");
		w.flush();
		
		XReadableModel model;
		int modelExisted = 0;
		int modelsWithChanges = 0;
		int restored = 0;
		while((model = ModelResource.readZippedModel(zis)) != null) {
			c.stopAndStart("parsed-" + model.getID());
			w.write("... parsed model '" + model.getAddress() + "' [" + model.getRevisionNumber()
			        + "] ...</br>");
			w.flush();
			
			SetStateResult result = ModelResource.setStateFrom(repoId, model);
			log.info("" + result);
			c.stopAndStart("applied-" + model.getID());
			modelExisted += result.modelExisted ? 1 : 0;
			modelsWithChanges += result.changes ? 1 : 0;
			restored++;
			log.debug(model.getAddress() + " " + c.getStats());
			w.write("... applied to server repository " + result + "</br>");
			w.flush();
		}
		w.write("... Done</br>");
		String stats = "Restored: " + restored + ", existed before: " + modelExisted
		        + ", noChangesIn:" + modelsWithChanges;
		w.write(stats + "</br>");
		w.flush();
		w.close();
		log.info("Stats: " + stats + " " + c.getStats());
	}
	
	public static String link(XID repoId) {
		return "/admin" + WebadminResource.XYADMIN + "/" + repoId;
	}
	
}

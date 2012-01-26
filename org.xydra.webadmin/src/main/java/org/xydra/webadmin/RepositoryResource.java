package org.xydra.webadmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.util.Clock;
import org.xydra.core.util.ConfigUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.webadmin.ModelResource.SetStateResult;


/**
 * Can list all models in a repo; provides import/export for a whole model.
 * 
 * @author xamde
 */
public class RepositoryResource {
	
	public static final Logger log = LoggerFactory.getLogger(RepositoryResource.class);
	
	public static void restless(Restless restless, String prefix) {
		
		restless.addMethod(prefix + "/{repoId}/", "GET", RepositoryResource.class, "index", true,
		        new RestlessParameter("repoId"),
		        new RestlessParameter("style", RStyle.html.name()),

		        new RestlessParameter("useTaskQueue", "false"),

		        new RestlessParameter("cacheInInstance", "true"),

		        new RestlessParameter("cacheInMemcache", "false"),

		        new RestlessParameter("cacheInDatastore", "false")

		);
		
		// cacheInInstance=true&cacheInMemcache=false&cacheInDatastore=true
		
		restless.addMethod(prefix + "/{repoId}/", "POST", RepositoryResource.class, "update", true,
		        new RestlessParameter("repoId"));
	}
	
	public static enum RStyle {
		html, htmlrevs, xmlzip, xmldump
	}
	
	/**
	 * @param repoIdStr never null
	 * @param styleStr see {@link RStyle}
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void index(String repoIdStr, String styleStr, String useTaskQueueStr,
	        String cacheInInstanceStr, String cacheInMemcacheStr, String cacheInDatastoreStr,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.startRequest();
		log.trace("logtest: trace");
		log.debug("logtest: debug");
		log.info("logtest: info");
		
		boolean useTaskQueue = ConfigUtils.isTrue(useTaskQueueStr);
		boolean cacheInInstance = ConfigUtils.isTrue(cacheInInstanceStr);
		boolean cacheInMemcache = ConfigUtils.isTrue(cacheInMemcacheStr);
		boolean cacheInDatastore = ConfigUtils.isTrue(cacheInDatastoreStr);
		StorageOptions storeOpts = StorageOptions.create(cacheInInstance, cacheInMemcache,
		        cacheInDatastore);
		
		Clock c = new Clock().start();
		XAddress repoAddress = XX.resolveRepository(XX.toId(repoIdStr));
		RStyle style = RStyle.valueOf(styleStr);
		XID repoId = XX.toId(repoIdStr);
		XydraPersistence p = Utils.getPersistence(repoId);
		if(style == RStyle.xmlzip || style == RStyle.xmldump) {
			List<XID> modelIdList = new ArrayList<XID>(p.getManagedModelIds());
			// TODO we can use paging here to split work; BUT number of models
			// might change.
			Collections.sort(modelIdList);
			boolean allUpToDate = SerialisationCache.updateAllModels(repoId, modelIdList,
			        MStyle.xml, false, useTaskQueue, cacheInInstance, cacheInMemcache,
			        cacheInDatastore);
			if(allUpToDate) {
				log.info("All model serialisations are up-to-date, generating zip");
				String archivename = Utils.filenameOfNow("repo-" + repoId);
				if(style == RStyle.xmlzip) {
					ZipOutputStream zos = Utils.toZipFileDownload(res, archivename);
					for(XID modelId : modelIdList) {
						XAddress modelAddress = XX.resolveModel(repoId, modelId);
						String serialisation = SerialisationCache.getSerialisation(modelAddress,
						        storeOpts);
						ModelResource.writeToZipstreamDirectly(modelId, MStyle.xml, serialisation,
						        zos);
					}
					zos.finish();
				} else {
					Writer w = HtmlUtils.startHtmlPage(res, "Xyadmin XML dump of repo " + repoId);
					for(XID modelId : modelIdList) {
						XAddress modelAddress = XX.resolveModel(repoId, modelId);
						String serialisation = SerialisationCache.getSerialisation(modelAddress,
						        storeOpts);
						w.write(serialisation);
					}
				}
			} else {
				Writer w = HtmlUtils.startHtmlPage(res, "Xyadmin Repo " + repoId);
				w.write("Generating serialisations took too long. Watch task queue to finish and consider using different caching params.<br/>\n");
				w.write(HtmlUtils.form(METHOD.GET, "").withHiddenInputText("", repoIdStr)
				        .withHiddenInputText("style", styleStr)
				        .withInputText("useTaskQueue", useTaskQueueStr)
				        .withInputText("cacheInInstance", cacheInInstanceStr)
				        .withInputText("cacheInMemcache", cacheInMemcacheStr)
				        .withInputText("cacheInDatastore", cacheInDatastoreStr)
				        .withInputSubmit("Retry").toString());
			}
		} else {
			// style HTML
			Writer w = Utils.writeHeader(res, "Repo", repoAddress);
			w.write(

			HtmlUtils.link(link(repoId) + "?style=" + RStyle.htmlrevs.name(), "With Revs")
			        
			        + " | "
			        + HtmlUtils.link(link(repoId) + "?style=" + RStyle.xmlzip.name(),
			                "Download as xml.zip")
			        
			        + " | "
			        + HtmlUtils.link(link(repoId) + "?style=" + RStyle.xmldump.name(),
			                "Dump as xml")

			        + "<br/>\n");
			
			// upload form
			w.write(HtmlUtils.form(METHOD.POST, link(repoId)).withInputFile("backupfile")
			        .withInputSubmit("Upload and set as current state").toString());
			
			List<XID> modelIdList = new ArrayList<XID>(p.getManagedModelIds());
			Collections.sort(modelIdList);
			for(XID modelId : modelIdList) {
				w.write("<h2>Model: " + modelId + "</h2>\n");
				w.flush();
				XAddress modelAddress = XX.toAddress(repoId, modelId, null, null);
				
				// do we have enough time left?
				if(c.getDurationSinceStart() > 10000) {
					// took to lonk, switch to task mode
					
				} else {
					// do it directly
					ModelResource.render(w, modelAddress, style == RStyle.htmlrevs ? MStyle.htmlrev
					        : MStyle.link);
				}
			}
			w.flush();
			w.close();
		}
		log.info(c.stop("index").getStats());
	}
	
	public static void update(String repoIdStr, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XydraRuntime.startRequest();
		
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

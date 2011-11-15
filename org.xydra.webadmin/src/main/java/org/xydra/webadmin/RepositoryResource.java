package org.xydra.webadmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.Clock;
import org.xydra.gae.UniversalTaskQueue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.webadmin.ModelResource.SetStateResult;

import com.google.appengine.api.taskqueue.DeferredTask;


public class RepositoryResource {
	
	public static final Logger log = LoggerFactory.getLogger(RepositoryResource.class);
	
	public static void restless(Restless restless, String prefix) {
		
		restless.addMethod(prefix + "/{repoId}/", "GET", RepositoryResource.class, "index", true,
		        new RestlessParameter("repoId"), new RestlessParameter("style", RStyle.html.name()));
		
		restless.addMethod(prefix + "/{repoId}/", "POST", RepositoryResource.class, "update", true,
		        new RestlessParameter("repoId"));
	}
	
	public static enum RStyle {
		html, htmlrevs, xmlzip
	}
	
	private static class Cache {
		
		private static final String PREFIX = "Serialised-";
		
		/**
		 * Key = ModelAddress
		 */
		static class MemcacheSerialisedModelEntry implements Serializable {
			private static final long serialVersionUID = -5945996390176658690L;
			@SuppressWarnings("unused")
			long time;
			String serialisation;
		}
		
		/**
		 * Key = RepoId
		 */
		static class MemcacheSerialisedRepositoryEntry implements Serializable {
			private static final long serialVersionUID = -3810056351260882859L;
			long time;
			@SuppressWarnings("unused")
			List<XID> modelIdList;
		}
		
		/**
		 * RepoId + '-updatedModels'
		 */
		
		public static boolean hasModelUpdateCountNotOlderThan(XID repoId, int modelCount,
		        int maxMillisecondsAgo) {
			String key = PREFIX + repoId + "-updatedModels";
			Object o = XydraRuntime.getMemcache().get(key);
			if(o != null) {
				Long l = (Long)o;
				if(l == modelCount) {
					// check age
					Object u = XydraRuntime.getMemcache().get(PREFIX + repoId);
					if(u != null) {
						MemcacheSerialisedRepositoryEntry repoEntry = (MemcacheSerialisedRepositoryEntry)u;
						return repoEntry.time + maxMillisecondsAgo > System.currentTimeMillis();
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				// set key to 0
				XydraRuntime.getMemcache().put(key, new Long(0));
				return false;
			}
		}
		
		public static void updateAllModels(long now, final XID repoId, List<XID> modelIdList,
		        final MStyle style) {
			MemcacheSerialisedRepositoryEntry repoEntry = new MemcacheSerialisedRepositoryEntry();
			repoEntry.time = now;
			repoEntry.modelIdList = modelIdList;
			XydraRuntime.getMemcache().put(Cache.PREFIX + repoId, repoEntry);
			
			for(final XID modelId : modelIdList) {
				// use task queue
				UniversalTaskQueue.enqueueTask(new DeferredTask() {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void run() {
						MemcacheSerialisedModelEntry modelEntry = new MemcacheSerialisedModelEntry();
						modelEntry.time = System.currentTimeMillis();
						XydraPersistence p = Utils.getPersistence(repoId);
						XWritableModel model = p.getModelSnapshot(XX.resolveModel(repoId, modelId));
						modelEntry.serialisation = ModelResource.computeSerialisation(model, style);
						XydraRuntime.getMemcache().put(PREFIX + XX.resolveModel(repoId, modelId),
						        modelEntry);
						// update count
						XydraRuntime.getMemcache()
						        .incrementAll(
						                Collections.singletonMap(
						                        PREFIX + repoId + "-updatedModels", 1l), 0);
					}
				});
			}
		}
		
		public static String getSerialisation(XAddress modelAddress) {
			MemcacheSerialisedModelEntry modelEntry = (MemcacheSerialisedModelEntry)XydraRuntime
			        .getMemcache().get(PREFIX + modelAddress);
			if(modelEntry == null) {
				throw new RuntimeException("memcache was null for " + modelAddress
				        + ". Maybe better use datastore instead?");
			}
			return modelEntry.serialisation;
		}
	}
	
	/**
	 * @param repoIdStr never null
	 * @param styleStr see {@link RStyle}
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void index(String repoIdStr, String styleStr, HttpServletResponse res)
	        throws IOException {
		XydraRuntime.startRequest();
		
		Clock c = new Clock().start();
		XAddress repoAddress = XX.resolveRepository(XX.toId(repoIdStr));
		RStyle style = RStyle.valueOf(styleStr);
		XID repoId = XX.toId(repoIdStr);
		XydraPersistence p = Utils.getPersistence(repoId);
		if(style == RStyle.xmlzip) {
			long now = System.currentTimeMillis();
			List<XID> modelIdList = new ArrayList<XID>(p.getModelIds());
			Collections.sort(modelIdList);
			int modelCount = modelIdList.size();
			if(Cache.hasModelUpdateCountNotOlderThan(repoId, modelCount, 60000)) {
				// all models have been updated at least once not too long ago
				String archivename = Utils.filenameOfNow("repo-" + repoId);
				ZipOutputStream zos = Utils.toZipFileDownload(res, archivename);
				for(XID modelId : modelIdList) {
					XAddress modelAddress = XX.resolveModel(repoId, modelId);
					String serialisation = Cache.getSerialisation(modelAddress);
					ModelResource.writeToZipstreamDirectly(modelId, MStyle.xml, serialisation, zos);
				}
				zos.finish();
			} else {
				Cache.updateAllModels(now, repoId, modelIdList, MStyle.xml);
				// show a download link
				Writer w = Utils.writeHeader(res, "Repo", repoAddress);
				w.write("Generating export in the background. Reload this page in 5 minutes.");
			}
		} else {
			// style HTML
			Writer w = Utils.writeHeader(res, "Repo", repoAddress);
			w.write(

			HtmlUtils.link(link(repoId) + "?style=" + RStyle.htmlrevs.name(), "With Revs")
			        
			        + " | "
			        + HtmlUtils.link(link(repoId) + "?style=" + RStyle.xmlzip.name(),
			                "Download as xml.zip") + "<br/>\n");
			
			// upload form
			w.write(HtmlUtils.form(METHOD.POST, link(repoId)).withInputFile("backupfile")
			        .withInputSubmit("Upload and set as current state").toString());
			
			List<XID> modelIdList = new ArrayList<XID>(p.getModelIds());
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

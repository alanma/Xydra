package org.xydra.webadmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.Clock;
import org.xydra.core.util.ConfigUtils;
import org.xydra.gae.UniversalUrlFetch;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.rmof.impl.delegate.WritableModelOnPersistence;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.webadmin.ModelResource.SetStateResult;


/**
 * Can list all models in a repo; provides import/export for a whole model.
 * 
 * @author xamde
 */
public class RepositoryResource {
	
	public static final Logger log = LoggerFactory.getLogger(RepositoryResource.class);
	
	public static final String PAGE_NAME = "Repository";
	public static String URL;
	
	private static XID actorId = XX.toId("_RepositoryResource");
	
	public static void restless(Restless restless, String prefix) {
		URL = prefix + "/repo";
		ObjectResource.restless(restless, URL);
		ModelResource.restless(restless, URL);
		restless.addMethod(URL + "/{repoId}", "GET", RepositoryResource.class, "deleteAllModels",
		        true,
		        
		        new RestlessParameter("repoId"),
		        
		        new RestlessParameter("command"),
		        
		        new RestlessParameter("sure", "no"));
		
		restless.addMethod(URL + "/{repoId}/", "GET", RepositoryResource.class, "index", true,
		        new RestlessParameter("repoId"),
		        
		        new RestlessParameter("style", RStyle.html.name()),
		        
		        new RestlessParameter("useTaskQueue", "false"),
		        
		        new RestlessParameter("cacheInInstance", "true"),
		        
		        new RestlessParameter("cacheInMemcache", "false"),
		        
		        new RestlessParameter("cacheInDatastore", "false")
		
		);
		
		restless.addMethod(URL + "/{repoId}/", "GET", RepositoryResource.class, "foreach", true,
		
		new RestlessParameter("repoId"),
		
		new RestlessParameter("foreachmodel"),
		
		new RestlessParameter("username", null),
		
		new RestlessParameter("password", "")
		
		);
		// cacheInInstance=true&cacheInMemcache=false&cacheInDatastore=true
		
		restless.addMethod(URL + "/{repoId}/", "POST", RepositoryResource.class, "update", true,
		        new RestlessParameter("repoId"),
		        
		        new RestlessParameter("replace", "false")
		
		);
		
	}
	
	public static enum RStyle {
		html, htmlrevs, xmlzip, xmldump
	}
	
	/**
	 * @param repoId never null
	 * @param foreachmodel a URL to be called for each model. Will get appended
	 *            a param 'modelAddress' with the currents model's address
	 * @param username if not null, is used for basic authentication against url
	 *            in foreachmodel
	 * @param password if not null, is used for basic authentication against url
	 *            in foreachmodel
	 * @param res where to print status infos
	 * @throws IOException ...
	 */
	public static void foreach(String repoId, String foreachmodel, String username,
	        String password, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "For each model");
		
		w.write("For-each model in repo " + repoId + " use param " + foreachmodel + "<br/>\n");
		w.flush();
		XID repositoryId = XX.toId(repoId);
		XydraPersistence p = Utils.getPersistence(repositoryId);
		List<XID> modelIdList = new ArrayList<XID>(p.getManagedModelIds());
		Collections.sort(modelIdList);
		Progress progress = new Progress();
		progress.startTime();
		for(XID modelId : modelIdList) {
			XAddress modelAddress = XX.resolveModel(repositoryId, modelId);
			String urlStr = foreachmodel + "?modelAddress=" + modelAddress;
			w.write("Calling " + urlStr + " ... ");
			w.flush();
			int result = UniversalUrlFetch.callUrl(urlStr, username, password, true);
			progress.makeProgress(1);
			w.write(" => " + result + ". Seconds left: "
			        + (progress.willTakeMsUntilProgressIs(modelIdList.size()) / 1000) + "<br/>\n");
			w.flush();
		}
		w.write("Done with all.<br/>\n");
		w.flush();
	}
	
	/**
	 * @param repoIdStr never null
	 * @param styleStr see {@link RStyle}
	 * @param useTaskQueueStr
	 * @param cacheInInstanceStr
	 * @param cacheInMemcacheStr
	 * @param cacheInDatastoreStr
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void index(String repoIdStr, String styleStr, String useTaskQueueStr,
	        String cacheInInstanceStr, String cacheInMemcacheStr, String cacheInDatastoreStr,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		log.info("index");
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
					Writer w = AppConstants.startPage(res, PAGE_NAME, "Xyadmin XML dump of repo "
					        + repoId);
					for(XID modelId : modelIdList) {
						XAddress modelAddress = XX.resolveModel(repoId, modelId);
						String serialisation = SerialisationCache.getSerialisation(modelAddress,
						        storeOpts);
						w.write(serialisation);
					}
				}
			} else {
				Writer w = AppConstants.startPage(res, PAGE_NAME, "Xyadmin Repo " + repoId);
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
			Writer w = AppConstants.startPage(res, PAGE_NAME, "" + repoAddress);
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
			w.write(HtmlUtils.form(METHOD.POST, link(repoId) + "?replace=true")
			        .withInputFile("backupfile")
			        .withInputSubmit("Upload and force set as current state").toString());
			w.write(HtmlUtils.form(METHOD.POST, link(repoId)).withInputFile("backupfile")
			        .withInputSubmit("Upload and update to this as current state").toString());
			
			// killer
			w.write(HtmlUtils.link(link(repoId) + "?command=deleteAllModels", "Delete all models"));
			
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
	
	public static void deleteAllModels(String repoIdStr, String commandStr, String sure,
	        HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Delete All Models");
		
		Clock c = new Clock().start();
		w.write("Found sure='" + sure + "'");
		w.flush();
		
		if(sure != null && sure.equalsIgnoreCase("yes")) {
			XID repoId = XX.toId(repoIdStr);
			XydraPersistence p = Utils.getPersistence(repoId);
			for(XID modelId : p.getManagedModelIds()) {
				w.write("Deleting model " + modelId);
				w.flush();
				XWritableModel model = new WritableModelOnPersistence(p, actorId, modelId);
				Collection<XID> objectIds = IteratorUtils.addAll(model.iterator(),
				        new HashSet<XID>());
				for(XID objectId : objectIds) {
					XCommand command = X.getCommandFactory().createForcedRemoveObjectCommand(
					        repoId, modelId, objectId);
					long l = p.executeCommand(actorId, command);
					w.write(" ... result = " + l + "<br/>\n");
				}
				w.flush();
			}
			w.write("Done<br/>\n");
		} else {
			w.write("Add ?sure=yes to url if you really mean it<br/>\n");
		}
		w.write("Stats: " + c.getStats() + "<br/>\n");
		w.flush();
		w.close();
	}
	
	/**
	 * @param repoIdStr
	 * @param req
	 * @param res
	 * @param replaceModelsStr if "true", existing models are completely
	 *            replaced, NON-transactional. Scales well for huge models.
	 * @throws IOException
	 */
	public static void update(String repoIdStr, String replaceModelsStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Restore Repository");
		
		XID repoId = XX.toId(repoIdStr);
		boolean replaceModels = replaceModelsStr != null
		        && replaceModelsStr.equalsIgnoreCase("true");
		
		w.write("Processing upload... replace=" + replaceModelsStr + "</br>");
		w.flush();
		
		InputStream fis = Utils.getMultiPartContentFileAsInputStream(w, req);
		assert fis != null;
		
		updateFromZippedInputStream(fis, repoId, w, replaceModels);
		
		w.close();
	}
	
	/**
	 * @param fis input stream which contains zipped content
	 * @param repoId for new models
	 * @param w for debug output
	 * @param replaceModels if true existing content is ignored and just
	 *            replaced with conent of stream. If false, existing content is
	 *            updated. TODO =?
	 * @throws IOException
	 */
	public static void updateFromZippedInputStream(InputStream fis, XID repoId, Writer w,
	        boolean replaceModels) throws IOException {
		Clock c = new Clock().start();
		ZipInputStream zis = new ZipInputStream(fis);
		w.write("... open zip stream ...</br>");
		w.flush();
		
		XReadableModel model;
		// stats
		int modelExisted = 0;
		int modelsWithChanges = 0;
		int restored = 0;
		while((model = ModelResource.readZippedModel(zis)) != null) {
			c.stopAndStart("parsed-" + model.getId());
			w.write("... parsed model '" + model.getAddress() + "' [" + model.getRevisionNumber()
			        + "] ...</br>");
			w.flush();
			
			SetStateResult result;
			if(replaceModels) {
				result = ModelResource.setStateTo(repoId, model);
			} else {
				result = ModelResource.updateStateTo(repoId, model, false);
			}
			log.info("" + result);
			c.stopAndStart("applied-" + model.getId());
			modelExisted += result.modelExisted ? 1 : 0;
			modelsWithChanges += result.changes ? 1 : 0;
			restored++;
			log.debug(model.getAddress() + " " + c.getStats());
			w.write("... applied to server repository " + result + "</br>");
			w.flush();
		}
		String stats = "Restored: " + restored + ", existed before: " + modelExisted
		        + ", noChangesIn:" + modelsWithChanges;
		w.write(stats + " " + c.getStats() + "</br>");
		log.debug("Stats: " + stats + " " + c.getStats());
		w.write("... Done</br>");
		w.flush();
	}
	
	/**
	 * @param repoId
	 * @return relative admin link with no slash at the end
	 */
	public static String link(XID repoId) {
		return "/admin" + URL + "/" + repoId;
	}
	
	/**
	 * @param p
	 * @param zipFile should be in a directory that exists already
	 * @throws IOException
	 */
	public static void saveRepositoryToZipFile(XydraPersistence p, File zipFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFile);
		List<XID> modelIdList = new ArrayList<XID>(p.getManagedModelIds());
		ZipOutputStream zos = new ZipOutputStream(fos);
		for(XID modelId : modelIdList) {
			XAddress modelAddress = XX.resolveModel(p.getRepositoryId(), modelId);
			XWritableModel model = p.getModelSnapshot(new GetWithAddressRequest(modelAddress,
			        ModelResource.INCLUDE_TENTATIVE));
			if(model == null) {
				log.warn("Could not find model " + modelAddress);
			} else {
				String serialisation = ModelResource.computeSerialisation(model, MStyle.xml);
				ModelResource.writeToZipstreamDirectly(modelId, MStyle.xml, serialisation, zos);
			}
		}
		zos.close();
		fos.close();
	}
	
	/**
	 * First completely wipes out the current repository, then loads from file
	 * 
	 * @param zipFile from where to load
	 * @param repoId to load, should match the file. Default is often
	 *            'gae-data-'
	 * @param w where debug infos are written. In a standalone app, use an
	 *            {@link OutputStreamWriter} wrapped around System.out.
	 * @throws IOException
	 */
	public static void loadRepositoryFromZipFile(File zipFile, XID repoId, Writer w)
	        throws IOException {
		SyncDatastore.deleteAllEntitiesOneByOne();
		assert SyncDatastore.getAllKinds().size() == 0;
		XydraRuntime.getMemcache().clear();
		InstanceContext.clearInstanceContext();
		
		FileInputStream fis = new FileInputStream(zipFile);
		RepositoryResource.updateFromZippedInputStream(fis, repoId, w, true);
	}
	
}

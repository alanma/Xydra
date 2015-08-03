package org.xydra.webadmin;

import java.io.ByteArrayInputStream;
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

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.util.Clock;
import org.xydra.core.util.ConfigUtils;
import org.xydra.index.iterator.Iterators;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.FileDownloadUtils;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.Progress;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.rmof.impl.delegate.WritableModelOnPersistence;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.webadmin.ModelResource.SetStateResult;
import org.xydra.xgae.XGae;
import org.xydra.xgae.gaeutils.GaeTestfixer;
import org.xydra.xgae.gaeutils.UniversalUrlFetch;

/**
 * Can list all models in a repo; provides import/export for a whole model.
 *
 * @author xamde
 */
public class RepositoryResource {

	private static final Logger log = LoggerFactory.getLogger(RepositoryResource.class);

	public static final String PAGE_NAME = "Repository";

	private static final String LINEEND = "<br/>\n";
	public static String URL;

	private static XId actorId = XX.toId("_RepositoryResource");

	public static void restless(final Restless restless, final String prefix) {
		URL = prefix + "/repo";
		ObjectResource.restless(restless, URL);
		ModelResource.restless(restless, URL);
		restless.addMethod(URL + "/{repoId}", "GET", RepositoryResource.class, "deleteAllModels",
				true,

				new RestlessParameter("repoId"),

				new RestlessParameter("command"),

				new RestlessParameter("confirm", null));

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

				new RestlessParameter("replace", "false"),

				new RestlessParameter("_upload_", null)

		);

	}

	public static enum RStyle {
		html, htmlrevs, xmlzip, xmldump, csvzip
	}

	/**
	 * @param repoId
	 *            never null
	 * @param foreachmodel
	 *            a URL to be called for each model. Will get appended a param
	 *            'modelAddress' with the currents model's address
	 * @param username
	 *            if not null, is used for basic authentication against url in
	 *            foreachmodel
	 * @param password
	 *            if not null, is used for basic authentication against url in
	 *            foreachmodel
	 * @param res
	 *            where to print status infos
	 * @throws IOException
	 *             ...
	 */
	public static void foreach(final String repoId, final String foreachmodel, final String username,
			final String password, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = Utils.startPage(res, PAGE_NAME, "For each model");

		w.write("For-each model in repo " + repoId + " use param " + foreachmodel + "<br/>\n");
		w.flush();
		final XId repositoryId = Base.toId(repoId);
		final XydraPersistence p = Utils.createPersistence(repositoryId);
		final List<XId> modelIdList = new ArrayList<XId>(p.getManagedModelIds());
		Collections.sort(modelIdList);
		final Progress progress = new Progress();
		progress.startTime();
		for (final XId modelId : modelIdList) {
			final XAddress modelAddress = Base.resolveModel(repositoryId, modelId);
			final String urlStr = foreachmodel + "?modelAddress=" + modelAddress;
			w.write("Calling " + urlStr + " ... ");
			w.flush();
			final int result = UniversalUrlFetch.callUrl(urlStr, username, password, true);
			progress.makeProgress(1);
			w.write(" => " + result + ". Seconds left: "
					+ progress.willTakeMsUntilProgressIs(modelIdList.size()) / 1000 + "<br/>\n");
			w.flush();
		}
		w.write("Done with all.<br/>\n");
		w.flush();
	}

	/**
	 * @param repoIdStr
	 *            never null
	 * @param styleStr
	 *            see {@link RStyle}
	 * @param useTaskQueueStr
	 * @param cacheInInstanceStr
	 * @param cacheInMemcacheStr
	 * @param cacheInDatastoreStr
	 * @param res
	 *            ..
	 * @throws IOException
	 *             ...
	 */
	public static void index(final String repoIdStr, final String styleStr, final String useTaskQueueStr,
			final String cacheInInstanceStr, final String cacheInMemcacheStr, final String cacheInDatastoreStr,
			final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		log.info("index");
		log.trace("logtest: trace");
		log.debug("logtest: debug");
		log.info("logtest: info");

		final boolean useTaskQueue = ConfigUtils.isTrue(useTaskQueueStr);
		final boolean cacheInInstance = ConfigUtils.isTrue(cacheInInstanceStr);
		final boolean cacheInMemcache = ConfigUtils.isTrue(cacheInMemcacheStr);
		final boolean cacheInDatastore = ConfigUtils.isTrue(cacheInDatastoreStr);

		final StorageOptions storeOpts = StorageOptions.create(cacheInInstance ? 1 : 0, cacheInMemcache,
				cacheInDatastore,
				/* always allow on the-fly-computation */
				true);

		final Clock c = new Clock().start();
		final XAddress repoAddress = Base.resolveRepository(Base.toId(repoIdStr));
		final RStyle style = RStyle.valueOf(styleStr);
		final XId repoId = Base.toId(repoIdStr);
		final XydraPersistence p = Utils.createPersistence(repoId);
		if (style == RStyle.xmlzip || style == RStyle.xmldump) {
			final List<XId> modelIdList = new ArrayList<XId>(p.getManagedModelIds());
			// IMPROVE we can use paging here to split work; BUT number of
			// models
			// might change.
			Collections.sort(modelIdList);
			final boolean allUpToDate = SerialisationCache.updateAllModels(repoId, modelIdList,
					MStyle.xml, false, useTaskQueue, cacheInInstance, cacheInMemcache,
					cacheInDatastore);
			if (allUpToDate) {
				log.info("All model serialisations are up-to-date, generating zip");
				final String archivename = Utils.filenameOfNow("repo-" + repoId);
				if (style == RStyle.xmlzip) {
					if (modelIdList.size() == 0) {
						log.info("No models, no zip");
						return;
					}
					final ZipOutputStream zos = FileDownloadUtils.toZipFileDownload(res, archivename);
					for (final XId modelId : modelIdList) {
						final XAddress modelAddress = Base.resolveModel(repoId, modelId);
						final String serialisation = SerialisationCache.getSerialisation(modelAddress,
								storeOpts);
						ModelResource.writeToZipstreamDirectly(modelId, MStyle.xml, serialisation,
								zos);
					}
					zos.finish();
				} else {
					final Writer w = Utils
							.startPage(res, PAGE_NAME, "Xyadmin XML dump of repo " + repoId);
					for (final XId modelId : modelIdList) {
						final XAddress modelAddress = Base.resolveModel(repoId, modelId);
						final String serialisation = SerialisationCache.getSerialisation(modelAddress,
								storeOpts);
						w.write(serialisation);
					}
				}
			} else {
				final Writer w = Utils.startPage(res, PAGE_NAME, "Xyadmin Repo " + repoId);
				w.write("Generating serialisations took too long. Watch task queue to finish and consider using different caching params.<br/>\n");
				w.write(SharedHtmlUtils.form(METHOD.GET, "").withHiddenInputText("", repoIdStr)
						.withHiddenInputText("style", styleStr)
						.withInputText("useTaskQueue", useTaskQueueStr)
						.withInputText("cacheInInstance", cacheInInstanceStr)
						.withInputText("cacheInMemcache", cacheInMemcacheStr)
						.withInputText("cacheInDatastore", cacheInDatastoreStr)
						.withInputSubmit("Retry").toString());
			}
		} else {
			// style HTML
			final Writer w = Utils.startPage(res, PAGE_NAME, "" + repoAddress);
			w.write(

			SharedHtmlUtils.link(link(repoId) + "?style=" + RStyle.htmlrevs.name(), "With Revs")

					+ " | "
					+ SharedHtmlUtils.link(link(repoId) + "?style=" + RStyle.xmlzip.name(),
							"Download as xml.zip")

					+ " | "
					+ SharedHtmlUtils.link(link(repoId) + "?style=" + RStyle.xmldump.name(),
							"Dump as xml")

					+ "<br/>\n");

			// upload form
			w.write(SharedHtmlUtils.form(METHOD.POST, link(repoId) + "?replace=true")
					.withInputFile("backupfile")
					.withInputSubmit("Upload and force set as current state").toString());
			w.write(SharedHtmlUtils.form(METHOD.POST, link(repoId)).withInputFile("backupfile")
					.withInputSubmit("Upload and update to this as current state").toString());

			// killer
			w.write(SharedHtmlUtils.link(link(repoId) + "?command=deleteAllModels", "Delete all models"));

			final List<XId> modelIdList = new ArrayList<XId>(p.getManagedModelIds());
			Collections.sort(modelIdList);
			for (final XId modelId : modelIdList) {
				w.write("<h2>Model: " + modelId + "</h2>\n");
				w.flush();
				final XAddress modelAddress = Base.toAddress(repoId, modelId, null, null);

				// do we have enough time left?
				if (c.getDurationSinceStart() > 10000) {
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

	private static final String passwordPropertyNameInWebXml = "org.xydra.webadmin.RepositoryResource.password";

	public static void deleteAllModels(final String repoIdStr, final String commandStr, final String confirmParam,
			final HttpServletRequest req, final HttpServletResponse res, final IRestlessContext context)
			throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();

		AdminAuthUtils.setTempAuthCookie(context, passwordPropertyNameInWebXml);

		final Writer w = Utils.startPage(res, PAGE_NAME, "Delete All Models");
		final Clock c = new Clock().start();

		final String password = context.getRestless().getInitParameter(passwordPropertyNameInWebXml);
		w.write("Password is '"
				+ password
				+ "' it must match the URL param 'confirm' and the cookie. Setting cookie for 120 seconds ..."
				+ LINEEND);

		AdminAuthUtils.checkIfAuthorised(context, passwordPropertyNameInWebXml, confirmParam);

		doDeleteAllData(w, repoIdStr);

		w.write("Stats: " + c.getStats() + "<br/>\n");
		w.flush();
		w.close();
	}

	private static void doDeleteAllData(final Writer w, final String repoIdStr) throws IOException {
		final XId repoId = Base.toId(repoIdStr);
		final XydraPersistence p = Utils.createPersistence(repoId);
		for (final XId modelId : p.getManagedModelIds()) {
			w.write("Deleting model " + modelId);
			w.flush();
			final XWritableModel model = new WritableModelOnPersistence(p, actorId, modelId);
			final Collection<XId> objectIds = Iterators.addAll(model.iterator(), new HashSet<XId>());
			for (final XId objectId : objectIds) {
				final XCommand command = BaseRuntime.getCommandFactory().createForcedRemoveObjectCommand(repoId,
						modelId, objectId);
				final long l = p.executeCommand(actorId, command);
				w.write(" ... result = " + l + "<br/>\n");
			}
			w.flush();
		}
		w.write("Done<br/>\n");
	}

	/**
	 * @param repoIdStr
	 * @param req
	 * @param res
	 * @param replaceModelsStr
	 *            if "true", existing models are completely replaced,
	 *            NON-transactional. Scales well for huge models.
	 * @param upload
	 * @throws IOException
	 */
	public static void update(final String repoIdStr, final String replaceModelsStr, final byte[] upload,

	final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = Utils.startPage(res, PAGE_NAME, "Restore Repository");

		final XId repoId = Base.toId(repoIdStr);
		final boolean replaceModels = replaceModelsStr != null
				&& replaceModelsStr.equalsIgnoreCase("true");

		w.write("Processing upload... replace=" + replaceModelsStr + "</br>");
		w.flush();

		assert upload != null;
		final InputStream fis = new ByteArrayInputStream(upload);
		// InputStream fis = Utils.getMultiPartContentFileAsInputStream(w, req);
		assert fis != null;

		updateFromZippedInputStream(fis, repoId, w, replaceModels);

		w.close();
	}

	/**
	 * @param fis
	 *            input stream which contains zipped content
	 * @param repoId
	 *            for new models
	 * @param w
	 *            for debug output
	 * @param replaceModels
	 *            if true existing content is ignored and just replaced with
	 *            content of stream. If false, existing content is updated. TODO
	 *            DOCU =?
	 * @throws IOException
	 */
	public static void updateFromZippedInputStream(final InputStream fis, final XId repoId, final Writer w,
			final boolean replaceModels) throws IOException {
		final Clock c = new Clock().start();
		final ZipInputStream zis = new ZipInputStream(fis);
		w.write("... open zip stream ...</br>");
		w.flush();

		XReadableModel model;
		// stats
		int modelExisted = 0;
		int modelsWithChanges = 0;
		int restored = 0;
		while ((model = ModelResource.readZippedModel(zis)) != null) {
			c.stopAndStart("parsed-" + model.getId());
			w.write("... parsed model '" + model.getAddress() + "' [" + model.getRevisionNumber()
					+ "] ...</br>");
			w.flush();

			SetStateResult result;
			if (replaceModels) {
				result = ModelResource.setStateTo(repoId, model);
			} else {
				result = ModelResource.updateStateTo(repoId, model, false);
			}
			log.info(model.getAddress() + "" + result);
			c.stopAndStart("applied-" + model.getId());
			modelExisted += result.modelExisted ? 1 : 0;
			modelsWithChanges += result.changes ? 1 : 0;
			restored++;
			log.trace(model.getAddress() + " " + c.getStats());
			w.write("... applied to server repository " + result + "</br>");
			w.flush();
		}
		final String stats = "Restored: " + restored + ", existed before: " + modelExisted
				+ ", noChangesIn:" + modelsWithChanges;
		w.write(stats + " " + c.getStats() + "</br>");
		log.trace("Stats: " + stats + " " + c.getStats());
		w.write("... Done</br>");
		w.flush();
	}

	/**
	 * @param repoId
	 * @return relative admin link with no slash at the end
	 */
	public static String link(final XId repoId) {
		return "/admin" + URL + "/" + repoId;
	}

	/**
	 * @param p
	 * @param zipFile
	 *            should be in a directory that exists already
	 * @throws IOException
	 */
	public static void saveRepositoryToZipFile(final XydraPersistence p, final File zipFile) throws IOException {
		final FileOutputStream fos = new FileOutputStream(zipFile);
		final List<XId> modelIdList = new ArrayList<XId>(p.getManagedModelIds());
		final ZipOutputStream zos = new ZipOutputStream(fos);
		for (final XId modelId : modelIdList) {
			final XAddress modelAddress = Base.resolveModel(p.getRepositoryId(), modelId);
			final XWritableModel model = p.getModelSnapshot(new GetWithAddressRequest(modelAddress,
					ModelResource.INCLUDE_TENTATIVE));
			if (model == null) {
				log.warn("Could not find model " + modelAddress + " - it might have been deleted");
			} else {
				final String serialisation = ModelResource.computeSerialisation(model, MStyle.xml);

				ModelResource.writeToZipstreamDirectly(modelId, MStyle.xml, serialisation, zos);
			}
		}
		zos.close();
		fos.close();
	}

	/**
	 * First completely wipes out the current repository, then loads from file
	 *
	 * @param zipFile
	 *            from where to load
	 * @param repoId
	 *            to load, should match the file. Default is often 'gae-data-'
	 * @param w
	 *            where debug infos are written. In a standalone app, use an
	 *            {@link OutputStreamWriter} wrapped around System.out.
	 * @throws IOException
	 */
	public static void loadRepositoryFromZipFile(final File zipFile, final XId repoId, final Writer w)
			throws IOException {
		// was "deleteOneByOne", I wonder why
		XGae.get().datastore().sync().clear();
		assert XGae.get().datastore().sync().getAllKinds().size() == 0;
		XGae.get().memcache().clear();
		InstanceContext.clear();

		final FileInputStream fis = new FileInputStream(zipFile);
		RepositoryResource.updateFromZippedInputStream(fis, repoId, w, true);
	}

}

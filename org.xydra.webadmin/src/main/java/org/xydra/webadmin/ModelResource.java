package org.xydra.webadmin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XFile;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XModel;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlSerializer;
import org.xydra.core.util.Clock;
import org.xydra.index.iterator.Iterators;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.FileDownloadUtils;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.ChangesUtils;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class ModelResource {

	public static final Logger log = LoggerFactory.getLogger(ModelResource.class);
	private static final String UTF8 = "UTF-8";
	public static final boolean INCLUDE_TENTATIVE = true;

	public static final String PAGE_NAME = "Model";

	public static enum MStyle {
		html, htmlrev, htmlevents, link, xml, json, xmlhtml, csv, /**
		 * Direct
		 * output of changelog
		 */
		htmlchanges
	}

	public static void restless(final Restless restless, final String prefix) {
		restless.addMethod(prefix + "/{repoId}/{modelId}/", "GET", ModelResource.class, "index",
				true,

				new RestlessParameter("repoId"), new RestlessParameter("modelId"),
				new RestlessParameter("style", MStyle.link.name()), new RestlessParameter(
						"download", "false")

		);

		restless.addMethod(prefix + "/{repoId}/{modelId}/command", "POST", ModelResource.class,
				"command", true,

				new RestlessParameter("repoId"), new RestlessParameter("modelId"),
				new RestlessParameter("cmd")

		);

		restless.addMethod(prefix + "/{repoId}/{modelId}/", "POST", ModelResource.class, "update",
				true,

				new RestlessParameter("repoId"), new RestlessParameter("modelId"),

				new RestlessParameter("replace", "false"),

				new RestlessParameter("_upload_", null)

		);
	}

	@SuppressWarnings("unused")
	public static void command(final String repoIdStr, final String modelIdStr, final String cmdStr,
			final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = Utils.startPage(res, PAGE_NAME, "Deleting Model");

		final XAddress modelAddress = Base.toAddress(Base.toId(repoIdStr), Base.toId(modelIdStr), null, null);
		if (cmdStr.equals("delete")) {
			// TODO use Repo on Persistence to delete model
		}

		Utils.endPage(w);
	}

	public static void index(final String repoIdStr, final String modelIdStr, final String styleStr,
			final String downloadStr, final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();

		final Clock c = new Clock().start();
		final XAddress modelAddress = Base.toAddress(Base.toId(repoIdStr), Base.toId(modelIdStr), null, null);
		final MStyle style = MStyle.valueOf(styleStr);
		final boolean download = Boolean.parseBoolean(downloadStr);

		if (style == MStyle.xml || style == MStyle.xmlhtml || style == MStyle.json) {
			final XydraPersistence p = Utils.createPersistence(modelAddress.getRepository());
			final ModelRevision rev = p.getModelRevision(new GetWithAddressRequest(modelAddress,
					INCLUDE_TENTATIVE));
			log.debug(modelAddress + " rev=" + rev.revision() + " exists:" + rev.modelExists());

			if (rev.modelExists()) {
				final XWritableModel model = p.getModelSnapshot(new GetWithAddressRequest(modelAddress,
						INCLUDE_TENTATIVE));
				if (download) {
					final String name = modelAddress.getRepository() + "-" + modelAddress.getModel()
							+ "-rev" + model.getRevisionNumber();
					final String archivename = Utils.filenameOfNow(name);
					final ZipOutputStream zos = FileDownloadUtils.toZipFileDownload(res, archivename);

					writeToZipstreamDirectly(model, zos, style);

					zos.finish();
				} else {
					if (style == MStyle.xml | style == MStyle.xmlhtml) {
						final XmlSerializer serializer = new XmlSerializer();
						ServletUtils.headers(res, 200, 1, serializer.getContentType());
						final Writer w = res.getWriter();
						final MiniWriter miniwriter = new MiniStreamWriter(w);
						final XydraOut out = serializer.create(miniwriter, false);
						out.enableWhitespace(true, true);
						// write xml header manually
						w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
						if (style == MStyle.xmlhtml) {
							// write link to xslt
							w.write("<?xml-stylesheet type=\"text/xsl\" href=\"/s/xml-to-html.xsl\"?>\n");
						}
						// write xml content
						SerializedModel.serialize(model, out);
					} else if (style == MStyle.json) {
						final JsonSerializer serializer = new JsonSerializer();
						ServletUtils.headers(res, 200, 1, serializer.getContentType());
						final Writer w = res.getWriter();
						serializeToWriter(model, serializer, w);
					}
				}
			} else {
				final Writer w = Utils.startPage(res, PAGE_NAME, "Model does not exist");
				w.write("<p>Model '" + modelAddress + "' does (currently) not exist.</p>");
				Utils.endPage(w);
			}
		} else {
			// html
			final Writer w = Utils.startPage(res, PAGE_NAME, "" + modelAddress);
			w.write(SharedHtmlUtils.link(RepositoryResource.link(modelAddress.getRepository()),
					"Back to repository '" + modelAddress.getRepository() + "'<br/>\n"));

			// upload form
			w.write(SharedHtmlUtils.form(METHOD.POST, link(modelAddress) + "?replace=true")
					.withInputFile("backupfile")
					.withInputSubmit("Upload and force set as current state").toString());
			w.write(SharedHtmlUtils.form(METHOD.POST, link(modelAddress)).withInputFile("backupfile")
					.withInputSubmit("Upload and update to this as current state").toString());

			w.write("<hr/>");
			render(w, modelAddress, style);
			Utils.endPage(w);
		}

		c.stop("index");
		log.info(c.getStats());
	}

	/**
	 * @param repoIdStr
	 * @param modelIdStr
	 * @param replaceStr
	 *            true or false
	 * @param upload
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	public static void update(final String repoIdStr, final String modelIdStr, final String replaceStr,
			final byte[] upload,

			final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = Utils.startPage(res, PAGE_NAME, "Restore Model");

		final Clock c = new Clock().start();

		final boolean isReplace = replaceStr != null && replaceStr.equals("true");
		final XId repoId = Base.toId(repoIdStr);

		w.write("Processing upload.with replace=" + isReplace + "..</br>");
		w.flush();

		assert upload != null;
		InputStream fis = new ByteArrayInputStream(upload);
		// InputStream fis = Utils.getMultiPartContentFileAsInputStream(w, req);
		assert fis != null;

		final ZipInputStream zis = new ZipInputStream(fis);
		w.write("... open zip stream ...</br>");
		w.flush();

		XReadableModel model;
		model = readZippedModel(zis);

		if (model == null) {
			fis = new ByteArrayInputStream(upload);
			model = readNonZippedModel(fis);
		}

		if (model == null) {
			throw new RuntimeException("Could not read model from ZIS");
		}
		c.stopAndStart("parsed-" + model.getId());
		if (model.getId().equals(Base.toId(modelIdStr))) {
			w.write("... parsed model '" + model.getAddress() + "' [" + model.getRevisionNumber()
					+ "] ...</br>");
			w.flush();

			final SetStateResult result = ModelResource.updateStateTo(repoId, model, isReplace);
			log.info("" + result);
			c.stopAndStart("applied-" + model.getId());
			w.write("... applied to server repository " + result + ".</br>");
		} else {
			w.write("ModelID of this resource (" + modelIdStr
					+ ") does not match the model ID in the file (" + model.getId() + ")");
		}

		w.write("Stats:" + c.getStats());
		log.info("Stats: " + c.getStats());
		Utils.endPage(w);
	}

	static final String getStorageName(final XId modelId, final MStyle style) {
		/** matches XFile.MODEL_SUFFIX for xml */
		return modelId + ".xmodel." + style;
	}

	public static void writeToZipstreamDirectly(final XWritableModel model, final ZipOutputStream zos,
			final MStyle style) throws IOException {
		final ZipEntry e = new ZipEntry(getStorageName(model.getId(), style));
		zos.putNextEntry(e);
		final OutputStreamWriter w = new OutputStreamWriter(zos, UTF8);
		XydraSerializer serializer = null;
		if (style == MStyle.xml) {
			serializer = new XmlSerializer();
		} else if (style == MStyle.json) {
			serializer = new JsonSerializer();
		}
		serializeToWriter(model, serializer, w);
		zos.closeEntry();
	}

	public static void writeToZipstreamDirectly(final XId modelId, final MStyle style, final String serialisation,
			final ZipOutputStream zos) throws IOException {
		XyAssert.xyAssert(serialisation != null);

		final ZipEntry e = new ZipEntry(getStorageName(modelId, style));
		zos.putNextEntry(e);
		final OutputStreamWriter w = new OutputStreamWriter(zos, UTF8);
		w.write(serialisation);
		w.flush();
		zos.closeEntry();
	}

	/**
	 * Run this in a task queue
	 *
	 * @param model
	 * @param style
	 * @return serialisation in given style
	 */
	public static String computeSerialisation(final XWritableModel model, final MStyle style) {
		final StringWriter sw = new StringWriter();
		XydraSerializer serializer = null;
		if (style == MStyle.xml) {
			serializer = new XmlSerializer();
		} else if (style == MStyle.json) {
			serializer = new JsonSerializer();
		}
		serializeToWriter(model, serializer, sw);
		try {
			sw.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		final String serialised = sw.getBuffer().toString();
		return serialised;
	}

	static void serializeToWriter(final XReadableModel model, final XydraSerializer serializer, final Writer w) {
		final MiniWriter miniwriter = new MiniStreamWriter(w);
		final XydraOut out = serializer.create(miniwriter);
		out.enableWhitespace(true, true);
		SerializedModel.serialize(model, out);
		out.flush();
		assert out.isClosed();
	}

	public static void render(final Writer w, final XAddress modelAddress, final MStyle style) throws IOException {
		w.write(

		SharedHtmlUtils.link(link(modelAddress)) + " | " +

		SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.html, modelAddress.toString())

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlrev, "Rev")

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlevents, "Events")

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlchanges, "Change log")

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xmlhtml, "XML-as-HTML")

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xml, "XML")

		+ SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xml + "&download=true", ".zip")

		+ " | " + SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.json, "JSON")

		+ SharedHtmlUtils.link(link(modelAddress) + "?style=" + MStyle.json + "&download=true", ".zip")

		+ "<br/>\n");

		if (style == MStyle.htmlevents || style == MStyle.htmlrev || style == MStyle.html) {

			// upload form
			w.write(""
					+ SharedHtmlUtils.form(METHOD.POST, link(modelAddress)).withInputFile("backupfile")
							.withInputSubmit("Upload and set as current state"));
		}

		if (style == MStyle.htmlrev || style == MStyle.htmlevents || style == MStyle.htmlchanges) {
			final XydraPersistence p = Utils.createPersistence(modelAddress.getRepository());
			final ModelRevision rev = p.getModelRevision(new GetWithAddressRequest(modelAddress,
					INCLUDE_TENTATIVE));
			w.write("rev=" + rev.revision() + " exists:" + rev.modelExists() + "<br/>\n");
			w.flush();
			if (style == MStyle.htmlevents) {
				final List<XEvent> events = p.getEvents(modelAddress, 0, rev.revision());
				if (events == null) {
					w.write("No events.");
				} else {
					XydraHtmlUtils.writeEvents(events, w);
				}
			} else if (style == MStyle.htmlchanges) {
				ChangesUtils.renderChangeLog(modelAddress, w);
				w.flush();
			}
		}

	}

	public static String link(final XAddress modelAddress) {
		return "/admin" + RepositoryResource.URL + "/" + modelAddress.getRepository() + "/"
				+ modelAddress.getModel();
	}

	static class SetStateResult {
		boolean modelExisted = false;
		boolean changes = false;
		String debug;

		@Override
		public String toString() {
			return "modelExisted?" + this.modelExisted + " changes?" + this.changes + " debugInfo:"
					+ this.debug;
		}
	}

	/**
	 * @param repoId
	 *            where to add
	 * @param model
	 *            which can have an address with NO repositoryId
	 * @param overwriteIfSameRevPresent
	 *            if true, content in repo is overwritten even if the same revNr
	 *            is found
	 * @return some statistical information
	 */
	public static SetStateResult updateStateTo(final XId repoId, final XReadableModel model,
			final boolean overwriteIfSameRevPresent) {
		log.debug("Set state from " + model.getAddress() + " to " + repoId);
		final XydraPersistence p = Utils.createPersistence(repoId);
		final SetStateResult result = new SetStateResult();

		final XId actor = Base.toId("ModelResource");
		final XAddress modelAddress = Base.resolveModel(repoId, model.getId());

		final ModelRevision modelRev = p.getModelRevision(new GetWithAddressRequest(modelAddress,
				INCLUDE_TENTATIVE));
		if (modelRev != null) {
			if (!overwriteIfSameRevPresent && model.getRevisionNumber() == modelRev.revision()) {
				log.debug("Model already stored.");
				result.changes = false;
				result.modelExisted = true;
				result.debug = "modelAlreadyStored";
				return result;
			}
		}
		// else: overwrite
		XReadableModel oldModel = p.getModelSnapshot(new GetWithAddressRequest(modelAddress,
				INCLUDE_TENTATIVE));

		if (oldModel != null) {
			result.modelExisted = true;
		} else {
			result.modelExisted = false;
			final XRepositoryCommand createModelCommand = MemoryRepositoryCommand.createAddCommand(
					Base.resolveRepository(repoId), XCommand.FORCED, model.getId());
			final long cmdResult = p.executeCommand(actor, createModelCommand);
			if (cmdResult == XCommand.FAILED) {
				/*
				 * should only happen if model changed since
				 * tb.changeModel(oldModel, model)
				 */
				throw new RuntimeException("error creating model \"" + modelAddress + "\" ");
			}
			oldModel = new SimpleModel(modelAddress, 0);
		}
		assert oldModel != null;
		final XTransactionBuilder tb = new XTransactionBuilder(modelAddress);
		tb.changeModel(oldModel, model);
		// IMPROVE or use the change log if the model has one?
		if (tb.isEmpty()) {
			if (result.modelExisted) {
				result.changes = false;
			} else {
				// only 1 change: creating the new empty model
				result.changes = true;
			}
		} else {
			result.changes = true;
			final long cmdResult = p.executeCommand(actor, tb.build());
			if (cmdResult == XCommand.FAILED) {
				/*
				 * should only happen if model changed since
				 * tb.changeModel(oldModel, model)
				 */
				throw new RuntimeException("error restoring model \"" + modelAddress
						+ "\", transaction failed");
			}
		}
		return result;
	}

	public static SetStateResult setStateTo(final XId repoId, final XReadableModel model) {
		log.debug("Set state of model " + model.getAddress() + " non-transactionally in repo "
				+ repoId);
		final XydraPersistence p = Utils.createPersistence(repoId);
		final SetStateResult result = new SetStateResult();

		final XId actor = Base.toId("ModelResource");
		final XId modelId = model.getId();
		final XAddress modelAddress = Base.resolveModel(repoId, modelId);

		final WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(p, actor);
		if (repo.hasModel(modelId)) {
			// avoid creating too large events
			final XWritableModel oldModel = repo.getModel(modelId);
			final Collection<XId> oldObjectsIds = Iterators.addAll(oldModel.iterator(),
					new HashSet<XId>());
			if (oldObjectsIds.size() > 20) {
				for (final XId oldObjectID : oldObjectsIds) {
					oldModel.removeObject(oldObjectID);
				}
			}
			repo.removeModel(modelId);
		}
		repo.createModel(modelId);
		for (final XId o : model) {
			final XReadableObject xo = model.getObject(o);

			final XTransactionBuilder tb = new XTransactionBuilder(modelAddress);
			tb.addObject(modelAddress, xo, true);
			final XTransaction txn = tb.build();
			final long cmdResult = p.executeCommand(actor, txn);
			if (cmdResult == XCommand.FAILED) {
				throw new RuntimeException("error restoring model \"" + modelAddress
						+ "\", object-transaction failed");
			} else {
				result.changes = true;
			}
		}
		return result;
	}

	static XReadableModel readZippedModel(final ZipInputStream zis) throws IOException {
		final ZipEntry ze = zis.getNextEntry();
		if (ze == null) {
			return null;
		}

		// ignore the directory part of the name
		final String name = new File(ze.getName()).getName();
		assert name.endsWith(XFile.MODEL_SUFFIX) : "name should end with " + XFile.MODEL_SUFFIX
				+ " but is " + name;

		// Read the file into a string.
		final Reader r = new InputStreamReader(zis, UTF8);
		try {
			final String xml = IOUtils.toString(r);
			// Parse the model.
			XModel model;
			try {
				final XydraElement e = new XmlParser().parse(xml);
				model = SerializedModel.toModel(XyAdminApp.ACTOR, null, e);
				return model;
			} catch (final Exception e) {
				throw new RuntimeException("error parsing model file \"" + name + "\"", e);
			}
		} catch (final ZipException e) {
			throw new RuntimeException("Error uncompressing", e);
		}

	}

	static XReadableModel readNonZippedModel(final InputStream is) throws IOException {

		// Read the file into a string.
		final Reader r = new InputStreamReader(is, UTF8);
		final String xml = IOUtils.toString(r);
		// Parse the model.
		XModel model;
		try {
			final XydraElement e = new XmlParser().parse(xml);
			model = SerializedModel.toModel(XyAdminApp.ACTOR, null, e);
			return model;
		} catch (final Exception e) {
			throw new RuntimeException("error parsing model file \"" + "unknown name" + "\"", e);
		}

	}

}

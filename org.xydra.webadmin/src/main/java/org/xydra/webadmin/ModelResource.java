package org.xydra.webadmin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XFile;
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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl3;


public class ModelResource {
	
	public static final Logger log = LoggerFactory.getLogger(ModelResource.class);
	private static final String UTF8 = "UTF-8";
	
	public static enum MStyle {
		html, htmlrev, htmlevents, link, xml, json, xmlhtml, /**
		 * Direct output of
		 * changelog
		 */
		htmlchanges
	}
	
	public static void restless(Restless restless, String prefix) {
		
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

		        new RestlessParameter("repoId"), new RestlessParameter("modelId")

		);
		
	}
	
	@SuppressWarnings("unused")
	public static void command(String repoIdStr, String modelIdStr, String cmdStr,
	        HttpServletRequest req, HttpServletResponse res) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Clock c = new Clock().start();
		XAddress modelAddress = XX.toAddress(XX.toId(repoIdStr), XX.toId(modelIdStr), null, null);
		if(cmdStr.equals("delete")) {
			// to delete: XMODEL: name=/gae-data/phonebook/-/-
			
			// to delete: XCHANGE: name=0/gae-data/phonebook
			
			/*
			 * There is no query to bind them all:
			 * 
			 * SELECT __key__ FROM XCHANGE WHERE __key__ <
			 * KEY('XCHANGE','0/gae-data/'+ modelId ...)
			 */

			// FIXME delete models completely!
		}
	}
	
	public static void index(String repoIdStr, String modelIdStr, String styleStr,
	        String downloadStr, HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Clock c = new Clock().start();
		XAddress modelAddress = XX.toAddress(XX.toId(repoIdStr), XX.toId(modelIdStr), null, null);
		MStyle style = MStyle.valueOf(styleStr);
		boolean download = Boolean.parseBoolean(downloadStr);
		
		if(style == MStyle.xml || style == MStyle.xmlhtml || style == MStyle.json) {
			XydraPersistence p = Utils.getPersistence(modelAddress.getRepository());
			ModelRevision rev = p.getModelRevision(modelAddress);
			log.debug(modelAddress + " rev=" + rev.revision() + " exists:" + rev.modelExists());
			
			if(rev.modelExists()) {
				XWritableModel model = p.getModelSnapshot(modelAddress);
				if(download) {
					String name = modelAddress.getRepository() + "-" + modelAddress.getModel()
					        + "-rev" + model.getRevisionNumber();
					String archivename = Utils.filenameOfNow(name);
					ZipOutputStream zos = Utils.toZipFileDownload(res, archivename);
					
					writeToZipstreamDirectly(model, zos, style);
					
					zos.finish();
				} else {
					if(style == MStyle.xml | style == MStyle.xmlhtml) {
						XmlSerializer serializer = new XmlSerializer();
						ServletUtils.headers(res, 200, 1, serializer.getContentType());
						Writer w = res.getWriter();
						MiniWriter miniwriter = new MiniStreamWriter(w);
						XydraOut out = serializer.create(miniwriter, false);
						out.enableWhitespace(true, true);
						// write xml header manually
						w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
						if(style == MStyle.xmlhtml) {
							// write link to xslt
							w.write("<?xml-stylesheet type=\"text/xsl\" href=\"/s/xml-to-html.xsl\"?>\n");
						}
						// write xml content
						SerializedModel.serialize(model, out);
					} else if(style == MStyle.json) {
						JsonSerializer serializer = new JsonSerializer();
						ServletUtils.headers(res, 200, 1, serializer.getContentType());
						Writer w = res.getWriter();
						serializeToWriter(model, serializer, w);
					}
				}
			} else {
				Writer w = HtmlUtils.startHtmlPage(res, "Model does not exist", new HeadLinkStyle(
				        "/s/xyadmin.css"));
				w.write("<p>Model '" + modelAddress + "' does (currently) not exist.</p>");
				HtmlUtils.endHtmlPage(w);
			}
		} else {
			// html
			Writer w = Utils.writeHeader(res, "Model", modelAddress);
			w.write(HtmlUtils.link(RepositoryResource.link(modelAddress.getRepository()),
			        "Back to repository '" + modelAddress.getRepository() + "'<br/>\n"));
			w.write("<hr/>");
			render(w, modelAddress, style);
			w.flush();
			w.close();
		}
		
		c.stop("index");
		log.info(c.getStats());
	}
	
	public static void update(String repoIdStr, String modelIdStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Clock c = new Clock().start();
		XID repoId = XX.toId(repoIdStr);
		
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Webadmin :: Restore Model",
		        new HeadLinkStyle("/s/xyadmin.css"));
		w.write("Processing upload...</br>");
		w.flush();
		
		InputStream fis = Utils.getMultiPartContentFileAsInputStream(w, req);
		assert fis != null;
		
		ZipInputStream zis = new ZipInputStream(fis);
		w.write("... open zip stream ...</br>");
		w.flush();
		
		XReadableModel model;
		model = readZippedModel(zis);
		
		if(model == null) {
			throw new RuntimeException("Could not read model from ZIS");
		}
		c.stopAndStart("parsed-" + model.getID());
		if(model.getID().equals(XX.toId(modelIdStr))) {
			w.write("... parsed model '" + model.getAddress() + "' [" + model.getRevisionNumber()
			        + "] ...</br>");
			w.flush();
			
			SetStateResult result = ModelResource.setStateFrom(repoId, model, false);
			log.info("" + result);
			c.stopAndStart("applied-" + model.getID());
			w.write("... applied to server repository " + result + ".</br>");
		} else {
			w.write("ModelID of this resource (" + modelIdStr
			        + ") does not match the model ID in the file (" + model.getID() + ")");
		}
		
		w.write("Stats:" + c.getStats());
		log.info("Stats: " + c.getStats());
		w.flush();
		w.close();
	}
	
	static final String getStorageName(XID modelId, MStyle style) {
		/** matches XFile.MODEL_SUFFIX for xml */
		return modelId + ".xmodel." + style;
	}
	
	static void writeToZipstreamDirectly(XWritableModel model, ZipOutputStream zos, MStyle style)
	        throws IOException {
		ZipEntry e = new ZipEntry(getStorageName(model.getID(), style));
		zos.putNextEntry(e);
		OutputStreamWriter w = new OutputStreamWriter(zos, UTF8);
		XydraSerializer serializer = null;
		if(style == MStyle.xml) {
			serializer = new XmlSerializer();
		} else if(style == MStyle.json) {
			serializer = new JsonSerializer();
		}
		serializeToWriter(model, serializer, w);
		zos.closeEntry();
	}
	
	static void writeToZipstreamDirectly(XID modelId, MStyle style, String serialisation,
	        ZipOutputStream zos) throws IOException {
		ZipEntry e = new ZipEntry(getStorageName(modelId, style));
		zos.putNextEntry(e);
		OutputStreamWriter w = new OutputStreamWriter(zos, UTF8);
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
	 * @throws IOException
	 */
	static String computeSerialisation(XWritableModel model, MStyle style) {
		StringWriter sw = new StringWriter();
		XydraSerializer serializer = null;
		if(style == MStyle.xml) {
			serializer = new XmlSerializer();
		} else if(style == MStyle.json) {
			serializer = new JsonSerializer();
		}
		serializeToWriter(model, serializer, sw);
		try {
			sw.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		String serialised = sw.getBuffer().toString();
		return serialised;
	}
	
	private static void serializeToWriter(XReadableModel model, XydraSerializer serializer, Writer w) {
		MiniWriter miniwriter = new MiniStreamWriter(w);
		XydraOut out = serializer.create(miniwriter);
		out.enableWhitespace(true, true);
		SerializedModel.serialize(model, out);
		out.flush();
		assert out.isClosed();
	}
	
	public static void render(Writer w, XAddress modelAddress, MStyle style) throws IOException {
		w.write(

		HtmlUtils.link("/admin" + WebadminResource.XYADMIN + "/"
		        + modelAddress.getRepository().toString())
		        + " | "
		        +

		        HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.html,
		                modelAddress.toString())
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlrev, "Rev")
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlevents, "Events")
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.htmlchanges, "Change log")
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xmlhtml, "XML-as-HTML")
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xml, "XML")
		        
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.xml + "&download=true",
		                ".zip")
		        
		        + " | "
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.json, "JSON")
		        
		        + HtmlUtils.link(link(modelAddress) + "?style=" + MStyle.json + "&download=true",
		                ".zip")

		        + "<br/>\n");
		
		if(style == MStyle.htmlevents || style == MStyle.htmlrev || style == MStyle.html) {
			
			// upload form
			w.write(""
			        + HtmlUtils.form(METHOD.POST, link(modelAddress)).withInputFile("backupfile")
			                .withInputSubmit("Upload and set as current state"));
		}
		
		if(style == MStyle.htmlrev || style == MStyle.htmlevents) {
			XydraPersistence p = Utils.getPersistence(modelAddress.getRepository());
			ModelRevision rev = p.getModelRevision(modelAddress);
			w.write("rev=" + rev.revision() + " exists:" + rev.modelExists() + "<br/>\n");
			w.flush();
			if(style == MStyle.htmlevents) {
				List<XEvent> events = p.getEvents(modelAddress, 0, rev.revision());
				XydraHtmlUtils.writeEvents(events, w);
			} else if(style == MStyle.htmlchanges) {
				GaeChangesServiceImpl3.renderChangeLog(modelAddress, w);
				w.flush();
			}
		}
		
	}
	
	public static String link(XAddress modelAddress) {
		return "/admin" + WebadminResource.XYADMIN + "/" + modelAddress.getRepository() + "/"
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
	 * @param repoId where to add
	 * @param model which has a weird address with NO repositoryId
	 * @param overwriteIfSameRevPresent if true, content in repo is overwritten
	 *            even if the same revNr is found
	 * @return some statistical information
	 */
	public static SetStateResult setStateFrom(XID repoId, XReadableModel model,
	        boolean overwriteIfSameRevPresent) {
		log.debug("Set state from " + model.getAddress() + " to " + repoId);
		XydraPersistence p = Utils.getPersistence(repoId);
		SetStateResult result = new SetStateResult();
		
		XID actor = XX.toId("ModelResource");
		XAddress modelAddress = XX.resolveModel(repoId, model.getID());
		
		ModelRevision modelRev = p.getModelRevision(modelAddress);
		if(modelRev != null) {
			if(!overwriteIfSameRevPresent && model.getRevisionNumber() == modelRev.revision()) {
				log.debug("Model already stored.");
				result.changes = false;
				result.modelExisted = true;
				result.debug = "modelAlreadyStored";
				return result;
			}
		}
		// else
		XReadableModel oldModel = p.getModelSnapshot(modelAddress);
		if(oldModel != null) {
			result.modelExisted = true;
		} else {
			result.modelExisted = false;
			// FIXME concurrency: move createCommand into transaction
			XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
			        XX.resolveRepository(repoId), XCommand.FORCED, model.getID());
			long cmdResult = p.executeCommand(actor, createCommand);
			assert cmdResult >= 0;
			oldModel = new SimpleModel(modelAddress, 0);
		}
		assert oldModel != null;
		
		XTransactionBuilder tb = new XTransactionBuilder(modelAddress);
		tb.changeModel(oldModel, model);
		// TODO or use the change log if the model has one?
		if(tb.isEmpty()) {
			if(result.modelExisted) {
				result.changes = false;
			} else {
				// only 1 change: creating the new empty model
				result.changes = true;
			}
		} else {
			result.changes = true;
			long cmdResult = p.executeCommand(actor, tb.build());
			if(cmdResult == XCommand.FAILED) {
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
	
	static XReadableModel readZippedModel(ZipInputStream zis) throws IOException {
		ZipEntry ze = zis.getNextEntry();
		if(ze == null) {
			return null;
		}
		
		// ignore the directory part of the name
		String name = new File(ze.getName()).getName();
		assert name.endsWith(XFile.MODEL_SUFFIX);
		
		// Read the file into a string.
		Reader r = new InputStreamReader(zis, UTF8);
		String xml = IOUtils.toString(r);
		
		// Parse the model.
		XModel model;
		try {
			XydraElement e = new XmlParser().parse(xml);
			model = SerializedModel.toModel(WebadminResource.ACTOR, null, e);
		} catch(Exception e) {
			throw new RuntimeException("error parsing model file \"" + name + "\"", e);
		}
		
		return model;
	}
	
}

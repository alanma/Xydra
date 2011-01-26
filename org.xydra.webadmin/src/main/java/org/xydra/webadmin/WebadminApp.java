package org.xydra.webadmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.XFile;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XModel;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.XmlOut;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStream;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


/**
 * Run this either by configuring your Restless servlet to run this
 * {@link WebadminApp} or call the
 * {@link WebadminApp#restless(Restless, String)} init method from your other
 * restless init code.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class WebadminApp {
	
	public static final Logger log = LoggerFactory.getLogger(WebadminApp.class);
	
	public static void restless(Restless restless, String prefix) {
		
		XydraRestServer.initializeServer(restless);
		
		restless.addMethod(prefix + "/backup", "GET", WebadminApp.class, "backup", true,
		        new RestlessParameter("logs"));
		
		restless.addMethod(prefix + "/restore", "POST", WebadminApp.class, "restore", true);
		
		restless.addMethod(prefix + "/", "GET", WebadminApp.class, "index", true);
	}
	
	public void index(HttpServletResponse res) throws IOException {
		// load index.html and return
		InputStream in = WebadminApp.class.getClassLoader().getResourceAsStream(
		        "org/xydra/webadmin/index.html");
		InputStreamReader isr = new InputStreamReader(in, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		while(line != null) {
			// copy line & output
			res.getWriter().write(line + "\r\n");
			line = br.readLine();
		}
	}
	
	public void backup(Restless restless, HttpServletResponse res, String logs) throws IOException {
		
		boolean includeLogs = logs == null ? false : Boolean.valueOf(logs);
		
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		
		log.info("request for xydra backup");
		int count = 0;
		Iterator<XID> it = server.iterator();
		while(it.hasNext()) {
			it.next();
			count++;
		}
		log.info("backing up " + count + " models");
		
		ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
		
		// Send the correct response headers.
		res.setContentType("application/zip");
		
		// Suggest an appropriate filename.
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		
		String archiveName = "xydra-backup";
		if(includeLogs) {
			archiveName += "+logs";
		}
		archiveName += "-" + sdf.format(now) + ".zip";
		
		res.addHeader("Content-Disposition", "attachment; filename=\"" + archiveName + "\"");
		
		// Add the models to the zip archive.
		for(XID modelId : server) {
			
			String filename = URLEncoder.encode(modelId.toString(), "UTF-8") + XFile.MODEL_SUFFIX;
			
			// Start a new entry in the zip archive.
			ZipEntry ze = new ZipEntry(filename);
			zos.putNextEntry(ze);
			
			log.info("adding model \"" + modelId.toString() + "\" as \"" + filename + "\"");
			
			XmlOut out = new XmlOutStream(zos);
			XmlModel.toXml(server.getModelSnapshot(modelId), out, true, false, includeLogs);
			out.flush();
			
			zos.closeEntry();
		}
		
		zos.finish();
		
		log.info("done creating backup archive");
		
	}
	
	public void restore(Restless restless, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		
		log.debug("restore from backup request");
		
		if(!ServletFileUpload.isMultipartContent(req)) {
			log.debug("not a multipart content => ignored");
			throw new RuntimeException("Expected File Upload");
		}
		
		ServletFileUpload upload = new ServletFileUpload();
		
		InputStream fis = null;
		
		// Parse the request
		try {
			FileItemIterator iter = upload.getItemIterator(req);
			while(iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if(!item.isFormField() && name.equals("archive")) {
					fis = stream;
					break;
				}
			}
		} catch(FileUploadException fue) {
			log.error("Error handling file upload", fue);
			throw new RuntimeException("Error handling file upload", fue);
		}
		
		if(fis == null) {
			log.debug("no file found");
			throw new RuntimeException("No File Uploaded");
		}
		
		ZipInputStream zis = new ZipInputStream(fis);
		
		// Statistics.
		int count = 0;
		int overwritten = 0;
		int ignoreSuffix = 0;
		int nochange = 0;
		
		ZipEntry ze;
		while((ze = zis.getNextEntry()) != null) {
			
			// ignore the directory part of the name
			String name = new File(ze.getName()).getName();
			
			// Check suffix.
			if(!name.endsWith(XFile.MODEL_SUFFIX)) {
				ignoreSuffix++;
				continue;
			}
			
			// Read the file into a string.
			Reader r = new InputStreamReader(zis, "UTF-8");
			StringBuilder b = new StringBuilder();
			char[] buf = new char[1024];
			int read;
			while((read = r.read(buf)) >= 0) {
				b.append(buf, 0, read);
			}
			
			// Parse the model.
			XModel model;
			try {
				MiniElement e = new MiniXMLParserImpl().parseXml(b.toString());
				model = XmlModel.toModel(XX.toId("WebadminApp"), null, e);
			} catch(Exception e) {
				throw new RuntimeException("error parsing model file \"" + name + "\"", e);
			}
			
			XID actor = null; // TODO
			
			boolean existed = false;
			XReadableModel oldModel = server.getModelSnapshot(model.getID());
			if(oldModel != null) {
				existed = true;
				overwritten++;
			} else {
				XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(server
				        .getRepositoryAddress(), XCommand.FORCED, model.getID());
				server.executeCommand(createCommand, actor);
				oldModel = server.getModelSnapshot(model.getID());
			}
			
			// FIXME concurrency: move createCommand into transaction
			
			XTransactionBuilder tb = new XTransactionBuilder(oldModel.getAddress());
			tb.changeModel(oldModel, model);
			// TODO or use the change log if the model has one?
			
			if(tb.isEmpty()) {
				if(existed) {
					nochange++;
				} else {
					count++;
				}
				continue;
			}
			
			long result = server.executeCommand(tb.build(), actor);
			
			if(result == XCommand.FAILED) {
				// can only happen if model changed since
				// tb.changeModel(oldModel, model);
				throw new RuntimeException("error restoring model \"" + name + "\" ("
				        + model.getID() + "), transaction failed");
			}
			
			// Check if a discussion with that name already exists.
			
			count++;
		}
		
		Writer w = res.getWriter();
		
		w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
		        + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
		        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
		        + "<head><title>Xydra Webadmin - Restored</title></head><body>");
		
		w.write("Restored " + count + " models<br/>");
		w.write("Overwritten: " + count + "<br/>");
		w.write("Files ignored: " + ignoreSuffix + "<br/>");
		w.write("Unchanged: " + nochange + "<br/>");
		
		w.write("</body></html>");
		
		log.info("restore done: count=" + count + "; overwritten=" + overwritten
		        + "; ignoreSuffix=" + ignoreSuffix + "; nochange=" + nochange);
		
	}
	
}

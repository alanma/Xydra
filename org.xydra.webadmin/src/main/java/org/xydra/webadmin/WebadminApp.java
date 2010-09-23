package org.xydra.webadmin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XFile;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
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


@RunsInAppEngine
@RunsInJava
public class WebadminApp {
	
	public static final Logger log = LoggerFactory.getLogger(WebadminApp.class);
	
	public void restless(Restless restless, String prefix) {
		
		XydraRestServer.initializeServer(restless);
		
		restless.addMethod("/admin/backup", "GET", WebadminApp.class, "backup", true,
		        new RestlessParameter("logs"));
		restless.addMethod("/admin/restore", "POST", WebadminApp.class, "restore", true);
		
	}
	
	public void backup(Restless restless, HttpServletResponse res, String logs) throws IOException {
		
		boolean includeLogs = logs == null ? false : Boolean.valueOf(logs);
		
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XRepository repo = server.getRepository();
		
		log.info("request for xydra backup");
		
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
		for(XID modelId : repo) {
			
			String filename = URLEncoder.encode(modelId.toURI(), "UTF-8") + XFile.MODEL_SUFFIX;
			
			// Start a new entry in the zip archive.
			ZipEntry ze = new ZipEntry(filename);
			zos.putNextEntry(ze);
			
			log.info("adding model \"" + modelId.toString() + "\" as \"" + filename + "\"");
			
			XmlOut out = new XmlOutStream(zos);
			XmlModel.toXml(repo.getModel(modelId), out, true, false, includeLogs);
			out.flush();
			
			zos.closeEntry();
		}
		
		zos.finish();
		
		log.info("done creating backup archive");
		
	}
	
	public void restore(Restless restless, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XRepository repo = server.getRepository();
		
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
			name = name.substring(0, name.length() - XFile.MODEL_SUFFIX.length());
			
			name = URLDecoder.decode(name, "UTF-8");
			
			// Read the file into a string.
			Reader r = new InputStreamReader(zis, "UTF-8");
			StringBuilder b = new StringBuilder();
			char[] buf = new char[1024];
			int read;
			while((read = r.read(buf)) >= 0) {
				b.append(buf, 0, read);
			}
			
			// TODO handle errors
			
			// Parse the model.
			MiniElement e = new MiniXMLParserImpl().parseXml(b.toString());
			XModel model = XmlModel.toModel(e);
			
			boolean existed = false;
			if(repo.hasModel(model.getID())) {
				existed = true;
				overwritten++;
			}
			
			XID actor = null; // TODO
			XModel oldModel = repo.createModel(actor, model.getID());
			
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
			
			oldModel.executeTransaction(actor, tb.build());
			
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

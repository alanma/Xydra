package org.xydra.webadmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;


public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static void writeDefaultInfos(Writer w) throws IOException {
		String instance = AboutAppEngine.getInstanceId() + "---" + AboutAppEngine.getThreadInfo();
		w.write(instance + "<br/>\n");
	}
	
	public static XydraPersistence getPersistence(XID repoId) {
		return new GaePersistence(repoId);
	}
	
	public static Writer writeHeader(HttpServletResponse res, String xtype, XAddress xa)
	        throws IOException {
		String title = xtype + " " + xa;
		Writer w = HtmlUtils.startHtmlPage(res, title, new HeadLinkStyle("/s/xyadmin.css"));
		Utils.writeDefaultInfos(w);
		w.write("<h3>" + title + "</h3>\n");
		w.flush();
		return w;
	}
	
	/**
	 * A ZOS is used like this
	 * 
	 * <pre>
	 * For each entry:
	 * ...
	 * ZipEntry ze = new ZipEntry(filename);
	 * zos.putNextEntry(ze);
	 * write bytes to zos
	 * zos.closeEntry();
	 * ...
	 * zos.finish();
	 * </pre>
	 * 
	 * @param res ..
	 * @param archivename '.zip' is added automatically
	 * @return a ZipOutputStream to which the caller should write his data. It
	 *         will end up in a downloadable zip file.
	 * @throws IOException ...
	 */
	public static ZipOutputStream toZipFileDownload(HttpServletResponse res, String archivename)
	        throws IOException {
		String fullArchiveName = archivename + ".zip";
		
		log.info("Wrapping in zipfile named " + fullArchiveName);
		
		// Send the correct response headers.
		res.setContentType("application/zip");
		res.addHeader("Content-Disposition", "attachment; filename=\"" + fullArchiveName + "\"");
		
		ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
		return zos;
	}
	
	/**
	 * @param name ..
	 * @return name + 'yyyy-MM-dd-HH-mm-ss' (as of now)
	 */
	public static String filenameOfNow(String name) {
		// Suggest an appropriate filename.
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return name + "-" + sdf.format(now);
	}
	
	public static InputStream getMultiPartContentFileAsInputStream(Writer w, HttpServletRequest req)
	        throws IOException {
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
				if(!item.isFormField()) {
					fis = stream;
					w.write("... found file '" + name + "'</br>");
					w.flush();
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
		return fis;
	}
	
}

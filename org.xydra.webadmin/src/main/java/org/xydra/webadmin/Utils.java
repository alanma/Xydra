package org.xydra.webadmin;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;


public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static XydraPersistence getPersistence(XID repoId) {
		return new GaePersistence(repoId);
	}
	
	/**
	 * TODO move to restless
	 * 
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
	 * This sets no "Content-Type" headers.
	 * 
	 * @param res
	 * @param archivename
	 * @param extension
	 * @param contentType e.g. 'application/zip' or 'text/csv'
	 * @return an outputstream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(HttpServletResponse res, String archivename,
	        String extension, String contentType) throws IOException {
		String fullFileName = archivename + "." + extension;
		log.info("Wrapping in file named " + fullFileName);
		res.addHeader("Content-Disposition", "attachment; filename=\"" + fullFileName + "\"");
		OutputStream os = res.getOutputStream();
		return os;
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
	
	// /**
	// * @param w
	// * @param req
	// * @return ...
	// * @throws IOException
	// * @deprecated This is now done via restless _upload_ and type byte[]
	// */
	// @Deprecated
	// public static InputStream getMultiPartContentFileAsInputStream(Writer w,
	// HttpServletRequest req)
	// throws IOException {
	// if(!ServletFileUpload.isMultipartContent(req)) {
	// log.debug("not a multipart content => ignored");
	// throw new RuntimeException("Expected File Upload");
	// }
	//
	// ServletFileUpload upload = new ServletFileUpload();
	// InputStream fis = null;
	// // Parse the request
	// try {
	// FileItemIterator iter = upload.getItemIterator(req);
	// while(iter.hasNext()) {
	// FileItemStream item = iter.next();
	// String name = item.getFieldName();
	// InputStream stream = item.openStream();
	// if(!item.isFormField()) {
	// fis = stream;
	// w.write("... found file '" + name + "'</br>");
	// w.flush();
	// break;
	// }
	// }
	// } catch(FileUploadException fue) {
	// log.error("Error handling file upload", fue);
	// throw new RuntimeException("Error handling file upload", fue);
	// }
	//
	// if(fis == null) {
	// log.debug("no file found");
	// throw new RuntimeException("No File Uploaded");
	// }
	// return fis;
	// }
	
}

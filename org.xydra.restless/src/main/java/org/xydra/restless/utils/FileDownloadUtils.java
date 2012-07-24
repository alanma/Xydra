package org.xydra.restless.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class FileDownloadUtils {
	
	/*
	 * FIXME most methods do not specify what happens when given parameters
	 * equal null or how this is handled.
	 * 
	 * TODO -> Mark parameters with @NeverNull and @CanBeNull in the method
	 * signatures and the JavaDoc (not just in this class, but everywhere)
	 */
	
	/*
	 * TODO is synchronization on response objects really necessary? Are
	 * responses shared between different calls to different methods?
	 */
	
	private static final Logger log = LoggerFactory.getLogger(FileDownloadUtils.class);
	
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
		
		synchronized(res) {
			
			res.setContentType("application/zip");
			res.addHeader("Content-Disposition", "attachment; filename=\"" + fullArchiveName + "\"");
			
			ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
			return zos;
		}
	}
	
	/**
	 * This sets no "Content-Type" headers.
	 * 
	 * @param res
	 * @param archivename
	 * @param extension
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(HttpServletResponse res, String archivename,
	        String extension) throws IOException {
		
		synchronized(res) {
			String fullFileName = archivename + "." + extension;
			log.info("Wrapping in file named " + fullFileName);
			res.addHeader("Content-Disposition", "attachment; filename=\"" + fullFileName + "\"");
			OutputStream os = res.getOutputStream();
			return os;
		}
	}
	
	/**
	 * This sets also "Content-Type" headers.
	 * 
	 * @param res
	 * @param archivename
	 * @param extension
	 * @param contentType e.g. 'application/zip' or 'text/csv'
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(HttpServletResponse res, String archivename,
	        String extension, String contentType) throws IOException {
		/*
		 * via
		 * http://stackoverflow.com/questions/398237/how-to-use-the-csv-mime-
		 * type
		 */
		synchronized(res) {
			ServletUtils.headers(res, contentType);
			return toFileDownload(res, archivename, extension);
		}
	}
	
}

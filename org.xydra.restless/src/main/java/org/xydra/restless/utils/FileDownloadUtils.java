package org.xydra.restless.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


@ThreadSafe
public class FileDownloadUtils {
	
	/*
	 * TODO is it really okay if archivenames and extensions are null?
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
	 * @param archivename '.zip' is added automatically @CanBeNull
	 * @return a ZipOutputStream to which the caller should write his data. It
	 *         will end up in a downloadable zip file. @NeverNull
	 * @throws IOException ...
	 */
	
	public static ZipOutputStream toZipFileDownload(@NeverNull HttpServletResponse res,
	        @CanBeNull String archivename) throws IOException {
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
	 * @param res @NeverNull
	 * @param archivename @CanBeNull
	 * @param extension @CanBeNull
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(@NeverNull HttpServletResponse res,
	        @CanBeNull String archivename, @CanBeNull String extension) throws IOException {
		
		String fullFileName = archivename + "." + extension;
		log.info("Wrapping in file named " + fullFileName);
		res.addHeader("Content-Disposition", "attachment; filename=\"" + fullFileName + "\"");
		OutputStream os = res.getOutputStream();
		return os;
		
	}
	
	/**
	 * This sets also "Content-Type" headers.
	 * 
	 * @param res @NeverNull
	 * @param archivename @CanBeNull
	 * @param extension @CanBeNull
	 * @param contentType e.g. 'application/zip' or 'text/csv' @NeverNull
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(@NeverNull HttpServletResponse res,
	        @CanBeNull String archivename, @CanBeNull String extension,
	        @NeverNull String contentType) throws IOException {
		/*
		 * via
		 * http://stackoverflow.com/questions/398237/how-to-use-the-csv-mime-
		 * type
		 */
		ServletUtils.headers(res, contentType);
		return toFileDownload(res, archivename, extension);
	}
	
}

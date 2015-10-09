package org.xydra.restless.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

@ThreadSafe
public class FileDownloadUtils {

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
	 * @param res
	 *            ..
	 * @param archivename
	 *            '.zip' is added automatically @NeverNull
	 * @return a ZipOutputStream to which the caller should write his data. It
	 *         will end up in a downloadable zip file. @NeverNull
	 * @throws IOException
	 *             ...
	 */

	public static ZipOutputStream toZipFileDownload(@NeverNull final HttpServletResponse res,
			@NeverNull final String archivename) throws IOException {
		final String fullArchiveName = archivename + ".zip";

		log.info("Wrapping in zipfile named " + fullArchiveName);

		// Send the correct response headers.

		res.setContentType("application/zip");
		res.addHeader("Content-Disposition", "attachment; filename=\"" + fullArchiveName + "\"");

		final ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
		return zos;
	}

	/**
	 * This sets no "Content-Type" headers.
	 *
	 * @param res @NeverNull
	 * @param archivename @NeverNull
	 * @param extension @NeverNull
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(@NeverNull final HttpServletResponse res,
			@NeverNull final String archivename, @NeverNull final String extension) throws IOException {

		final String fullFileName = archivename + "." + extension;
		log.info("Wrapping in file named " + fullFileName);
		res.addHeader("Content-Disposition", "attachment; filename=\"" + fullFileName + "\"");
		final OutputStream os = res.getOutputStream();
		return os;

	}

	/**
	 * This sets also "Content-Type" headers.
	 *
	 * @param res @NeverNull
	 * @param archivename @NeverNull
	 * @param extension @NeverNull
	 * @param contentType
	 *            e.g. 'application/zip' or 'text/csv' @NeverNull
	 * @return an OutputStream to which you can write
	 * @throws IOException
	 */
	public static OutputStream toFileDownload(@NeverNull final HttpServletResponse res,
			@NeverNull final String archivename, @NeverNull final String extension,
			@NeverNull final String contentType) throws IOException {
		/*
		 * via
		 * http://stackoverflow.com/questions/398237/how-to-use-the-csv-mime-
		 * type
		 */
		ServletUtils.headers(res, contentType);
		return toFileDownload(res, archivename, extension);
	}

}

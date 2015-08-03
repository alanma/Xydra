package org.xydra.gwttools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 *
 *
 * @author xamde
 *
 */
public class GwtBuildHelper implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GwtBuildHelper.class);

	/**
	 * Copy from target dir to src, so that a local Jetty works
	 *
	 * @param warPath
	 *            e.g. './target/myname-0.1.2-SNAPSHOT'
	 * @param moduleName
	 *            e.g. 'mygwtmodule'
	 */
	public static void copyCompiledGwtModule(final String warPath, final String moduleName) {
		final File targetGwt = new File(warPath + "/" + moduleName);
		if (!targetGwt.exists()) {
			log.error("GWT data not found in " + targetGwt.getAbsolutePath()
					+ ". Some AJAX will not work. \n Please run 'mvn gwt:compile' first"
					+ "Or make sure your module has the correct rename-to entry.");
			System.exit(1);
		}
		assert targetGwt.isDirectory();
		final File sourceWebAppGwt = new File("./src/main/webapp/" + moduleName);
		assert sourceWebAppGwt.getParentFile().exists() : "Found no folder "
				+ sourceWebAppGwt.getParentFile().getAbsolutePath();
		log.info("Copying GWT files temporarily to " + sourceWebAppGwt.getAbsolutePath());
		FileUtils.deleteQuietly(sourceWebAppGwt);
		sourceWebAppGwt.mkdirs();
		try {
			FileUtils.copyDirectory(targetGwt, sourceWebAppGwt);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param dir
	 * @return true if the given dir looks like a folder in which GWT code was
	 *         compiled
	 */
	static boolean looksLikeGwtDir(final File dir) {
		final File clearCache = new File(dir, "clear.cache.gif");
		final File hosted = new File(dir, "hosted.html");
		return clearCache.exists() && hosted.exists();
	}

	/**
	 * Copies a source file to a target file
	 *
	 * @param srcFile
	 * @param targetFile
	 */
	public static void copyFile(final String srcFile, final String targetFile) {
		final File src = new File(srcFile);
		final File target = new File(targetFile);
		try {
			FileUtils.copyFile(src, target);
		} catch (final IOException e) {
			log.error("Could not copy from '" + srcFile + "' to '" + targetFile + "'", e);
		}
	}

	/**
	 * @param targetDir
	 * @return a list of all directories that are a) direct children of the
	 *         given targetDir and b) look like they contain compiled GWT code; @NeverNull
	 */
	public static List<File> autoDiscoverAllGwtModulesInTarget(final File targetDir) {
		final List<File> list = new ArrayList<File>();

		assert targetDir.isDirectory();
		final File[] subDirs = targetDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(final File pathname) {
				return pathname.isDirectory();
			}
		});

		for (final File f : subDirs) {
			if (f.getName().contains(".gen.")) {
				log.info("Found GWT module "
						+ f.getAbsolutePath()
						+ "\n"
						+ "  but ignoring it, because it looks like an auto-generated domain model, from Xydra OO");
			}
			list.add(f);
		}

		return list;
	}

	/**
	 * Scans warPath and copies all auto-detected GWT compile-result module
	 * folders to src/main/webapp/{folder}
	 *
	 * @param warPath
	 *            to be scanned
	 */
	public static void copyAllGwtModulesFoundInTarget(final String warPath) {
		final File targetDir = new File(warPath);
		if (!targetDir.exists()) {
			log.error("Target WAR dir not found as \n" + "  " + targetDir.getAbsolutePath() + "\n"
					+ "Compile something first, e.g. call 'mvn compile'\n"
					+ " - and make sure your pom is packaging=war.");
			System.exit(1);
		}
		final List<File> subDirs = autoDiscoverAllGwtModulesInTarget(targetDir);
		if (subDirs.isEmpty()) {
			log.warn("No subdirectories found in " + targetDir.getAbsolutePath()
					+ " - nothing copied.");
			return;
		}
		assert subDirs != null;
		for (final File subDir : subDirs) {
			if (looksLikeGwtDir(subDir)) {
				copyCompiledGwtModule(warPath, subDir.getName());
			}
		}
	}

	public GwtBuildHelper(final String warPath) {
		super();
		this.warPath = warPath;
	}

	private final String warPath;

	@Override
	public void run() {
		copyAllGwtModulesFoundInTarget(this.warPath);
	}

}

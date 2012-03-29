package org.xydra.testgae;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class CopyGwt {
	
	private static final Logger log = LoggerFactory.getLogger(CopyGwt.class);
	
	public static void copyGwt() {
		// need to copy GWT stuff
		// TODO kill copyCompiledGwtModule("./target/favr-0.2.5-SNAPSHOT",
		// "gwtfrontpage");
		copyCompiledGwtModule("./target/testgae-0.1.5-SNAPSHOT", "gwt");
	}
	
	/**
	 * @param warPath e.g. './target/favr-0.2.5-SNAPSHOT'
	 * @param moduleName e.g. 'frontpage'
	 */
	private static void copyCompiledGwtModule(String warPath, String moduleName) {
		File targetGwt = new File(warPath + "/" + moduleName);
		if(!targetGwt.exists()) {
			log.warn("GWT data not found in " + targetGwt.getAbsolutePath()
			        + ".Some AJAX will not work. \n Please run 'mvn gwt:compile' first");
		} else {
			assert targetGwt.isDirectory();
			File sourceWebAppGwt = new File("./src/main/webapp/" + moduleName);
			assert sourceWebAppGwt.getParentFile().exists();
			log.info("Copying GWT files temporarily to " + sourceWebAppGwt.getAbsolutePath());
			FileUtils.deleteQuietly(sourceWebAppGwt);
			sourceWebAppGwt.mkdirs();
			try {
				FileUtils.copyDirectory(targetGwt, sourceWebAppGwt);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void main(String[] args) {
		CopyGwt.copyGwt();
	}
	
}

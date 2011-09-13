package org.xydra.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class LogUtils {
	
	private static final Logger log = LoggerFactory.getLogger(LogUtils.class);
	
	public static void configureLog4j() {
		File file = new File("./src/test/resources/log4j.properties");
		if(!file.exists()) {
			log.warn("Could not update log conf at runtime from file '" + file.getAbsolutePath()
			        + "' -- not found");
		}
		Properties props = new Properties();
		Reader r;
		try {
			r = new FileReader(file);
			props.load(r);
			r.close();
			// TODO Do we really want that?
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(props);
			log.info("Updated local log config from " + file.getAbsolutePath());
		} catch(FileNotFoundException e) {
		} catch(IOException e) {
		}
		
	}
	
}

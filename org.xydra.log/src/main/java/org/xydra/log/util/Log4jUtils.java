package org.xydra.log.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Utility class for managing log4j.properties files. If some bundled jar
 * contains a log4j.properties (e.g. it might come in from a /src/test/resources
 * folder), then the first log4j.properties found on the classpath is used. To
 * fix that, this class allows to explicitly load a local file from
 * /src/main/resources and apply the config listed in there.
 * 
 * @author xamde
 */
public class Log4jUtils {

	private static final Logger log = LoggerFactory.getLogger(Log4jUtils.class);

	/**
	 * Use local FILE (not resource)
	 * './src/{main,test}/resources/log4j.properties' to overwrite log4j
	 * settings
	 */
	public static void configureLog4j() {
		File file = new File("./src/main/resources/log4j.properties");
		if (!file.exists()) {
			file = new File("./src/test/resources/log4j.properties");
		}
		if (!file.exists()) {
			log.warn("Logging: Could not update log conf at runtime from file '"
					+ file.getAbsolutePath() + "' -- not found");
		}
		Properties props = new Properties();
		Reader r;
		try {
			r = new FileReader(file);
			props.load(r);
			r.close();
			PropertyConfigurator.configure(props);
			log.info("Logging: Updated local log config from " + file.getAbsolutePath());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

	}

	/**
	 * Dump log4j.properties resource from classpath to System.out
	 * 
	 * @throws IOException
	 */
	public static void listConfigFromClasspath() throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final String name = "log4j.properties";
		InputStream in = cl.getResourceAsStream(name);
		if (in == null) {
			System.out.println("System.out: Found not log4j.properties on classpath.");
			return;
		}
		// found!
		URL url = cl.getResource(name);
		System.out.println("Found in " + url.toString());
		Reader r = new InputStreamReader(in, "utf-8");
		String s = toString(r);
		System.out.println("System.out: Found config:\n" + s);
	}

	/**
	 * This is not fast (hence not public) and only used to dump an internal
	 * config
	 * 
	 * @param r
	 * @return
	 */
	private static String toString(Reader r) {
		StringBuilder b = new StringBuilder();
		int c;
		try {
			do {
				c = r.read();
				if (c >= 0) {
					b.append((char) c);
				}
			} while (c >= 0);
			return b.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws IOException {
		listConfigFromClasspath();
	}

	/**
	 * Calls the internal log4j configuration reset
	 */
	public static void resetLog4jConfig() {
		LogManager.resetConfiguration();
		log.info("Logging: Log4j config resetted");
	}

	/**
	 * Tweak log level at runtime
	 * 
	 * @param clazz
	 * @param log4jLevel
	 */
	public static void setLevel(Class<?> clazz, Level log4jLevel) {
		LogManager.getLogger(clazz).setLevel(log4jLevel);
	}

	public static void setRootLevel(Level log4jLevel) {
		LogManager.getRootLogger().setLevel(log4jLevel);
	}
}

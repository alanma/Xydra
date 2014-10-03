package org.xydra.log.conf;

import org.xydra.conf.IConfig;
import org.xydra.env.Env;
import org.xydra.log.api.Logger;
import org.xydra.log.api.Logger.Level;
import org.xydra.log.api.LoggerFactory;

public class LogConfTool {

	private static final Logger log = LoggerFactory.getLogger(LogConfTool.class);

	/**
	 * Processes IConfig and applies all defined log levels. They are defined
	 * via 'log.(CLASSNAME)' = String or {@link Level}.
	 */
	public static void applyLogConf() {
		IConfig conf = Env.get().conf();

		for (String key : conf.getDefinedKeys()) {
			if (key.startsWith("log.")) {
				String logKey = key.substring("log.".length());
				if (logKey.length() == 0) {
					log.warn("Found nonsense log key: '" + key + "'");
					continue;
				}

				Class<?> clazz;
				try {
					clazz = Class.forName(logKey);
					Logger logger = LoggerFactory.getLogger(clazz);
					Object o = conf.get(key);
					// we are tolerant here
					Level level;
					if (o instanceof String) {
						level = Level.valueOf((String) o);
					} else if (o instanceof Level) {
						level = (Level) o;
					} else {
						log.warn("Could not set unparsable level '" + o
								+ "'. Use string or Level type");
						continue;
					}
					log.trace("Setting logger " + logKey + "=" + level);
					logger.setLevel(level);
				} catch (ClassNotFoundException e) {
					log.warn("Log key '" + logKey + "' has no corresponding class", e);
					continue;
				}
			}
		}
	}

}

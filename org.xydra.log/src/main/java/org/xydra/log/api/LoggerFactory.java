package org.xydra.log.api;

import org.xydra.annotations.Setting;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.Logger.Level;
import org.xydra.log.coreimpl.sysout.DefaultLoggerFactorySPI;
import org.xydra.log.spi.ILoggerFactorySPI;

import java.util.HashSet;
import java.util.Set;

/**
 * The basic logger factory. Same signature to obtain a {@link Logger} as slf4j.
 * You only need to change the import to switch from one logging framework to
 * the other.
 * 
 * <pre>
 * private static final Logger log = LoggerFactory.getLogger(Foo.class);
 * </pre>
 * 
 * <h2>How To Solve Log Config Problems</h2>
 * 
 * Set {@link LoggerFactory#DEBUG_LOG_CONFIG} to true to see the stacktrace of
 * the calls that cause the log system to fall-back to auto-config.
 * 
 * 
 * @author voelkel
 */
@ThreadSafe
public class LoggerFactory {

	/**
	 * The logger name used for the log system to log about itself.
	 */
	private static final String LOGGER_NAME_SELF_LOGGING = "org.xydra.log.system";

	/**
	 * The internal Service Provider. During init, one provider is set. For
	 * logging, there must exactly one provider be active.
	 */
	private static ILoggerFactorySPI loggerFactorySPI;

	/**
	 * Set of log listeners, is passed to each {@link Logger} instance, which is
	 * created from there on.
	 */
	private static Set<ILogListener> logListeners = new HashSet<ILogListener>();

	private static Logger SELF_LOGGER = null;

	private static Exception lastCallerException;

	/**
	 * All log messages are also sent to the registered {@link ILogListener}.
	 * This is only effective for Logger instances, created after this setting.
	 * 
	 * @param logListener
	 *            a listener to receive all log messages
	 */
	public static synchronized void addLogListener(ILogListener logListener) {
		ensureLoggerFactoryDefined();
		LoggerFactory.logListeners.add(logListener);
		getSelfLogger().info("Logging: Attached log listener " + logListener.getClass().getName());
	}

	@Setting("to debug the config of the log system itself")
	public static boolean DEBUG_LOG_CONFIG = false;

	private static synchronized void ensureLoggerFactoryDefined() {
		if (loggerFactorySPI == null) {
			/**
			 * set fall-back logger factory to display log messages anyhow -
			 * they will be mostly about logging and configuration
			 */
			loggerFactorySPI = new DefaultLoggerFactorySPI();

			getSelfLogger()
					.info("Logging: Log system started with built-in logger to System.out. Expecting config via LoggerFactory.setLoggerFactorySPI() later.");

			if (DEBUG_LOG_CONFIG) {
				lastCallerException = createException();
				getSelfLogger().info("Here is the code that triggered the auto-config",
						lastCallerException);
			}
		}
	}

	private static Exception createException() {
		try {
			throw new RuntimeException("+++ This is not an error. +++");
		} catch (Exception e) {
			return e;
		}
	}

	/**
	 * @param clazz
	 *            used as a name for the logger
	 * @return a logger, using the sub-system configured by
	 */
	public static synchronized Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Note {@link #getLogger(Class)} is preferred. It survives refactorings
	 * better.
	 * 
	 * @param name
	 *            for the logger
	 * @return a logger, using the sub-system configured by
	 */
	public static synchronized Logger getLogger(String name) {
		ensureLoggerFactoryDefined();
		return loggerFactorySPI.getLogger(name, LoggerFactory.logListeners);
	}

	/**
	 * Helps debugging.
	 * 
	 * @return @CanBeNull
	 */
	public static ILoggerFactorySPI getLoggerFactorySPI() {
		return loggerFactorySPI;
	}

	/**
	 * @return a {@link Logger} representing the log system itself. Should only
	 *         be used in {@link ILoggerFactorySPI} implementations. Has a
	 *         default level of INFO.
	 */
	public static synchronized Logger getSelfLogger() {
		if (loggerFactorySPI == null)
			throw new IllegalStateException("Cannot use self-logger before SPI is set");
		assert loggerFactorySPI != null;
		if (SELF_LOGGER == null) {
			SELF_LOGGER = loggerFactorySPI.getThreadSafeLogger(LOGGER_NAME_SELF_LOGGING, null);
			SELF_LOGGER.setLevel(Level.Info);
		}
		return SELF_LOGGER;
	}

	public static synchronized Logger getThreadSafeLogger(Class<?> clazz) {
		return getThreadSafeLogger(clazz.getName());
	}

	public static synchronized Logger getThreadSafeLogger(String name) {
		ensureLoggerFactoryDefined();
		return loggerFactorySPI.getThreadSafeLogger(name, LoggerFactory.logListeners);
	}

	// private static synchronized void setFallbackLoggerFactory() {
	//
	// // try to use GWT logger
	// if(gwtEnabled() && gwtLogEnabled()) {
	// loggerFactorySPI = new GwtLoggerFactorySPI();
	// getSelfLogger().info("Logging: Found no LoggerFactorySPI, using GWT Log");
	// } else {
	// loggerFactorySPI = new DefaultLoggerFactorySPI();
	// getSelfLogger().error("Logging: Found no LoggerFactorySPI, using default to std.out");
	// try {
	// throw new RuntimeException(
	// "Logging: FYI (Not an error) This was the first logging call which triggered the log init(). ");
	// } catch(RuntimeException e) {
	// getSelfLogger().info("Logging: Printing caller to System.out", e);
	// e.fillInStackTrace();
	// e.printStackTrace();
	// }
	// }
	// }

	// /**
	// * @return true if this code is running as JavaScript (or in JUnit tests)
	// */
	// public static boolean gwtEnabled() {
	// try {
	// if(GWT.isClient()) {
	// return true;
	// }
	// } catch(Exception e) {
	// } catch(Error e) {
	// }
	// return false;
	// }
	//
	// /**
	// * @return true if the GWT Log system is working
	// */
	// public static boolean gwtLogEnabled() {
	// try {
	// // any class access would do
	// if(com.google.gwt.logging.client.NullLogHandler.class != null) {
	// return true;
	// }
	// } catch(Exception e) {
	// } catch(Error e) {
	// }
	// return false;
	// }

	public static synchronized boolean hasLoggerFactorySPI() {
		return loggerFactorySPI != null;
	}

	/**
	 * Stop adding the given {@link ILogListener} to newly created loggers.
	 * Rarely used in practice.
	 * 
	 * @param logListener
	 *            the listener to be removed from the set of listeners.
	 */
	public static synchronized void removeLogListener(ILogListener logListener) {
		ensureLoggerFactoryDefined();
		LoggerFactory.logListeners.remove(logListener);
		getSelfLogger().info("Logging: Removed log listener " + logListener.getClass().getName());
	}

	/**
	 * @param spi
	 *            an {@link ILoggerFactorySPI} instance
	 * @param configSource
	 *            @CanBeNull an optional String indicating where this can be
	 *            configured
	 */
	public static synchronized void setLoggerFactorySPI(ILoggerFactorySPI spi, String configSource) {
		ILoggerFactorySPI lastLoggerFactorySPI = loggerFactorySPI;
		loggerFactorySPI = spi;
		Exception currentCallerException = createException();

		// avoid redundant ops
		if (lastLoggerFactorySPI != null
				&& !lastLoggerFactorySPI.getClass().equals(DefaultLoggerFactorySPI.class)) {
			assert lastCallerException != null : "lastLogger="
					+ lastLoggerFactorySPI.getClass().getName();

			getSelfLogger().warn(
					"Setting new loggerFactorySpi: " + spi.getClass().getName()
							+ " had until now: " + lastLoggerFactorySPI.getClass().getName());
			getSelfLogger().info("Last loggerFactorySpi was set here", lastCallerException);
			getSelfLogger().info("Current loggerFactorySpi was set hre", currentCallerException);
		}

		// force creating a new SELF-LOGGER
		SELF_LOGGER = null;
		getSelfLogger().info(
				"---- Logging: Configured XydraLog with " + spi.getClass().getName() + ". "
						+ (configSource == null ? "" : "Config source: '" + configSource + "'")
						+ " See " + LoggerFactory.class.getName()
						+ " documentation to solve config problems.");
		lastCallerException = currentCallerException;
	}

}

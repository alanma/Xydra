package org.xydra.log.impl.universal;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.conf.IConfig;
import org.xydra.conf.annotations.RequireConf;
import org.xydra.env.Env;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.jul.JulLoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.spi.ILoggerFactorySPI;

/**
 * In development mode log4j is used. In production, j.u.l. is used.
 * 
 * This class plays a double role. Calling {@link #activate()} uses the static
 * setup.
 * 
 * The zero-args constructor can be used to use {@link UniversalLogger} as a
 * delegating proxy.
 * 
 * @author xamde
 */
@ThreadSafe
public class UniversalLogger implements ILoggerFactorySPI {

	private static boolean activated = false;

	/** delegatee */
	private ILoggerFactorySPI innerFactory = null;

	/**
	 * Create and register appropriate factory.
	 * 
	 * Don't set this manually to LoggerFactory.
	 * 
	 * @param givenFactory
	 * @CanBeNull
	 * @param configSource
	 */
	@RequireConf(value = { ConfParamsXydrdaUniversalLog.GAE_IN_PRODUCTION,
			ConfParamsXydrdaUniversalLog.GWT_IN_PRODUCTION })
	public UniversalLogger(ILoggerFactorySPI givenFactory, String configSource) {
		this.innerFactory = configure(givenFactory, configSource);
	}

	private static ILoggerFactorySPI configure(ILoggerFactorySPI givenFactory, String configSource) {
		ILoggerFactorySPI innerFactory = null;
		IConfig conf = Env.get().conf();
		if (givenFactory == null) {
			// reasonable auto-conf
			boolean gwtInProduction = conf
					.getBoolean(ConfParamsXydrdaUniversalLog.GWT_IN_PRODUCTION);
			boolean gaeInProduction = conf
					.getBoolean(ConfParamsXydrdaUniversalLog.GAE_IN_PRODUCTION);

			if (gwtInProduction || gaeInProduction) {
				innerFactory = new JulLoggerFactory();
			} else {
				innerFactory = new Log4jLoggerFactory();
			}
		} else {
			innerFactory = givenFactory;
		}
		// don't log anything before this is done :-)
		LoggerFactory.setLoggerFactorySPI(innerFactory, configSource);

		conf.setInstance(org.xydra.log.spi.ILoggerFactorySPI.class, innerFactory);
		assert conf.resolve(ILoggerFactorySPI.class) != null;

		return innerFactory;
	}

	@Override
	public Logger getLogger(String name, Collection<ILogListener> logListener) {
		return this.innerFactory.getLogger(name, logListener);
	}

	@Override
	public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
		return this.innerFactory.getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
	}

	@Override
	public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
		return this.innerFactory.getThreadSafeLogger(name, logListeners);
	}

	@Override
	public Logger getThreadSafeWrappedLogger(String name,
			String fullyQualifiedNameOfDelegatingLoggerClass) {
		return this.innerFactory.getThreadSafeWrappedLogger(name,
				fullyQualifiedNameOfDelegatingLoggerClass);
	}

	/**
	 * Activate NOT in GWT and NOT on AppEngine
	 */
	public static void activate() {
		activate(false, false);
	}

	/**
	 * Configures default values of {@link ConfParamsXydrdaUniversalLog} and
	 * sets logger factory.
	 * 
	 * Sets config values into Env.{@link IConfig}.
	 * 
	 * @param inGWT
	 *            if running as compiled JavaScript
	 * @param onGAE
	 *            if running on App Engine
	 */
	public static synchronized void activate(boolean inGWT, boolean onGAE) {
		if (activated)
			return;
		IConfig conf = Env.get().conf();

		// set reasonable defaults
		new ConfParamsXydrdaUniversalLog().configureDefaults(conf);
		// set values given by user
		conf.setBoolean(ConfParamsXydrdaUniversalLog.GWT_IN_PRODUCTION, inGWT);
		conf.setBoolean(ConfParamsXydrdaUniversalLog.GAE_IN_PRODUCTION, onGAE);

		// check for explicit configuration of factory
		String source;

		ILoggerFactorySPI spi = conf.tryToResolve(ConfParamsXydrdaUniversalLog.LOGGER_FACTORY_SPI);
		if (spi != null) {
			source = "conf.getInstance";
		}
		if (spi == null) {
			// maybe we find a resolver
			spi = conf.tryToResolve(ILoggerFactorySPI.class);
		}
		if (spi != null) {
			source = "conf.getResolver";
		} else {
			source = "automatic";
		}

		// init
		configure(spi, "UniversalLoggerFactorySPI.activate");
		LoggerFactory.getSelfLogger().info("LoggerFactorySPI configured from " + source);
		activated = true;
	}
}

package org.xydra.env;

import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.conf.impl.MemoryConfig;

/**
 * JVM-wide singleton to manage creation of {@link IEnvironment} instances.
 * 
 * @author xamde
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
public class Env {

	public static final String DEFAULT_NAME = "default";

	/** Singleton - no instances please */
	protected Env() {
	}

	protected static final Map<String, IEnvironment> map = new HashMap<String, IEnvironment>();

	/**
	 * Use this method in tests in order to start multiple, different
	 * environments to test interactions between them
	 * 
	 * @param name
	 * @return a named environment
	 */
	public static synchronized IEnvironment getNamedEnvironment(String name) {
		IEnvironment env = map.get(name);
		if (env == null) {
			env = createNamedEnvironment(name);
			map.put(name, env);
		}
		return env;
	}

	/**
	 * Use this method in production code
	 * 
	 * @return the default environment
	 */
	public static synchronized IEnvironment get() {
		return getNamedEnvironment(DEFAULT_NAME);
	}

	protected static IEnvironment createNamedEnvironment(String name) {
		return new SimpleEnvironment(new MemoryConfig());
	}

	/**
	 * Loose all state information. Makes only sense in test environment that
	 * run multiple tests without restarting the JVM.
	 */
	public static void reset() {
		map.clear();
	}

}

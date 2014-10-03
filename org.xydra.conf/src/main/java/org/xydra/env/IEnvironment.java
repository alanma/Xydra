package org.xydra.env;

import org.xydra.conf.IConfig;

/**
 * An environment acts like a sub-runtime within a JVM. In one JVM multiple such
 * IEnvs can be started, so that interactions between them can be tested.
 * 
 * @author xamde
 */
public interface IEnvironment {

	/**
	 * @return the active config of this environment
	 */
	IConfig conf();

}

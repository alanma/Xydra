package org.xydra.conf;

/**
 * Allows to find {@link IConfigProvider} implementations via reflection as
 * well.
 *
 * Implementations should be named by convention "ConfParams....".
 *
 * The class body of ConfParams... usually looks like this:
 *
 * <pre>
 * &#064;ConfType(boolean.class)
 * &#064;ConfDoc(&quot;If true, loads P13n resources only from classpath (not file system)&quot;)
 * public static final String RUN_LOCAL = &quot;resource-runLocal&quot;;
 * </pre>
 *
 * with ALL-CAPS final String variables that contain a lowercase-camelCase name.
 * We like names with a descriptive prefix like 'resource-'. Config files are
 * written with keys sorted alphabetically.
 *
 * @author xamde
 */
public interface IConfigProvider {

	/**
	 * Set a number of configuration settings in the given conf
	 *
	 * @param conf
	 */
	void configureDefaults(IConfig conf);

}

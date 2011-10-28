package org.xydra.server.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A simple restless app that allows sharing of XydraPersistence instances in
 * the servlet context (= the Restless context).
 * 
 * Simply load this app in your Restless as well and add an init param in
 * web.xml with 'org.xydra.persistence' which is used as a class name to
 * instantiate an {@link XydraPersistence}. The constructor ClassName(XID
 * repositoryId) is called.
 * 
 * @author xamde
 * @deprecated Use XydraRuntime.getPersistence instead od the servlet context
 *             from XydraRestServer
 */
@Deprecated
public class ShareXydraPersistenceApp {
	
	private static final Logger log = LoggerFactory.getLogger(ShareXydraPersistenceApp.class);
	
	/**
	 * Name of init parameter in web.xml which is used as a class name to
	 * instantiate an {@link XydraPersistence}. The constructor ClassName(XID
	 * repositoryId) is called.
	 */
	public static final String INIT_PARAM_XYDRA_PERSISTENCE = "org.xydra.persistence";
	
	/**
	 * Name of servlet context parameter where the {@link XydraPersistence}
	 * instance is stored. One instance is stored per repository id under the
	 * key key.repoid
	 */
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE = "org.xydra.persistence";
	
	/**
	 * @param restless The current Restless instance.
	 * @return an instance of {@link XydraPersistence} from the current servlet
	 *         context that has hopefully been put there by a previous call of
	 *         restless().
	 */
	public static XydraPersistence getXydraPersistence(Restless restless, XID repositoryId) {
		String key = SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE + "." + repositoryId;
		XydraPersistence xydraPersistence = (XydraPersistence)restless.getServletContext()
		        .getAttribute(key);
		if(xydraPersistence == null) {
			xydraPersistence = createInternal(restless, repositoryId);
			// store in context
			restless.getServletContext().setAttribute(key, xydraPersistence);
			log.info("xydraPersistence instance stored in servletContext at key '" + key + "'");
		}
		return xydraPersistence;
	}
	
	private static XydraPersistence createInternal(Restless restless, XID repositoryId) {
		// create via reflection
		String persistenceClassName = restless.getInitParameter(INIT_PARAM_XYDRA_PERSISTENCE);
		if(persistenceClassName != null) {
			Class<?> persistenceClass;
			try {
				persistenceClass = Class.forName(persistenceClassName);
				Constructor<?> cons = persistenceClass.getConstructor(XID.class);
				assert XydraPersistence.class.isAssignableFrom(persistenceClass) : persistenceClass
				        .getClass() + " is not a XydraPersistence";
				XydraPersistence xydraPersistence = (XydraPersistence)cons
				        .newInstance(repositoryId);
				return xydraPersistence;
			} catch(ClassNotFoundException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(SecurityException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(IllegalArgumentException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(InstantiationException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(IllegalAccessException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			} catch(InvocationTargetException e) {
				throw new RuntimeException("Error configuring XydraPersistence from class '"
				        + persistenceClassName + "'", e);
			}
		} else {
			throw new RuntimeException("no XydraPersistence backend configured in web.xml");
		}
	}
	
	/**
	 * Setup
	 */
	public void restless(Restless restless, String prefix) {
		// nothing to do at init time
	}
	
}

package org.xydra.conf;

/**
 * Delivers runtime instances that are behind the scenes created via Java's new
 * keyword, via GWT.create(), via java.util.ServiceLoader, via Google Guava or
 * GIN, or maybe even via OSGi or what not.
 * 
 * @author xamde
 * 
 * @param <T>
 */
public interface IResolver<T> {

	/**
	 * @return the desired singleton instance or @CanBeNull
	 */
	T resolve();

	/**
	 * @return false iff {@link #resolve()} returns null
	 */
	boolean canResolve();

}

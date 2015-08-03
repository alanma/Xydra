package org.xydra.oo.generator.codespec;

import java.util.Set;

/**
 * A class member, either a field or a method.
 *
 * Implementations must have working {@link #equals(Object)} and
 * {@link #hashCode()}
 *
 * @author xamde
 */
public interface IMember extends Comparable<IMember> {

	/**
	 * @return name of member
	 */
	String getName();

	/**
	 * @return JavaDoc comment
	 */
	String getComment();

	/**
	 * For debugging
	 */
	void dump();

	/**
	 * @return fully qualified class names
	 */
	Set<String> getRequiredImports();

	/**
	 * @return debug info
	 */
	String getGeneratedFrom();

}

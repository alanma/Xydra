package org.xydra.oo.runtime.shared;

import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;

public interface IType {

	/**
	 * @return a valid type expression in java
	 */
	String getTypeString();

	Set<String> getRequiredImports();

	/**
	 * @return a unique id for debugging, hashcode, equals, ...
	 */
	String id();

	@NeverNull
	IBaseType getBaseType();

	boolean isArray();

	String getComment();

	void setComment(String string);

	@CanBeNull
	IBaseType getComponentType();

	String getGeneratedFrom();

}

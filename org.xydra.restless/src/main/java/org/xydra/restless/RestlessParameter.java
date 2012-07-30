package org.xydra.restless;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;


/**
 * A parameter that can be used in a method exposed via Restless to the web.
 * 
 * @author voelkel
 */

@ThreadSafe
public class RestlessParameter {
	
	/*
	 * currently, all variables (and its contents) are never written after
	 * creation and all methods only read these variable -> no synchronization
	 * necessary at the moment
	 */
	private String name;
	private Object defaultValue;
	private boolean isArray;
	
	public static final String DEFAULT_VALUE_NONE_BUT_REQUIRED = "missing_required_parameter";
	
	private static final String[] defaultArray = new String[0];
	
	/**
	 * 
	 * @param name for binding it to variable names @NeverNull TODO is this
	 *            correct?
	 * @param defaultValue the default value if the parameter could not be set
	 *            from HTTP request content. Use null for none. @CanBeNull
	 */
	public RestlessParameter(@NeverNull String name, @CanBeNull String defaultValue) {
		super();
		/*
		 * TODO what's the reason for calling super() here? This class has no
		 * super class other than Object?
		 */
		
		this.name = name;
		this.defaultValue = defaultValue;
		this.isArray = false;
	}
	
	/**
	 * @param name for binding it to variable names @NeverNull TODO is this
	 *            correct?
	 * @param isArray true if there can be multiple values @NeverNull
	 */
	public RestlessParameter(@NeverNull String name, @NeverNull boolean isArray) {
		super();
		this.name = name;
		this.defaultValue = isArray ? defaultArray : null;
		this.isArray = true;
	}
	
	/**
	 * Create a parameter without a default value. Method fails if parameter not
	 * set.
	 * 
	 * @param name for binding it to variable names @NeverNull TODO is this
	 *            correct?
	 */
	public RestlessParameter(@NeverNull String name) {
		this(name, DEFAULT_VALUE_NONE_BUT_REQUIRED);
	}
	
	public boolean mustBeDefinedExplicitly() {
		return this.defaultValue == DEFAULT_VALUE_NONE_BUT_REQUIRED;
	}
	
	/**
	 * 
	 * @return true, if there can be multiple values
	 */
	public boolean isArray() {
		return this.isArray;
	}
	
	/**
	 * 
	 * @return a String for binding it to variable names
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * 
	 * @return the default value if the parameter could not be set from HTTP
	 *         request content. May be null (for none).
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}
	
}

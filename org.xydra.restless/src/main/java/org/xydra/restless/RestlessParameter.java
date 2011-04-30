package org.xydra.restless;

/**
 * A parameter that can be used in a method exposed via Restless to the web.
 * 
 * @author voelkel
 */
public class RestlessParameter {
	
	String name;
	Object defaultValue;
	boolean isArray;
	
	private static final String[] defaultArray = new String[0];
	
	/**
	 * 
	 * @param name for binding it to variable names
	 * @param defaultValue the default value if the parameter could not be set
	 *            from HTTP request content. Use null for none.
	 */
	public RestlessParameter(String name, String defaultValue) {
		super();
		this.name = name;
		this.defaultValue = defaultValue;
		this.isArray = false;
	}
	
	/**
	 * @param name for binding it to variable names
	 * @param isArray true if there can be multiple values
	 */
	public RestlessParameter(String name, boolean isArray) {
		super();
		this.name = name;
		this.defaultValue = isArray ? defaultArray : null;
		this.isArray = true;
	}
	
	/**
	 * Create a parameter without a default value. Method fails if parameter not
	 * set.
	 * 
	 * @param name for binding it to variable names
	 */
	public RestlessParameter(String name) {
		this(name, null);
	}
	
}

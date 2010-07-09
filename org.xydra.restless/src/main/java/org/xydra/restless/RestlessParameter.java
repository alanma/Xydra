package org.xydra.restless;

/**
 * A parameter that can be used in a method exposed via Restless to the web.
 * 
 * @author voelkel
 */
public class RestlessParameter {
	
	String name;
	String defaultValue;
	
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

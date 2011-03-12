package org.xydra.googleanalytics;

/**
 * An event that can be tracked in GA
 * 
 * @author voelkel
 * 
 */
public class GaEvent {
	
	String category, action, optionalLabel = null;
	int optionalValue = -1;
	
	/**
	 * Parameter need not to be URL-encoded
	 * 
	 * @param category to be tracked
	 * @param action to be tracked
	 * @param optionalLabel to be tracked
	 * @param optionalValue to be tracked
	 */
	public GaEvent(String category, String action, String optionalLabel, int optionalValue) {
		super();
		this.category = category;
		this.action = action;
		this.optionalLabel = optionalLabel;
		this.optionalValue = optionalValue;
	}
	
	/**
	 * Parameter need not to be URL-encoded
	 * 
	 * @param category to be tracked
	 * @param action to be tracked
	 * @param optionalLabel to be tracked
	 */
	public GaEvent(String category, String action, String optionalLabel) {
		super();
		this.category = category;
		this.action = action;
		this.optionalLabel = optionalLabel;
	}
	
	/**
	 * Parameter need not to be URL-encoded
	 * 
	 * @param category to be tracked
	 * @param action to be tracked
	 * @param optionalValue to be tracked
	 */
	public GaEvent(String category, String action, int optionalValue) {
		super();
		this.category = category;
		this.action = action;
		this.optionalValue = optionalValue;
	}
	
	/**
	 * Parameter need not to be URL-encoded
	 * 
	 * @param category to be tracked
	 * @param action to be tracked
	 */
	public GaEvent(String category, String action) {
		super();
		this.category = category;
		this.action = action;
	}
	
}

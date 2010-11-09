/** This code is originally based on code from http://code.google.com/p/jgoogleanalytics/  under Apache License 2.0 */
package org.xydra.googleanalytics;


/**
 * Focus point of the application. It can represent data points like application
 * load, application module load, user actions, error events etc.
 * 
 * @author Siddique Hameed (initial code)
 * @author Max Völkel
 */
public class FocusPoint {

	private static final String TITLE_SEPARATOR = "-";
	private static final String URI_SEPARATOR = "/";
	private String name;
	private FocusPoint parent;

	public FocusPoint(String name) {
		this.name = name;
	}

	public FocusPoint(String name, FocusPoint parent) {
		this(name);
		this.parent = parent;
	}

	public String getContentTitle() {
		return (this.parent != null ? this.parent.getContentTitle()
				+ TITLE_SEPARATOR : "")
				+ Utils.urlencode(this.getName());
	}

	public String getContentURI() {
		return (this.parent != null ? this.parent.getContentURI() : "")
				+ URI_SEPARATOR + Utils.urlencode(this.getName());
	}

	public String getName() {
		return this.name;
	}

	public FocusPoint getParent() {
		return this.parent;
	}

	public void setParent(FocusPoint parentFocusPoint) {
		this.parent = parentFocusPoint;
	}
}

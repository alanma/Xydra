package org.xydra.core.serialize;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class ParsingError extends IllegalArgumentException {
	
	private static final long serialVersionUID = -3598672499003466804L;
	
	public ParsingError(XydraElement element, String message, Throwable cause) {
		super("@<" + (element.getType() == null ? "xnull" : element.getType()) + ">: " + message,
		        cause);
	}
	
	/**
	 * @param element
	 * @param message
	 */
	public ParsingError(@NeverNull XydraElement element, String message) {
		this(element, message, null);
	}
	
}

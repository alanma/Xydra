package org.xydra.core.serialize;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class ParsingException extends IllegalArgumentException {

	private static final long serialVersionUID = -3598672499003466804L;

	public ParsingException(final XydraElement element, final String message, final Throwable cause) {
		super("@<" + (element.getType() == null ? "xnull" : element.getType()) + ">: " + message,
		        cause);
	}

	/**
	 * @param element
	 * @param message
	 */
	public ParsingException(@NeverNull final XydraElement element, final String message) {
		this(element, message, null);
	}

}

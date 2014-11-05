package org.xydra.core.model;

import org.xydra.core.XX;


/**
 * Thrown by safe... methods of {@link XX} if the expected models, objects,
 * fields or values are not present.
 * 
 * @author xamde
 * 
 */
public class MissingPieceException extends RuntimeException {
	
	private static final long serialVersionUID = 6517815296601829981L;
	
	public MissingPieceException(String message) {
		super(message);
	}
	
}

package com.sonicmetrics.core.shared;

/**
 * If a query returns as many events as the maximal event limit
 * 
 * @author Andi
 * 
 */
public class LimitReachedException extends RuntimeException {
	
	private static final long serialVersionUID = -4415939536632916914L;
	
	public LimitReachedException(long limit) {
		super("limit of " + limit + " was reached");
	}
	
}

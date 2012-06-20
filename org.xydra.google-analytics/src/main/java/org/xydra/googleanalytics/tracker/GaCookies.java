package org.xydra.googleanalytics.tracker;

/**
 * Cookie values
 * 
 * '__utma=230887938.1463229748.1317737798.1317737798.1317737798.1;
 * +__utmz=230887938.1317737798
 * .1.1.utmcsr=google|utmccn=(organic)|utmcmd=organic
 * |utmctr=tracking%20qr%20codes;'
 * 
 * @author xamde
 */
public class GaCookies {
	
	String utma;
	
	String utmz;
	
	String utmcsr;
	
	String utmccn;
	
	String utmcmd;
	
	String utmctr;
	
	public String toUrlParamValueString() {
		// TODO
		return null;
	}
	
}

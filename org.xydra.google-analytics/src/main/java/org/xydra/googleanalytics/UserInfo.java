package org.xydra.googleanalytics;

/**
 * Data needed to simulate the Urchin cookie.
 * 
 * @author voelkel
 *
 */
/**
 * @author voelkel
 * 
 */
public interface UserInfo {

	/**
	 * @return UTC time-stamp of first visitor session <em>in seconds</em>
	 */
	public long getFirstVisitStartTime();

	/**
	 * @return domain name, without leading "www.". It's ok to keep other third-level domain names.
	 */
	public String getDomainName();

	/**
	 * @return UTC timestamp of last visitor session <em>in seconds</em>
	 */
	public long getLastVisitStartTime();

	/**
	 * @return UTC timestamp of current visitor session <em>in seconds</em>
	 */
	public long getCurrentSessionStartTime();

	/**
	 * @return number of sessions; always incremented for each new session <em>in seconds</em>
	 */
	public long getSessionCount();

	/**
	 * Optional value.
	 * 
	 * @return null if not defined. The value of setVar() otherwise.
	 */
	public String getVar();

	public String getHostName();

	public long get31BitId();

}

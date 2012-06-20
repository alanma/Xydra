package org.xydra.googleanalytics.tracker;

import org.xydra.googleanalytics.Utils;


/**
 * Expires never.
 * 
 * <code>__utma='domainhash.randomValue.ftime.ltime.stime.2</code>
 * 
 * Format:
 * 
 * 'domainhash.unique.ftime.ltime.stime.sessioncount' where:
 * 
 * <pre>
 * domainhash = hash of the domain name of the website
 * unique = a randomly generated 31 bit integer
 * ftime = UTC timestamp of first visitor session
 * ltime = UTC timestamp of last visitor session
 * stime = UTC timestamp of current visitor session
 * sessioncount = number of sessions; always incremented for each new session
 * </pre>
 * 
 * @author xamde
 */
public class GaUtmaCookie extends GaUtmbCookie {
	
	/**
	 * State must be set via {@link #setFromCookieString(String)}
	 * 
	 * @param domainName
	 * @param domainHash
	 */
	public GaUtmaCookie(String domainName, String domainHash) {
		super(domainName, domainHash);
	}
	
	public GaUtmaCookie(String cookieString) {
		setFromCookieString(cookieString);
	}
	
	public GaUtmaCookie(String domainName, long the31BitId, long firstVisitStartTime,
	        long lastVisitStartTime, long currentSessionStartTime, long sessionCount) {
		super(domainName, null, currentSessionStartTime, sessionCount);
		this.the31BitId = the31BitId;
		this.firstVisitStartTime = firstVisitStartTime;
		this.lastVisitStartTime = lastVisitStartTime;
	}
	
	@Override
	public String toCookieString() {
		return getDomainHash() + "." + this.the31BitId + "." + this.firstVisitStartTime + "."
		        + this.lastVisitStartTime + "." + this.currentSessionStartTime + "."
		        + this.sessionCount;
	}
	
	@Override
	public void setFromCookieString(String cookieString) throws IllegalArgumentException {
		// try to parse
		String[] dotParts = cookieString.split("\\.");
		if(dotParts.length == 6) {
			this.domainHash = dotParts[0];
			this.the31BitId = Utils.parseAsLong(dotParts[1]);
			this.firstVisitStartTime = Utils.parseAsLong(dotParts[2]);
			this.lastVisitStartTime = Utils.parseAsLong(dotParts[3]);
			this.currentSessionStartTime = Utils.parseAsLong(dotParts[4]);
			this.sessionCount = Utils.parseAsLong(dotParts[5]);
		} else {
			throw new IllegalArgumentException("Could not parse '" + cookieString
			        + "' into six dot-separated parts");
		}
	}
	
	public long getUniqueId() {
		return this.the31BitId;
	}
	
	public long getFirstVisit() {
		return this.firstVisitStartTime;
	}
	
	/**
	 * @return uniqueId number + '.' + first visit start time = a very good
	 *         unique ID
	 */
	public String getCombinedIdString() {
		return getUniqueId() + "." + getFirstVisit();
	}
	
	/**
	 * @param combinedId must be in format long+'.'+long NEVERNULL
	 * @return a {@link GaUtmaCookie} with only {@link #getUniqueId()} and
	 *         {@link #getFirstVisit()} set.
	 */
	public static GaUtmaCookie fromCombinedIdString(String combinedId) {
		String[] parts = combinedId.split("[.]");
		assert parts.length == 2;
		long id = Utils.parseAsLong(parts[0]);
		long firstVisist = Utils.parseAsLong(parts[1]);
		GaUtmaCookie utma = new GaUtmaCookie(null, id, firstVisist, 0, 0, 0);
		return utma;
	}
	
	/**
	 * UTC time-stamp of first visitor session <em>in seconds</em>
	 */
	public long firstVisitStartTime;
	
	/**
	 * UTC timestamp of last visitor session <em>in seconds</em>
	 */
	public long lastVisitStartTime;
	
	public long the31BitId;
	
}

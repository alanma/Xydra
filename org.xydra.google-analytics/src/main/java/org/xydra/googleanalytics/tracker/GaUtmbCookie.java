package org.xydra.googleanalytics.tracker;

import org.xydra.googleanalytics.Utils;


/**
 * 30-minute expiry.
 * 
 * <code>__utmb=aaa</code>
 * 
 * Begin of session.
 * 
 * This cookie is used to establish and continue a user session with your site.
 * When a user views a page on your site, the Google Analytics code attempts to
 * update this cookie. If it does not find the cookie, a new one is written and
 * a new session is established. Each time a user visits a different page on
 * your site, this cookie is updated to expire in 30 minutes, thus continuing a
 * single session for as long as user activity continues within 30-minute
 * intervals. This cookie expires when a user pauses on a page on your site for
 * longer than 30 minutes. You can modify the default length of a user session
 * with the _setSessionTimeout() method.
 * 
 * Hashcode. Changes to identify each unique session. Non-persistent cookie.
 * Works with __utmc to determine when a session ends. Dies when a browser is
 * closed. If it disappears a new visitor session is started.
 * 
 * @author xamde
 */
class GaUtmbCookie extends DomainHashCookie {
	
	public GaUtmbCookie(String domainName, String domainHash, long currentSessionStartTime,
	        long sessionCount) {
		super(domainName, domainHash);
		this.currentSessionStartTime = currentSessionStartTime;
		this.sessionCount = sessionCount;
	}
	
	/**
	 * Need to set state via {@link #setFromCookieString(String)}
	 */
	protected GaUtmbCookie() {
	}
	
	public GaUtmbCookie(String domainName, String domainHash) {
		super(domainName, domainHash);
	}
	
	/**
	 * UTC timestamp of current visitor session <em>in seconds</em>
	 */
	public long currentSessionStartTime;
	
	/**
	 * number of sessions; always incremented for each new session
	 * <em>in seconds</em>
	 */
	public long sessionCount;
	
	public String toCookieString() {
		// utmb = {domain hash}.{session count + 1}.10.{now in seconds}
		return getDomainHash() + "." + // .
		        this.sessionCount + "." + // .
		        "10." + this.currentSessionStartTime;
	}
	
	public void setFromCookieString(String cookieString) throws IllegalArgumentException {
		// try to parse
		String[] dotParts = cookieString.split("\\.");
		if(dotParts.length == 4) {
			this.domainHash = dotParts[0];
			this.sessionCount = Utils.parseAsLong(dotParts[1]);
			// .10.
			this.currentSessionStartTime = Utils.parseAsLong(dotParts[3]);
		} else {
			throw new IllegalArgumentException("Could not parse '" + cookieString
			        + "' into four dot-separated parts");
		}
	}
}

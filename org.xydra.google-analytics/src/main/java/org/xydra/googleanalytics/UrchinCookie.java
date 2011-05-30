package org.xydra.googleanalytics;

/**
 * This class models a cookie as used by Google Analytics 4.3. As a pure Java
 * application have no cookies, the application must provide useful values.
 * 
 * 
 * A sample string, not URL encoded looks like this: <code>
 * __utma='domainhash.randomValue.ftime.ltime.stime.2;+__utmb=aaa;+__utmc=bbb;+__utmz=ccc.dddtime.2.2.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none);+__utmv=eee
 * </code>
 * 
 * Info based on <a href=
 * "http://www.analyticsexperts.com/google-analytics/information-about-the-utmlinker-and-the-__utma-__utmb-and-__utmc-cookies/"
 * >this blog</a> and <a href=
 * "http://code.google.com/apis/analytics/docs/concepts/gaConceptsCookies.html"
 * >official sources</a>.
 * 
 * @author voelkel
 */
public class UrchinCookie {
	
	private Utma utma;
	private Utmb utmb;
	private Utmc utmc;
	private Utmv utmv;
	private Utmz utmz;
	
	public UrchinCookie(UserInfo userinfo) {
		this.utma = new Utma(userinfo.getDomainName(), userinfo.get31BitId(),
		        userinfo.getFirstVisitStartTime(), userinfo.getLastVisitStartTime(),
		        userinfo.getCurrentSessionStartTime(), userinfo.getSessionCount());
		this.utmb = new Utmb(userinfo.getDomainName(), userinfo.getCurrentSessionStartTime(),
		        userinfo.getSessionCount());
		this.utmc = null;
		if(userinfo.getVar() != null) {
			this.utmv = new Utmv(userinfo.getVar());
		}
		this.utmz = new Utmz();
		this.utmz.domainName = userinfo.getDomainName();
		
	}
	
	public UrchinCookie(String utmaCookie, String utmzCookie) {
		this.utma = new Utma();
		this.utma.setFromCookieString(utmaCookie);
		this.utmb = new Utmb();
		this.utmb.domainHash = this.utma.getDomainHash();
		this.utmb.currentSessionStartTime = Utils.getCurrentTimeInSeconds();
		this.utmb.sessionCount = this.utma.sessionCount;
		this.utmc = null;
		// TODO consider parsing from cookie
		this.utmv = null;
		this.utmz = new Utmz();
		this.utmz.setFromCookieString(utmzCookie);
	}
	
	/**
	 * Minimum: utmcc=__utma%3D<utma cookie>3B%2B__utmz%3D<utmz cookie>%3B
	 * 
	 * @return cookie string. Used as the value of the 'utmcc' URL param.
	 */
	public String getCookieString() {
		
		// ORIG
		// __utma%3D19047217.4457234770434814500.1243945311.1243945311.1243945311.1%3B%2B
		// __utmz%3D19047217.1243945311.1.1.utmcsr%3D(direct)%7Cutmccn%3D(direct)%7Cutmcmd%3D(none)%3B
		
		// FAKE
		// __utma%3D249065557.379525002.1243946955375.1243946955375.1243946955375.1%3B%2B
		// __utmb%3D249065557.1.10.1243946955%3B%2B
		// __utmc%3D249065557%3B%2B
		// __utmz%3D249065557.0.0.0.utmccn%3D%28direct%29%7Cutmcsr%3D%28direct%29%7Cutmcmd%3D%28none%29%7Cutmctr%3Dnone%7Cutmcct%3Dnone&
		
		String result = "__utma=" + this.utma.toCookieString() + ";";
		
		result += "+__utmb=" + this.utmb.toCookieString() + ";";
		
		if(this.utmc != null) {
			result += "+__utmc=" + this.utmc.toCookieString() + ";";
		}
		
		result += "+__utmz=" + this.utmz.toCookieString() + ";";
		
		/** optional parts */
		if(this.utmv != null) {
			String utmv = this.utmv.toCookieString();
			if(utmv != null) {
				result += ";+__utmv=" + utmv;
			}
		}
		
		// String utmx = getUtmx();
		// if (utmx != null) {
		// result += ";+__utmx=" + utmx;
		// }
		
		return result;
	}
	
	/**
	 * This cookie is used by Website Optimizer and only set when the Website
	 * Optimizer tracking code is installed and correctly configured for your
	 * pages. When the optimizer script executes, this cookie stores the
	 * variation this visitor is assigned to for each experiment, so the visitor
	 * has a consistent experience on your site. See the Website Optimizer Help
	 * Center for more information.
	 * http://www.google.com/support/websiteoptimizer/
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getUtmx() {
		return null;
	}
	
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
	static class Utma extends Utmb {
		
		/**
		 * State must be set via {@link #setFromCookieString(String)}
		 */
		public Utma() {
			super();
		}
		
		public Utma(String domainName, long the31BitId, long firstVisitStartTime,
		        long lastVisitStartTime, long currentSessionStartTime, long sessionCount) {
			super(domainName, currentSessionStartTime, sessionCount);
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
				this.the31BitId = parseAsLong(dotParts[1]);
				this.firstVisitStartTime = parseAsLong(dotParts[2]);
				this.lastVisitStartTime = parseAsLong(dotParts[3]);
				this.currentSessionStartTime = parseAsLong(dotParts[4]);
				this.sessionCount = parseAsLong(dotParts[5]);
			} else {
				throw new IllegalArgumentException("Could not parse '" + cookieString
				        + "' into six dot-separated parts");
			}
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
	
	static class DomainHashCookie {
		
		public String getDomainHash() {
			if(this.domainHash != null) {
				return this.domainHash;
			} else {
				assert this.domainName != null;
				return "" + Utils.getDomainhash(this.domainName);
			}
		}
		
		/**
		 * domain name, without leading "www.". It's ok to keep other
		 * third-level domain names.
		 */
		protected String domainName;
		
		/**
		 * Since domain name cannot be reconstructed from hash, we store the
		 * hash when we manipulate cookies.
		 */
		protected String domainHash;
		
	}
	
	/**
	 * 30-minute expiry.
	 * 
	 * <code>__utmb=aaa</code>
	 * 
	 * Begin of session.
	 * 
	 * This cookie is used to establish and continue a user session with your
	 * site. When a user views a page on your site, the Google Analytics code
	 * attempts to update this cookie. If it does not find the cookie, a new one
	 * is written and a new session is established. Each time a user visits a
	 * different page on your site, this cookie is updated to expire in 30
	 * minutes, thus continuing a single session for as long as user activity
	 * continues within 30-minute intervals. This cookie expires when a user
	 * pauses on a page on your site for longer than 30 minutes. You can modify
	 * the default length of a user session with the _setSessionTimeout()
	 * method.
	 * 
	 * Hashcode. Changes to identify each unique session. Non-persistent cookie.
	 * Works with __utmc to determine when a session ends. Dies when a browser
	 * is closed. If it disappears a new visitor session is started.
	 * 
	 * @author xamde
	 */
	static class Utmb extends DomainHashCookie {
		
		public Utmb(String domainName, long currentSessionStartTime, long sessionCount) {
			super();
			this.domainName = domainName;
			this.currentSessionStartTime = currentSessionStartTime;
			this.sessionCount = sessionCount;
		}
		
		/**
		 * Need to set state via {@link #setFromCookieString(String)}
		 */
		public Utmb() {
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
			        "1." + this.currentSessionStartTime;
		}
		
		public void setFromCookieString(String cookieString) throws IllegalArgumentException {
			// try to parse
			String[] dotParts = cookieString.split("\\.");
			if(dotParts.length == 4) {
				this.domainHash = dotParts[0];
				this.sessionCount = parseAsLong(dotParts[1]);
				// .10.
				this.currentSessionStartTime = parseAsLong(dotParts[3]);
			} else {
				throw new IllegalArgumentException("Could not parse '" + cookieString
				        + "' into four dot-separated parts");
			}
		}
	}
	
	/**
	 * Close of session.
	 * 
	 * <code>__utmc=bbb</code>
	 * 
	 * This cookie operates in conjunction with the __utmb cookie to determine
	 * whether or not to establish a new session for the user. In particular,
	 * this cookie is not provided with an expiration date, so it expires when
	 * the user exits the browser. Should a user visit your site, exit the
	 * browser and then return to your website within 30 minutes, the absence of
	 * the __utmc cookie indicates that a new session needs to be established,
	 * despite the fact that the __utmb cookie has not yet expired.
	 * 
	 * Session based cookie. Destroyed after 30 minutes of inactivity. Can be
	 * set higher. Works with __utmb to determine when session ends. If it
	 * disappears, a new visitor session starts. Visitor timeout set in
	 * __utm.js. Default is 1800 seconds. 30 minutes is appropriate. Some
	 * websites and their visitor traffic may require a different timeout value.
	 * 
	 * @author xamde
	 */
	static class Utmc extends DomainHashCookie {
		
		public String toCookieString() {
			return "" + getDomainHash();
		}
	}
	
	/**
	 * <code>__utmv=eee</code>
	 * 
	 * This cookie is not normally present in a default configuration of the
	 * tracking code. The __utmv cookie passes the information provided via the
	 * _setVar() method, which you use to create a custom user segment. This
	 * string is then passed to the Analytics servers in the GIF request URL via
	 * the utmcc parameter. This cookie is only written if you have added the
	 * _setVar() method for the tracking code on your website page.
	 * 
	 * @author xamde
	 * 
	 */
	static class Utmv {
		
		public Utmv(String var) {
			super();
			this.var = var;
		}
		
		private String var;
		
		public String toCookieString() {
			return this.var;
		}
		
	}
	
	/**
	 * <code>__utmz=ccc.dddtime.2.2.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none)</code>
	 * 
	 * This cookie stores the type of referral used by the visitor to reach your
	 * site, whether via a direct method, a referring link, a website search, or
	 * a campaign such as an ad or an email link. It is used to calculate search
	 * engine traffic, ad campaigns and page navigation within your own site.
	 * The cookie is updated with each page view to your site.
	 * 
	 * __utmz=<domain hash>.<timestamp when cookie was set>.<visit
	 * count>.<source count>.utmccn=<campaign>|utmcsr=<source>|utmcmd=<medium>;
	 * 
	 * 19047217.1243945311.1.1.utmcsr=(direct)| utmccn=(direct)|utmcmd=(none);
	 * 
	 * @author xamde
	 */
	static class Utmz extends DomainHashCookie {
		
		public Utmz(String campaignCreationTime, String campaignSessions, String responseCount,
		        String campaignSource, String campaignName, String campaignMedium,
		        String campaignTerms, String campaignContent) {
			super();
			this.campaignCreationTime = campaignCreationTime;
			this.campaignSessions = campaignSessions;
			this.responseCount = responseCount;
			this.campaignSource = campaignSource;
			this.campaignName = campaignName;
			this.campaignMedium = campaignMedium;
			this.campaignTerms = campaignTerms;
			this.campaignContent = campaignContent;
		}
		
		/**
		 * Make sure to set content via {@link #setFromCookieString(String)}
		 */
		public Utmz() {
		}
		
		private String campaignCreationTime = "" + Utils.getCurrentTimeInSeconds();
		private String campaignSessions = "1";
		private String responseCount = "0";
		/** 'utmcsr', e.g. 'Affilliate' */
		private String campaignSource = "(direct)";
		private String campaignName = "(direct)";
		/** 'utmcmd', e.g. 'cpc' or 'thewebsite.com' */
		private String campaignMedium = "(none)";
		private String campaignTerms = "(none)";
		private String campaignContent = "(none)";
		
		public void setFromCookieString(String utmzCookie) throws IllegalArgumentException {
			String[] dotParts = utmzCookie.split("\\.");
			if(dotParts.length != 5) {
				throw new IllegalArgumentException("Could not parse '" + utmzCookie
				        + " to five dot-parts.");
			}
			this.domainHash = dotParts[0];
			this.campaignCreationTime = dotParts[1];
			this.campaignSessions = dotParts[2];
			this.responseCount = dotParts[3];
			// more parsing to 'name=value' pairs
			String[] pipeParts = dotParts[4].split("\\|");
			for(String p : pipeParts) {
				String[] nameValue = p.split("=");
				if(nameValue.length == 2) {
					String name = nameValue[0];
					String value = nameValue[1];
					if(name.equals("utmcsr")) {
						this.campaignSource = value;
					} else if(name.equals("utmccn")) {
						this.campaignName = value;
					} else if(name.equals("utmcmd")) {
						this.campaignMedium = value;
					} else if(name.equals("utmctr")) {
						this.campaignTerms = value;
					} else if(name.equals("utmcct")) {
						this.campaignContent = value;
					}
				}
			}
		}
		
		public String toCookieString() {
			return getDomainHash()

			+ "." + this.campaignCreationTime

			+ "." + this.campaignSessions

			+ "." + this.responseCount

			+ "." + "utmcsr=" + this.campaignSource

			+ "|utmccn=" + this.campaignName

			+ "|utmcmd=" + this.campaignMedium

			+ "|utmctr=" + this.campaignTerms

			+ "|utmcct=" + this.campaignContent

			;
		}
		
	}
	
	static long parseAsLong(String s) throws IllegalArgumentException {
		try {
			return Long.parseLong(s);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
}

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
 * Info based on <a href="http://www.analyticsexperts.com/google-analytics/information-about-the-utmlinker-and-the-__utma-__utmb-and-__utmc-cookies/"
 * >this blog</a> and <a href=
 * "http://code.google.com/apis/analytics/docs/concepts/gaConceptsCookies.html"
 * >official sources</a>.
 * 
 * @author voelkel
 */
public class UrchinCookie {

	private UserInfo userinfo;

	public UrchinCookie(UserInfo userinfo) {
		this.userinfo = userinfo;
	}

	/**
	 * 'Never' expires.
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
	 */
	private String getUtma() {
		return Utils.getDomainhash(this.userinfo.getDomainName()) + "."
				+ this.userinfo.get31BitId() + "."
				+ this.userinfo.getFirstVisitStartTime() + "."
				+ this.userinfo.getLastVisitStartTime() + "."
				+ this.userinfo.getCurrentSessionStartTime() + "."
				+ this.userinfo.getSessionCount();
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

		String result = "__utma=" + getUtma() + ";";

		result += "+__utmb=" + getUtmb() + ";";

		// "__utmc=" + getUtmc() + ";+" +

		result += "+__utmz=" + getUtmz() + ";";

		// /** optional parts */
		// String utmv = getUtmv();
		// if (utmv != null) {
		// result += ";+__utmv=" + utmv;
		// }
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
	 * This cookie is not normally present in a default configuration of the
	 * tracking code. The __utmv cookie passes the information provided via the
	 * _setVar() method, which you use to create a custom user segment. This
	 * string is then passed to the Analytics servers in the GIF request URL via
	 * the utmcc parameter. This cookie is only written if you have added the
	 * _setVar() method for the tracking code on your website page.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getUtmv() {
		return this.userinfo.getVar();
	}

	/**
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
	 * @return
	 */
	private String getUtmz() {
		return Utils.getDomainhash(this.userinfo.getDomainName()) + "."
				+ getCampaignCreationTime() + "." + getCampaignSessions() + "."
				+ getResponseCount() + // .
				".utmcsr=" + getUtmCampaignSource() + // .
				"|utmccn=" + getUtmCampaignName() + // .
				"|utmcmd=" + getUtmCampaignMedium() // .
		// "|utmctr=" + getUtmCampaignTerms() + // .
		// "|utmcct=" + getUtmCampaignContent()
		;
	}

	public int getResponseCount() {
		return 0;
	}

	@SuppressWarnings("unused")
	private String getUtmCampaignContent() {
		return "(none)";
	}

	public long getCampaignCreationTime() {
		return Utils.getCurrentTimeInSeconds();
	}

	public int getCampaignSessions() {
		return 1;
	}

	@SuppressWarnings("unused")
	private String getUtmCampaignTerms() {
		return "(none)";
	}

	/**
	 * @return 'utmcmd', e.g. 'cpc' or 'thewebsite.com'
	 */
	private String getUtmCampaignMedium() {
		return "(none)";
	}

	/**
	 * @return 'utmcsr', e.g. 'Affilliate'
	 */
	private String getUtmCampaignSource() {
		return "(direct)";
	}

	/**
	 * @return 'utmccn'
	 */
	private String getUtmCampaignName() {
		return "(direct)";
	}

	/**
	 * Close of session.
	 * 
	 * This cookie operates in conjunction with the __utmb cookie to determine
	 * whether or not to establish a new session for the user. In particular,
	 * this cookie is not provided with an expiration date, so it expires when
	 * the user exits the browser. Should a user visit your site, exit the
	 * browser and then return to your website within 30 minutes, the absence of
	 * the __utmc cookie indicates that a new session needs to be established,
	 * despite the fact that the __utmb cookie has not yet expired.
	 * 
	 * 
	 * Session based cookie. Destroyed after 30 minutes of inactivity. Can be
	 * set higher. Works with __utmb to determine when session ends. If it
	 * disappears, a new visitor session starts. Visitor timeout set in
	 * __utm.js. Default is 1800 seconds. 30 minutes is appropriate. Some
	 * websites and their visitor traffic may require a different timeout value.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getUtmc() {
		return "" + Utils.getDomainhash(this.userinfo.getDomainName());
	}

	/**
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
	 * Works with __utmc to determine when a session end.s Dies when a browser
	 * is closed. If it disappears a new visitor session is started.
	 * 
	 * @return
	 */
	private String getUtmb() {
		// utmb = {domain hash}.{session count + 1}.10.{now in seconds}
		return Utils.getDomainhash(this.userinfo.getDomainName()) + "." + // .
				this.userinfo.getSessionCount() + "." + // .
				"1." + this.userinfo.getCurrentSessionStartTime();
	}

}

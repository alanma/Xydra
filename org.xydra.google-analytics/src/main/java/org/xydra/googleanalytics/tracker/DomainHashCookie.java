package org.xydra.googleanalytics.tracker;

import org.xydra.googleanalytics.Utils;


class DomainHashCookie {
	
	/**
	 * One of the two may be null.
	 * 
	 * @param domainName may be null if domainHash is set
	 * @param domainHash may be null if domainName is set
	 */
	public DomainHashCookie(String domainName, String domainHash) {
		assert domainName != null || domainHash != null;
		this.domainName = domainName;
		this.domainHash = domainHash;
	}
	
	/**
	 * Need to set state
	 */
	protected DomainHashCookie() {
	}
	
	public String getDomainHash() {
		if(this.domainHash != null) {
			return this.domainHash;
		} else {
			assert this.domainName != null;
			return "" + Utils.getDomainhash(this.domainName);
		}
	}
	
	/**
	 * domain name, without leading "www.". It's ok to keep other third-level
	 * domain names.
	 */
	protected String domainName;
	
	/**
	 * Since domain name cannot be reconstructed from hash, we store the hash
	 * when we manipulate cookies.
	 */
	protected String domainHash;
	
}

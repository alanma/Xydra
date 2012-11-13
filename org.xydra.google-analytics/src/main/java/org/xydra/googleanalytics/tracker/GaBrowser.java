package org.xydra.googleanalytics.tracker;

public class GaBrowser {
	
	/**
	 * @param cs =UTF-8 – Language encoding for the browser. Some browsers don’t
	 *            set this, in which case it is set to “-”
	 * @param sr =1280×1024 – Screen resolution
	 * @param sc =24-bit – Screen color depth
	 * @param ul =en-us – Browser language
	 * @param je =1 – Indicates if browser is Java enabled. 1 is true.
	 * @param fl =10.3 r183 – Flash version
	 * 
	 * @author xamde
	 * 
	 */
	public GaBrowser(String cs, String sr, String sc, String ul, String je, String fl) {
		super();
		this.cs = cs;
		this.sr = sr;
		this.sc = sc;
		this.ul = ul;
		this.je = je;
		this.fl = fl;
	}
	
	String cs, sr, sc, ul, je, fl;
	
}

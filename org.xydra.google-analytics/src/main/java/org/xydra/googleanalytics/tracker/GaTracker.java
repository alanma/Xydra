package org.xydra.googleanalytics.tracker;

import org.xydra.googleanalytics.httpclient.HttpUserAgent;


/**
 * @author xamde TODO should run in GWT as well
 */
public class GaTracker {
	
	/**
	 * Internally, GA sends all parameters in a single URL, each with the prefix
	 * 'umt'.
	 * 
	 * @param httpClient
	 * @param wv =5.1.7 – Tracking code version
	 * @param s =1 – Session number. Number of sessions/visits from this
	 *            particular browser Session requests. Updates every time a
	 *            __.gif request is made. Stops incrementing at 500 (max number
	 *            of GIF requests per session).
	 * @param n =1894752493 – Unique ID generated for each GIF request to
	 *            prevent caching of the GIF image
	 * @param hn =www.lunametrics.com – Host name, which is a URL-encoded string
	 * @param cs =UTF-8 – Language encoding for the browser. Some browsers don’t
	 *            set this, in which case it is set to “-”
	 * @param sr =1280×1024 – Screen resolution
	 * @param sc =24-bit – Screen color depth
	 * @param ul =en-us – Browser language
	 * @param je =1 – Indicates if browser is Java enabled. 1 is true.
	 * @param fl =10.3 r183 – Flash version
	 * @param dt =Tracking QR Codes with Google Analytics – Page title, which is
	 *            a URL-encoded string
	 * @param hid =1681965357 – A random number used to link the GA GIF request
	 *            with AdSense
	 * @param r =http://www.google.com/search?q=tracking+qr+codes&ie=utf-8&oe=
	 *            utf -8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a –
	 *            Referral, complete URL
	 * @param p =/blog/2011/08/18/tracking-qr-codes-google-anaytics/ – Page
	 *            request of the current page
	 * @param ac =UA-296882-1 – Account string, appears on all requests
	 * @param cc 
	 *            =__utma=230887938.1463229748.1317737798.1317737798.1317737798.1;
	 *            +__utmz=230887938.1317737798.1.1.utmcsr=google|utmccn=(organic
	 *            )|utmcmd=organic|utmctr=tracking%20qr%20codes; – Cookie
	 *            values. This request parameter sends all the cookies requested
	 *            from the page.
	 * @param u =DC~ – This is a new parameter that contains some internal state
	 *            that helps improve ga.js.
	 **/
	public static void track(HttpUserAgent httpClient, String wv, String s, String n, String hn,
	        String cs, String sr, String sc, String ul, String je, String fl, String dt,
	        String hid, String r, String p, String ac, String cc, String u) {
	}
	
	/**
	 * Internally, GA sends all parameters in a single URL, each with the prefix
	 * 'umt'.
	 * 
	 * @param httpClient
	 * @param wv =5.1.7 – Tracking code version
	 * @param s =1 – Session number. Number of sessions/visits from this
	 *            particular browser Session requests. Updates every time a
	 *            __.gif request is made. Stops incrementing at 500 (max number
	 *            of GIF requests per session).
	 * @param n =1894752493 – Unique ID generated for each GIF request to
	 *            prevent caching of the GIF image
	 * @param hn =www.lunametrics.com – Host name, which is a URL-encoded string
	 * @param cs =UTF-8 – Language encoding for the browser. Some browsers don’t
	 *            set this, in which case it is set to “-”
	 * @param sr =1280×1024 – Screen resolution
	 * @param sc =24-bit – Screen color depth
	 * @param ul =en-us – Browser language
	 * @param je =1 – Indicates if browser is Java enabled. 1 is true.
	 * @param fl =10.3 r183 – Flash version
	 * @param dt =Tracking QR Codes with Google Analytics – Page title, which is
	 *            a URL-encoded string
	 * @param hid =1681965357 – A random number used to link the GA GIF request
	 *            with AdSense
	 * @param r =http://www.google.com/search?q=tracking+qr+codes&ie=utf-8&oe=
	 *            utf -8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a –
	 *            Referral, complete URL
	 * @param p =/blog/2011/08/18/tracking-qr-codes-google-anaytics/ – Page
	 *            request of the current page
	 * @param ac =UA-296882-1 – Account string, appears on all requests
	 * @param cc This request parameter sends all the cookies requested from the
	 *            page.
	 * @param u =DC~ – This is a new parameter that contains some internal state
	 *            that helps improve ga.js.
	 **/
	public static void track(HttpUserAgent httpClient, String wv, String s, String n, String hn,
	        String cs, String sr, String sc, String ul, String je, String fl, String dt,
	        String hid, String r, String p, String ac, GaCookies cc, String u) {
		track(httpClient, wv, s, n, hn, cs, sr, sc, ul, je, fl, dt, hid, r, p, ac,
		        cc.toUrlParamValueString(), u);
	}
}

package org.xydra.googleanalytics;

import java.nio.charset.Charset;
import java.util.Locale;


public class UrchinUrl {
	
	private static final String TRACKING_URL_Prefix = "http://www.google-analytics.com/__utm.gif";
	
	/**
	 * 
	 * <h3>Minimal request</h3> <code><pre>
	 * http://www.google-analytics.com/__utm.gif? 
	 * utmwv=4.3& 
	 * utmn=<random#>&
	 * utmhn=<hostname>& 
	 * utmhid=<random#>& 
	 * utmr=-& 
	 * utmp=<URL>& 
	 * utmac=UA-XXXX-1&
	 * utmcc=__utma%3D<utma cookie>3B%2B__utmz%3D<utmz cookie>%3B
	 * </pre></code>
	 * 
	 * @param hostname from which this request is sent
	 * @param focusPoint to be tracked
	 * @param refererURL to be tracked
	 * @param cookie to be tracked
	 * @param trackingCode UA-......
	 * @param gaEvent to be tracked
	 * @return a URL as a String
	 */
	public static String toURL(String hostname, FocusPoint focusPoint, String refererURL,
	        UrchinCookie cookie, String trackingCode, GaEvent gaEvent) {
		StringBuffer url = new StringBuffer(TRACKING_URL_Prefix);
		
		// Function:Tracking code version
		// Example:utmwv=1
		url.append("?utmwv=4.3");
		
		// Function:Unique ID generated for each GIF request to prevent caching
		// of the GIF image.
		// Example:utmn=1142651215
		appendIfNotEmpty(url, "utmn", "" + Utils.random31bitInteger(), true);
		
		// Function:Host Name, which is a URL-encoded string.
		// Example:utmhn=x343.gmodules.com
		appendIfNotEmpty(url, "utmhn", hostname, true);
		
		// Function:Language encoding for the browser. Some browsers don’t set
		// this, in which case it is set to “-”
		// Example:utmcs=ISO-8859-1
		String charset = Charset.defaultCharset().name();
		appendIfNotEmpty(url, "utmcs", charset, true);
		
		/** Fake values */
		
		// Function:Screen resolution
		// Example:utmsr=2400×1920&
		appendIfNotEmpty(url, "utmsr", "23x42", true);
		
		// Function:Screen color depth
		// Example:utmsc=24-bit
		appendIfNotEmpty(url, "utmsc", "13-bit", true);
		
		// Function:Browser language.
		// Example:utmul=pt-br
		Locale locale = Locale.getDefault();
		String langcode = locale.getLanguage();
		appendIfNotEmpty(url, "utmul", langcode, true);
		
		/** Always true, as we work on Java */
		// Function:Indicates if browser is Java-enabled. 1 is true.
		// Example:utmje=1
		appendIfNotEmpty(url, "utmje", "1", true);
		
		// Function:Flash Version
		// Example:utmfl=9.0%20r48&
		appendIfNotEmpty(url, "utmfl", "(not set)", true);
		/*
		 * this is a valid value in May 2009
		 */

		// Function:Page title, which is a URL-encoded string.
		// Example:utmdt=analytics%20page%20test
		assert focusPoint.getContentTitle() != null;
		appendIfNotEmpty(url, "utmdt", focusPoint.getContentTitle(), false);
		
		// a visitors AdSense ID, but in most cases is also a random number.
		// utmhid - seems to be a random number - unclear if 31 or 32 bit
		long hid = Math.round(Math.random() * 0x7fffffff);
		appendIfNotEmpty(url, "utmhid", "" + hid, true);
		
		// Function:Referral, complete URL.
		// Example:utmr=http://www.example.com/aboutUs/index.php?var=selected
		String useRefererUrl = refererURL;
		if(useRefererUrl == null) {
			useRefererUrl = "-";
		}
		appendIfNotEmpty(url, "utmr", useRefererUrl, true);
		
		// Function:Page request of the current page.
		// Example:utmp=/testDirectory/myPage.html
		assert focusPoint.getContentURI() != null;
		appendIfNotEmpty(url, "utmp", focusPoint.getContentURI(), false);
		
		// Function:Account String. Appears on all requests.
		// Example:utmac=UA-2202604-2
		appendIfNotEmpty(url, "utmac", trackingCode, true);
		
		// Function:Cookie values. This request parameter sends all the cookies
		// requested from the page.
		// Example:utmcc=__utma%3D117243.1695285.22%3B%2B
		// __utmz%3D117945243.1202416366.21.10. utmcsr%3Db%7C
		// utmccn%3D(referral)%7C utmcmd%3Dreferral%7C utmcct%3D%252Fissue%3B%2B
		appendIfNotEmpty(url, "utmcc", cookie.getCookieString(), true);
		
		/** === Optional parameters === */
		
		/** campaigns */
		
		// utmcn
		// Function:Starts a new campaign session. Either utmcn or utmcr is
		// present on any given request. Changes the campaign tracking data; but
		// does not start a new session
		// Example:utmcn=1
		//
		// utmcr
		// Function:Indicates a repeat campaign visit. This is set when any
		// subsequent clicks occur on the same link. Either utmcn or utmcr is
		// present on any given request.
		// Example:utmcr=1
		//
		//
		//
		// utmipc
		// Function:Product Code. This is the sku code for a given product.
		// Example:utmipc=989898ajssi
		//
		// utmipn
		// Function:Product Name, which is a URL-encoded string.
		// Example:utmipn=tee%20shirt
		//
		// utmipr
		// Function:Unit Price. Set at the item level. Value is set to numbers
		// only in U.S. currency format.
		// Example:utmipr=17100.32
		//
		// utmiqt
		// Function:Quantity.
		// Example:utmiqt=4
		//
		// utmiva
		// Function:Variations on an item. For example: large, medium, small,
		// pink, white, black, green. String is URL-encoded.
		// Example:utmiva=red;
		// utmt
		// Function:A special type variable applied to events, transactions,
		// items and user-defined variables.
		// Example:utmt=Dog%20Owner
		if(gaEvent != null) {
			appendIfNotEmpty(url, "utmt", "event", true);
			// utme
			// Function:X10 Data Parameter
			// Example:Value is encoded.
			// example: 5(category*action*optional_label)(12)
			String utmeValue = "5(" + Utils.urlencode(gaEvent.category) + "*"
			        + Utils.urlencode(gaEvent.action);
			if(gaEvent.optionalLabel != null) {
				utmeValue += "*" + Utils.urlencode(gaEvent.optionalLabel);
			}
			utmeValue += ")";
			if(gaEvent.optionalValue != -1) {
				utmeValue += "(" + gaEvent.optionalValue + ")";
			}
			appendIfNotEmpty(url, "utme", utmeValue, false);
		}
		
		//
		// utmtci
		// Function:Billing City
		// Example:utmtci=San%20Diego
		//
		// utmtco
		// Function:Billing Country
		// Example:utmtco=United%20Kingdom
		//
		// utmtid
		// Function:Order ID, URL-encoded string.
		// Example:utmtid=a2343898
		//
		// utmtrg
		// Function:Billing region, URL-encoded string.
		// Example:utmtrg=New%20Brunswick
		//
		// utmtsp
		// Function:Shipping cost. Values as for unit and price
		// Example:utmtsp=23.95
		//
		// utmtst
		// Function:Affiliation. Typically used for brick and mortar
		// applications in ecommerce.
		// Example:utmtst=google%20mtv%20store
		//
		// utmtto
		// Function:Total. Values as for unit and price.
		// Example:utmtto=334.56
		//
		// utmttx
		// Function:Tax. Values as for unit and price.
		// Example:utmttx=29.16
		
		return url.toString();
	}
	
	private static void appendIfNotEmpty(StringBuffer url, String key, String value,
	        boolean urlEncode) {
		if(value != null) {
			url.append("&");
			url.append(key);
			url.append("=");
			url.append(urlEncode ? Utils.urlencode(value) : value);
		}
	}
	
}

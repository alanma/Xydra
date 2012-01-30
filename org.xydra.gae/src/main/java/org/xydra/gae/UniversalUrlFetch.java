package org.xydra.gae;

import java.io.IOException;
import java.net.URL;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.apache.http.client.ClientProtocolException;
import com.google.appengine.repackaged.org.apache.http.client.methods.HttpGet;
import com.google.appengine.repackaged.org.apache.http.impl.client.DefaultHttpClient;


/**
 * TODO run in GWT as well? requires a custom http response wrapper
 * 
 * @author xamde
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class UniversalUrlFetch {
	
	public static void callUrlAsync(String urlStr) throws IOException {
		if(AboutAppEngine.onAppEngine()) {
			URLFetchService service = URLFetchServiceFactory.getURLFetchService();
			service.fetchAsync(new URL(urlStr));
		} else {
			// TODO dont keep connections open (maybe we do that here)
			HttpGet request = new HttpGet(urlStr);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			try {
				httpClient.execute(request);
				request.abort();
			} catch(ClientProtocolException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}

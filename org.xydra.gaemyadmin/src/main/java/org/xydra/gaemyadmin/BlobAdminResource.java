package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;


public class BlobAdminResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BlobAdminResource.class);
	static final String PAGE_NAME = "Blob Admin";
	static String URL;
	
	public static void restless(Restless restless, String prefix) {
		URL = prefix + "/blobs";
		restless.addMethod(URL, "GET", BlobAdminResource.class, "index", true);
		restless.addMethod(URL, "POST", BlobAdminResource.class, "upload", true);
		restless.addMethod(URL, "GET", BlobAdminResource.class, "download", true,
		        new RestlessParameter("name")
		
		);
	}
	
	public void index(HttpServletResponse res, HttpServletRequest req) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Writer w = AppConstants.startPage(res, PAGE_NAME, "");
		
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		w.write("<b>Status</b><br/>Upload seems to work, download does not.<br />\n");
		
		w.write("<b>Upload</b><br/>");
		String url = blobStoreService.createUploadUrl("/admin" + URL,
		        UploadOptions.Builder.withMaxUploadSizeBytes(100 * 1024 * 1024));
		w.write(HtmlUtils.form(METHOD.POST, url).withInputFile("tempfile")
		        .withInputSubmit("Upload as 'tempfile'").toString());
		
		w.write("<b>Download</b><br/>");
		w.write(HtmlUtils.form(METHOD.GET, url).withInputText("name", "tempfile")
		        .withInputSubmit("Download").toString());
		
		AppConstants.endPage(w);
	}
	
	public void upload(HttpServletResponse res, HttpServletRequest req) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Writer w = AppConstants.startPage(res, PAGE_NAME, "");
		
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		Map<String,List<BlobKey>> blobs = blobStoreService.getUploads(req);
		w.write(HtmlUtils.toOrderedList(blobs.keySet()) + "<br/>\n");
		
		List<BlobKey> keys = blobs.get("tempfile");
		if(keys.isEmpty()) {
			w.write("No tempfiles uploaded");
		} else {
			for(BlobKey key : keys) {
				w.write("Have a tempfile uploaded. Key is '" + key.getKeyString() + "'");
			}
		}
		
		AppConstants.endPage(w);
	}
	
	public void download(String name, HttpServletResponse res, HttpServletRequest req)
	        throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey = new BlobKey(name);
		blobStoreService.serve(blobKey, res);
	}
}

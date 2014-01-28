package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xydra.base.id.UUID;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.testgae.server.rest.ConsistencyTestResource;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;


/**
 * A thread doing requests to a GAE-deployed app which exposes a
 * {@link ConsistencyTestResource} at URL '/consistency'
 * 
 * @author xamde
 * 
 */
public class ConsistencyTestClient extends Thread {
	
	public static int done = 0;
	
	private static boolean error = false;
	
	public static int failedReads = 0;
	
	public static int failedWrites = 0;
	
	private static final Logger log = LoggerFactory.getLogger(ConsistencyTestClient.class);
	
	/**
	 * Page format:
	 * 
	 * <pre>
	 * <div style='font-family: "Courier New", Courier, monospace;font-size: 13px;'>
	 * instanceId=OgkCF8E1I+=21-'Request E03D9CBC'<br/>
	 * version=210<br/>
	 * id=thread-7-187-BF8NR8a0<br/>
	 * id=thread-4-130-bLDPHS4U<br/>
	 * ...
	 * id=thread-5-85-FFkW3fRX<br/>
	 * size=210<br/>
	 * ----<br/>
	 * More HTML here for human users
	 * </pre>
	 * 
	 * @param list
	 * @return the page parsed as a list of items
	 * @throws IOException
	 */
	private static List<String> getIds(Page list) throws IOException {
		List<String> result = new ArrayList<String>();
		String s = list.getWebResponse().getContentAsString();
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line = br.readLine();
		assert line != null;
		// skip headers, if any
		while(!line.startsWith("instanceId")) {
			line = br.readLine();
			assert line != null;
		}
		assert line.startsWith("instanceId") : line;
		@SuppressWarnings("unused")
		String instance = line;
		line = br.readLine();
		assert line != null : "line was null, page is \n" + s;
		assert line.startsWith("version") : line;
		line = br.readLine();
		while(line != null && line.startsWith("id=")) {
			String id = line.substring(3);
			// strip trailing <br/>
			id = id.substring(0, id.length() - "<br/>".length());
			result.add(id);
			line = br.readLine();
		}
		if(line == null) {
			info(list, null);
			throw new RuntimeException("line = null");
		}
		assert line.startsWith("size") : line + " url=" + list.getUrl();
		String sizeStr = line.substring(5);
		// strip trailing <br/>
		sizeStr = sizeStr.substring(0, sizeStr.length() - "<br/>".length());
		int size = Integer.parseInt(sizeStr);
		assert result.size() == size : result.size() + " - " + size + " url=" + list.getUrl();
		// ignore all following lines
		br.close();
		return result;
	}
	
	/**
	 * @param page
	 * @return instanceId extracted from Page
	 * @throws IOException
	 */
	private static String getInstance(Page page) throws IOException {
		String s = page.getWebResponse().getContentAsString();
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line = br.readLine();
		// skip header, if any
		while(!line.startsWith("instanceId")) {
			line = br.readLine();
			assert line != null;
		}
		assert line != null;
		assert line.startsWith("instanceId") : line;
		String instance = line;
		br.close();
		return instance;
	}
	
	/**
	 * Helps to find parse errors
	 * 
	 * @param listPage
	 * @param createPage
	 */
	private static void info(Page listPage, Page createPage) {
		log.info("Url was: " + listPage.getUrl());
		log.info("Load time was:" + listPage.getWebResponse().getLoadTime());
		log.info("Verify Content was: " + listPage.getWebResponse().getContentAsString());
		if(createPage != null) {
			log.info("Create content was: " + createPage.getWebResponse().getContentAsString());
		}
	}
	
	private int rounds;
	
	private String serverRootUrl;
	
	private WebClient webClient;
	
	/**
	 * @param serverRootUrl
	 * @param name
	 * @param rounds
	 */
	public ConsistencyTestClient(String serverRootUrl, String name, int rounds) {
		this.serverRootUrl = serverRootUrl;
		BrowserVersion bv = BrowserVersion.getDefault();
		bv.setApplicationName("ConsistencyTestClient");
		bv.setApplicationCodeName("TestGae");
		bv.setApplicationVersion("20110914");
		bv.setUserAgent("ConsistencyTestClient");
		this.webClient = new WebClient(bv);
		this.webClient.setJavaScriptEnabled(false);
		this.setName(name);
		this.rounds = rounds;
	}
	
	/**
	 * @param id the item to create
	 * @param traceId
	 * @return Page for calling create
	 * @throws Exception
	 */
	public Page create(String id, String traceId) throws Exception {
		final Page page = getPage(this.serverRootUrl + "/consistency?create=" + id + "&traceId="
		        + traceId + "~create", 5, true);
		return page;
	}
	
	public Page doGetAddIdVerifyRead() throws Exception {
		log.info(this.getName() + " get-add-verify");
		// read
		Page pageList = getList("init");
		String instanceCreate = getInstance(pageList);
		List<String> ids = getIds(pageList);
		int before = ids.size();
		String newId = this.getName() + ids.size() + "-" + UUID.uuid(8);
		
		// create
		Page pageCreate = create(newId, instanceCreate);
		instanceCreate = getInstance(pageCreate);
		
		// read again & verify
		Page list = verifyListContains(instanceCreate, newId, before, pageCreate);
		return list;
	}
	
	/**
	 * @param trace which will appear in the request URL to help debugging
	 * @return a Page obtained by HTTP GET
	 * @throws Exception
	 */
	public Page getList(String trace) throws Exception {
		final Page page = getPage(this.serverRootUrl + "/consistency?trace=" + trace + "~getList",
		        10, false);
		return page;
	}
	
	/**
	 * Worker
	 * 
	 * @param url
	 * @param tries retry count
	 * @param writeAccess to log read/write errors correctly
	 * @return a Page obtained by HTTP GET
	 * @throws FailingHttpStatusCodeException
	 */
	private Page getPage(String url, int tries, boolean writeAccess)
	        throws FailingHttpStatusCodeException {
		Page page = null;
		int code = 0;
		int retries = 0;
		do {
			try {
				page = this.webClient.getPage(url);
				code = page.getWebResponse().getStatusCode();
				retries++;
			} catch(Exception e) {
				log.warn("Thread " + this.getName() + " got exception", e);
				if(writeAccess) {
					failedWrites++;
				} else {
					failedReads++;
				}
			}
		} while(page == null || (code == 500 && retries <= tries));
		if(retries == tries) {
			throw new RuntimeException("Retried URL '" + url + "' for " + tries
			        + " times, always got 500er");
		}
		return page;
	}
	
	@Override
	public void run() {
		boolean run = true;
		// each thread stops after first error
		while(run && !error && this.rounds > 0) {
			try {
				this.doGetAddIdVerifyRead();
			} catch(Throwable e) {
				error = true;
				run = false;
				log.warn("------ERROR", e);
				e.printStackTrace();
				System.out.println("------");
			}
			this.rounds--;
		}
		done++;
	}
	
	public void teardown() {
		// avoid memory leak
		this.webClient.closeAllWindows();
	}
	
	private Page verifyListContains(String instanceCreate, String newId, int before, Page creatPage)
	        throws Exception {
		String trace = instanceCreate + "&newid=" + newId;
		Page listPage = getList(trace);
		String instanceVerify = getInstance(listPage);
		List<String> ids = getIds(listPage);
		int after = ids.size();
		// FIXME !!!!!!!!!! HERE
		if(after < before) {
			String info = " createdOn " + instanceCreate + " verifiedOn " + instanceVerify;
			log.error("before:" + before + " after:" + after + info + " trace=" + trace);
			info(listPage, creatPage);
			failedWrites++;
		}
		/*
		 * higher snapshot version than created is returned, newId seems still
		 * missing.
		 */
		if(!ids.contains(newId)) {
			log.error("ids do not contain new id '" + newId + "' trace=" + trace);
			info(listPage, creatPage);
			failedWrites++;
		}
		return listPage;
	}
	
}

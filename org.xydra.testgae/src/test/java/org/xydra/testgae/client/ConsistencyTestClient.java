package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xydra.core.model.impl.memory.UUID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;


public class ConsistencyTestClient extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(ConsistencyTestClient.class);
	
	private static boolean error = false;
	
	private WebClient webClient;
	
	private String serverRootUrl;
	
	private int rounds;
	
	public static int failedWrites;
	
	public static int done = 0;
	
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
	
	public void teardown() {
		// avoid memory leak
		this.webClient.closeAllWindows();
	}
	
	public Page getList(String trace) throws Exception {
		final Page page = getPage(this.serverRootUrl + "/consistency?trace=" + trace + "~getList",
		        1);
		return page;
	}
	
	private Page getPage(String url, int tries) throws FailingHttpStatusCodeException {
		Page page = null;
		int code = 0;
		int retries = 0;
		do {
			try {
				page = this.webClient.getPage(url);
				code = page.getWebResponse().getStatusCode();
				retries++;
			} catch(Exception e) {
				failedWrites++;
			}
		} while(page == null || (code == 500 && retries < tries));
		if(retries == 10) {
			throw new RuntimeException("Retried URL '" + url + "' for " + tries
			        + " times, always got 500er");
		}
		return page;
	}
	
	public Page create(String id, String traceId) throws Exception {
		final Page page = getPage(this.serverRootUrl + "/consistency?create=" + id + "&traceId="
		        + traceId + "~create", 1);
		return page;
	}
	
	public Page doRandomAction() throws Exception {
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
	
	private void info(Page listPage, Page createPage) {
		log.info("Url was: " + listPage.getUrl());
		log.info("Load time was:" + listPage.getWebResponse().getLoadTime());
		log.info("Verify Content was: " + listPage.getWebResponse().getContentAsString());
		if(createPage != null) {
			log.info("Create content was: " + createPage.getWebResponse().getContentAsString());
		}
	}
	
	private String getInstance(Page page) throws IOException {
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
	
	private List<String> getIds(Page list) throws IOException {
		List<String> result = new ArrayList<String>();
		String s = list.getWebResponse().getContentAsString();
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line = br.readLine();
		assert line != null;
		// skip header, if any
		while(!line.startsWith("instanceId")) {
			line = br.readLine();
			assert line != null;
		}
		assert line.startsWith("instanceId") : line;
		@SuppressWarnings("unused")
		String instance = line;
		line = br.readLine();
		assert line != null;
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
	
	@Override
	public void run() {
		boolean run = true;
		while(run && !error && this.rounds <= 100 && failedWrites == 0) {
			try {
				this.doRandomAction();
			} catch(Throwable e) {
				error = true;
				log.warn("", e);
				run = false;
				log.warn("------ERROR", e);
				e.printStackTrace();
				System.out.println("------");
			}
			this.rounds++;
		}
		done++;
	}
	
}

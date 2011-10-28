package org.xydra.testgae.shared;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.xydra.core.util.Clock;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.testgae.server.model.xmas.NameUtils;


/**
 * A simulated user makes a number of actions, almost like a real user would do.
 * Idealy for performance testing.
 * 
 * @author xamde
 * 
 */
public class SimulatedUser extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(SimulatedUser.class);
	
	private String serverUrl;
	
	private String repoIdStr;
	
	private int actions = 0;
	
	private Exchanger<Exception> exchanger;
	
	int getActions() {
		return this.actions;
	}
	
	private static int random(int max) {
		return 1 + (int)(Math.random() * max);
	}
	
	private static enum Action {
		CreateList(10), DeleteList(1), EditListAddWish(20), EditListDeleteWish(5), EditListEditWishName(
		        10), EditListEditWishPrice(10), EditListEditWishUrl(10);
		
		private int howOften;
		
		Action(int howOften) {
			this.howOften = howOften;
		}
		
		static Action randomAction() {
			int sum = 0;
			for(Action a : Action.values()) {
				sum += a.howOften;
			}
			int rnd = random(sum);
			
			sum = 0;
			for(Action a : Action.values()) {
				sum += a.howOften;
				if(sum >= rnd) {
					return a;
				}
			}
			throw new AssertionError("Algorithm has a bug");
		}
	}
	
	/**
	 * 
	 * @param serverUrl should not include repository. May not have a trailing
	 *            slash at the end.
	 */
	public SimulatedUser(String serverUrl, String repo, Exchanger<Exception> exchanger) {
		super();
		this.serverUrl = serverUrl;
		this.repoIdStr = repo;
		
		// TODO Document
		this.exchanger = exchanger;
	}
	
	/**
	 * @param w can be null
	 * @throws IOException ...
	 */
	public void doBenchmark1(Writer w) throws IOException {
		Clock c = new Clock();
		
		// add 20 lists
		c.start();
		doAction(Action.CreateList, 10);
		c.stop("CreateList");
		doAction(Action.CreateList, 10);
		
		// delete 10 lists
		c.start();
		doAction(Action.DeleteList, 10);
		c.stop("DeleteList");
		
		// add 20 wishes
		c.start();
		doAction(Action.EditListAddWish, 10);
		c.stop("EditListAddWish");
		doAction(Action.EditListAddWish, 10);
		
		// delete 10 wishes
		c.start();
		doAction(Action.EditListDeleteWish, 10);
		c.stop("EditListDeleteWish");
		
		if(w != null) {
			w.write(c.getStats());
		}
		log.info("Benchmark1: " + c.getStats());
	}
	
	@Override
	public void run() {
		Exception exception = null;
		
		while(!this.stopSoon) {
			try {
				doRandomAction();
			} catch(RuntimeException e) {
				
				exception = e;
				
				if(e.getCause().getClass().equals(SocketTimeoutException.class)) {
					if(this.stopSoon) {
						// fine
						log.info("Socket timeout", e);
					} else {
						log.warn("Socket timeout", e);
					}
				}
				
				break;
			}
			yield();
		}
		
		// TODO document exchange
		try {
			this.exchanger.exchange(exception);
		} catch(InterruptedException ie) {
			// do nothing
		}
		log.info("Thread done");
	}
	
	public void doRandomAction() {
		/* choose action */
		Action action = Action.randomAction();
		doAction(action);
	}
	
	/**
	 * Do action n times
	 * 
	 * @param action ..
	 * @param n ..
	 */
	public void doAction(Action action, int n) {
		for(int i = 0; i < n; i++) {
			doAction(action);
		}
	}
	
	public void doAction(Action action) {
		log.info("Trying action " + this.actions + " = " + action);
		/*
		 * some actions make no sense under some circumstances, but we cannot
		 * know, so we just try
		 */
		if(action == Action.CreateList) {
			/* create 1 new list with 0 wishes */
			HttpUtils.makeGetRequest(this.serverUrl + "/xmas/" + this.repoIdStr
			        + "/add?lists=1&wishes=0");
			log.info("List created");
		} else {
			// choose a list - requires an HTTP GET
			String rootRelativeListUrl = chooseList(this.repoIdStr);
			if(rootRelativeListUrl != null) {
				assert !rootRelativeListUrl.endsWith("/");
				assert rootRelativeListUrl.startsWith("/") : "url is '" + rootRelativeListUrl + "'";
				String absoluteListUrl = this.serverUrl + rootRelativeListUrl;
				if(action == Action.DeleteList) {
					HttpUtils.makeGetRequest(absoluteListUrl + "/clear");
					log.info("Deleted list");
				} else {
					// action in list
					if(action == Action.EditListAddWish) {
						// add a wish
						HttpUtils.makeGetRequest(absoluteListUrl + "/add?wishes=1");
						log.info("Added wish");
					} else {
						// choose a wish from list
						String wishUrl = chooseWish(absoluteListUrl);
						if(wishUrl != null) {
							if(action == Action.EditListDeleteWish) {
								// delete wish
								HttpUtils.makeGetRequest(absoluteListUrl + "/delete");
								log.info("Deleted wish");
							} else {
								// edit wish
								if(action == Action.EditListEditWishName) {
									String name = NameUtils.getProductName();
									
									HttpUtils.makeGetRequest(absoluteListUrl + "/editName?name="
									        + name.replace(" ", "+"));
									log.info("Edited name of wish");
								} else if(action == Action.EditListEditWishPrice) {
									int price = (int)(Math.random() * 1000);
									HttpUtils.makeGetRequest(absoluteListUrl + "/editPrice?price="
									        + price);
									log.info("Edited price of wish");
								} else {
									assert (action == Action.EditListEditWishUrl);
									String url = "http://www.google.de/images?q="
									        + NameUtils.getProductName();
									HttpUtils.makeGetRequest(absoluteListUrl + "/editUrl?url="
									        + url.replace(" ", "+"));
									log.info("Edited url of wish");
								}
								
							}
						} else {
							log.info("Found no wish for " + action);
						}
					}
				}
			} else {
				log.info("Found no list for " + action);
			}
		}
		this.actions++;
	}
	
	/**
	 * @return a randomly chosen list URL or null of none found
	 */
	private String chooseList(String repoIdStr) {
		/* retrieve list of list urls */
		List<String> listUrls = listAllListsInRepository(repoIdStr);
		log.info("Choosing one of " + listUrls.size() + " lists");
		return randomEntry(listUrls);
	}
	
	private String randomEntry(List<String> entries) {
		if(entries.size() == 0) {
			return null;
		}
		int rnd = random(entries.size()) - 1;
		return entries.get(rnd);
	}
	
	private String chooseWish(String listUrl) {
		/* retrieve list of wish urls */
		List<String> wishUrls = listAllWishesInList(listUrl);
		log.info("Choosing one of " + wishUrls.size() + " wishes");
		return randomEntry(wishUrls);
	}
	
	/**
	 * @param absoluteUrl should inclide the repository but not end with a slash
	 * @return a list of all URLs of wish lists
	 */
	private List<String> listAllListsInRepository(String repoIdStr) {
		String content = HttpUtils.getRequestAsStringResponse(this.serverUrl + "/xmas/" + repoIdStr
		        + "?format=urls");
		return stringParsedByLinebreaks(content);
	}
	
	private List<String> stringParsedByLinebreaks(String content) {
		String[] lines = content.split("\\n");
		List<String> list = new ArrayList<String>();
		for(String line : lines) {
			if(line.length() > 0) {
				list.add(line);
			}
		}
		return list;
	}
	
	/**
	 * @param listId ..
	 * @param absoluteUrl should inclide the repository but not end with a slash
	 * @return a list of all wish URLs in given wish list
	 */
	private List<String> listAllWishesInList(String listUrl) {
		assert listUrl.startsWith("http");
		String content = HttpUtils.getRequestAsStringResponse(listUrl + "?format=urls");
		return stringParsedByLinebreaks(content);
	}
	
	private boolean stopSoon = false;
	
	public synchronized void pleaseStopSoon() {
		log.info("Requested to stop");
		this.stopSoon = true;
	}
	
	public static void main(String[] args) {
		// TODO fix the SimulatedUser creation
		SimulatedUser su = new SimulatedUser("http://localhost:8787", "repo1", null);
		System.out.println(su.listAllWishesInList(su.listAllListsInRepository("repo1").get(0)));
		su.pleaseStopSoon();
		System.out.println("Performed " + su.getActions() + " actions");
	}
	
}

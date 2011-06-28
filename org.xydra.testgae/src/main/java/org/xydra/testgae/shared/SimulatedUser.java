package org.xydra.testgae.shared;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
	
	int getActions() {
		return this.actions;
	}
	
	private static int random(int max) {
		return 1 + (int)(Math.random() * max);
	}
	
	private static enum Action {
		CreateList(50), DeleteList(1), EditListAddWish(200), EditListDeleteWish(5), EditListEditWish(
		        10);
		
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
	public SimulatedUser(String serverUrl, String repo) {
		super();
		this.serverUrl = serverUrl;
		this.repoIdStr = repo;
	}
	
	@Override
	public void run() {
		while(!this.stopSoon) {
			try {
				doRandomAction();
			} catch(RuntimeException e) {
				if(e.getCause().getClass().equals(SocketTimeoutException.class)) {
					if(this.stopSoon) {
						// fine
						log.info("Socket timeout", e);
					} else {
						log.warn("Socket timeout", e);
					}
				}
			}
			this.actions++;
			yield();
		}
		log.info("Thread done");
	}
	
	private void doRandomAction() {
		/* choose action */
		Action action = Action.randomAction();
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
								assert (action == Action.EditListEditWish);
								// TODO edit wish, not yet implemented
								log.info("Edit wish - not yet impl");
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
		SimulatedUser su = new SimulatedUser("http://localhost:8787", "repo1");
		System.out.println(su.listAllWishesInList(su.listAllListsInRepository("repo1").get(0)));
		su.pleaseStopSoon();
		System.out.println("Performed " + su.getActions() + " actions");
	}
	
}

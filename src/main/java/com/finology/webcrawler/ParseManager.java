package com.finology.webcrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;

public class ParseManager {
	private final ExecutorService executorService;
	private static final long PAUSE_TIME = 1000;
	private final Set<URL> masterList = new HashSet<>();
	private final List<Future<ParsePage>> futures = new ArrayList<>();
	private final BiPredicate<URL, Integer> shouldVisit;

	public ParseManager( BiPredicate<URL, Integer> shouldVisit, int THREAD_COUNT) {
		this.shouldVisit = shouldVisit;
		this.executorService = Executors.newFixedThreadPool(THREAD_COUNT);
	}

	public void go(URL start) throws IOException, InterruptedException, Exception {
		submitNewURL(start, 0);
		while (checkPageParses())
			;
		executorService.shutdown();
	}

	/**
	 * This method is charged with checking the status of all the threads
	 * and collecting their work effort.
	 *
	 * @return false = all the threads are done.
	 * @throws InterruptedException
	 */
	private boolean checkPageParses() throws InterruptedException, Exception{
		Thread.sleep(PAUSE_TIME);
		Set<ParsePage> pageSet = new HashSet<>();
		Iterator<Future<ParsePage>> iterator = futures.iterator();

		while (iterator.hasNext()) {
			Future<ParsePage> future = iterator.next();
			if (future.isDone()) {
				iterator.remove();
				pageSet.add(future.get());
			}
		}

		for (ParsePage parsePage : pageSet) {
			addNewURLs(parsePage);
		}

		return (futures.size() > 0);
	}

	/**
	 * Get the URLs from the parse page object.
	 * remove any anchor references
	 * save the url into the to-do list.
	 *
	 * @param parsePage object containing the URL list
	 */
	private void addNewURLs(ParsePage parsePage) throws Exception {
		for (URL url : parsePage.getUrlList()) {
			if (url.toString().contains("#")) {
				try {
					url = new URL(url.toString().substring(0, url.toString().indexOf("#")));
				} catch (MalformedURLException e) {
				}
			}

			testAndSubmitNewURL(url, parsePage.getDepth() + 1);
		}
	}

	/**
	 * Check if the URL passes muster and add it to the work list
	 *
	 * @param url
	 * @param depth
	 */
	private void testAndSubmitNewURL(URL url, int depth) throws Exception {
		if (internalShouldVisit(url)
				&& shouldVisit.test(url, depth)) {  // ask the BiPredicate
			submitNewURL(url, depth);
		}
	}

	/**
	 * Do the work of actually adding a work item.
	 *
	 * @param url
	 * @param depth
	 */
	private void submitNewURL(URL url, int depth) throws Exception{
		masterList.add(url);

		ParsePage parsePage = new ParsePage(url, depth);
		Future<ParsePage> future = executorService.submit(parsePage);
		futures.add(future);
	}


	/**
	 * Some things we need to control inside the manager itself.
	 * Like, do not visit the same page twice.
	 * @param url
	 * @return
	 */
	private boolean internalShouldVisit(URL url) {
		if (masterList.contains(url)) {
			return false;
		}
		return true;
	}

	public Set<URL> getMasterList() {
		return masterList;
	}

}

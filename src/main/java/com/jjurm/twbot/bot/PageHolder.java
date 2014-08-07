package com.jjurm.twbot.bot;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class that loads pages and stores them for future use. Also it helps with
 * cleaning the pages up.
 * 
 * @author JJurM
 */
public class PageHolder {

	/**
	 * WebClient to use
	 */
	TribalWars tribalWars;

	/**
	 * Map of loaded pages paired with URL as key
	 */
	Map<URL, PageData> pages = new HashMap<URL, PageData>();

	/**
	 * List of all pages in the PageHolder, to clean them all up
	 */
	List<PageData> allPages = new ArrayList<PageData>();

	/**
	 * Basic constructor
	 * 
	 * @param webClient
	 */
	public PageHolder(TribalWars tribalWars) {
		this.tribalWars = tribalWars;
	}
	
	protected synchronized void put(URL url, PageData pageData) {
		pages.put(url, pageData);
		allPages.add(pageData);
	}

	/**
	 * Simply calls {@link PageData#getPage()} on result from
	 * {@link #getPageData(URL)}.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws FailingHttpStatusCodeException
	 */
	public synchronized HtmlPage get(URL url) throws FailingHttpStatusCodeException, IOException {
		return getPageData(url).getPage();
	}

	/**
	 * This will search URL in <tt>pages</tt> map and return associated
	 * <tt>PageData</tt>. If not found, it will load it, store it and then
	 * return.
	 * 
	 * @param url
	 * @return
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public synchronized PageData getPageData(URL url) throws FailingHttpStatusCodeException,
			IOException {
		if (pages.containsKey(url)) {
			return pages.get(url);
		}
		PageData pageData = tribalWars.getPageData(url);
		put(url, pageData);
		add(pageData);
		return pageData;
	}

	/**
	 * Adds the given <tt>PageData</tt> object to the map (if the URL key
	 * doesn't exists).
	 * 
	 * @param pageData <tt>PageData</tt> to add
	 * @return the same object
	 */
	public synchronized PageData add(PageData pageData) {
		URL url = pageData.getPage().getUrl();
		put(url, pageData);
		return pageData;
	}

	/**
	 * Constructs new <tt>PageData</tt> from the given <tt>HtmlPage</tt> and
	 * adds it to the map (if the URL key doesn't exists).
	 * 
	 * @param page <tt>HtmlPage</tt> to add
	 * @return the <tt>PageData</tt> object
	 */
	public synchronized PageData add(HtmlPage page) {
		return add(new PageData(page));
	}

	/**
	 * This will clean up all pages and remove them from list.
	 */
	public synchronized void cleanUp() {
		while (allPages.size() > 0) {
			PageData pageData = allPages.get(0);
			pageData.cleanUp();
			pages.remove(pageData);
			allPages.remove(pageData);
		}
	}

}

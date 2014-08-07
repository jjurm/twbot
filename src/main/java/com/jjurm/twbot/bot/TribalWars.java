package com.jjurm.twbot.bot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.jjurm.twbot.system.config.Config;
import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.tribalwars.World;

public class TribalWars extends WebClient {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger();

	World world;
	HierarchicalConfiguration state;
	DelayGenerator delayGenerator;

	/**
	 * Constructor, that can try to firstly load cookies. If it's unsuccessful,
	 * it will log in and overwrite old cookies.
	 * 
	 * @param world
	 * @param loadCookies
	 * @throws IOException
	 * @throws FailingHttpStatusCodeException
	 */
	public TribalWars(World world, HierarchicalConfiguration state, DelayGenerator delayGenerator,
			boolean loadCookies)
			throws FailingHttpStatusCodeException, IOException {
		super(Config.browserVersion);
		this.world = world;
		this.state = state;
		this.delayGenerator = delayGenerator;

		setOptions();

		if (loadCookies) {
			LOG.debug("Trying to reuse stored cookies");
			loadCookies();
			boolean wrong = ensureLogin();
			if (!wrong) {
				LOG.info("Successfully reused stored cookies");
			}
		} else {
			LOG.debug("Logging in");
			login();
		}

	}

	/**
	 * This will load requested page. If the page is sid_wrong page, it will
	 * login again and then load and return requested page.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws FailingHttpStatusCodeException
	 */
	public synchronized HtmlPage get(URL url) throws IOException, FailingHttpStatusCodeException {
		delayGenerator.sleepRequestDelay();
		HtmlPage p = getPage(url);
		boolean wrong = ensureLogin(p);
		if (wrong) {
			delayGenerator.sleepRequestDelay();
			p.cleanUp();
			return getPage(url);
		} else {
			return p;
		}
	}

	/**
	 * This calls {@link #get(URL)} method to retrieve a page. Note that also
	 * this method is synchronized to ensure that only one page is being loaded
	 * at a time.
	 * 
	 * @param url
	 * @return
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public synchronized PageData getPageData(URL url) throws FailingHttpStatusCodeException,
			IOException {
		return new PageData(get(url));
	}

	/**
	 * This will ensure that the web client is logged into the tribal-wars
	 * server.
	 * 
	 * @return <tt>true</tt> if login had to be done, <tt>false</tt> if it's
	 *         already logged in
	 */
	boolean ensureLogin() {
		try {
			delayGenerator.sleepRequestDelay();
			HtmlPage p = getPage(Urls.getOverviewVillagesUrl());
			boolean wrong = ensureLogin(p);
			p.cleanUp();
			return wrong;
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * This will ensure that the web client is logged into the tribal-wars
	 * server.
	 * 
	 * @param p TribalWarsPage to check instead of loading new one
	 * @return <tt>true</tt> if new login have been done, <tt>false</tt> if it's
	 *         already logged in
	 */
	boolean ensureLogin(HtmlPage p) {
		boolean wrong = Urls.isSidWrongUrl(p.getUrl());
		if (wrong) {
			LOG.warn("Wrong sid, logging in");
			try {
				login();
			} catch (FailingHttpStatusCodeException | IOException e) {
				e.printStackTrace();
			}
		}
		return wrong;
	}

	/**
	 * Login to tribal wars game and save cookies.
	 * 
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public void login() throws FailingHttpStatusCodeException, IOException {
		delayGenerator.sleepRequestDelay();
		HtmlPage p = getPage(Urls.getLoginUrl());
		if (Urls.isSidWrongUrl(p.getUrl())) {
			LOG.warn("First logging attempt failed, retrying");
			p.cleanUp();
			delayGenerator.sleepRequestDelay();
			p = getPage(Urls.getLoginUrl());
			if (Urls.isSidWrongUrl(p.getUrl())) {
				LOG.error("Can't login to the TribalWars server");
				p.cleanUp();
				return;
			}
		}
		p.cleanUp();
		LOG.info("Logged in.");
		saveCookies();
	}

	/**
	 * This will disable warnings from HtmlUnit library
	 */
	public static void disableHtmlUnitWarnings() {

		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

	}

	/**
	 * This will set preferred values for some options.
	 */
	void setOptions() {
		WebClientOptions options = getOptions();

		options.setCssEnabled(false);
		//getOptions().setJavaScriptEnabled(false);

		options.setThrowExceptionOnScriptError(false);
		options.setThrowExceptionOnFailingStatusCode(false);

		setJavaScriptTimeout(2000);

		setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {}
		});

		setCssErrorHandler(new ErrorHandler() {
			@Override
			public void warning(CSSParseException arg0) throws CSSException {}
			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {}
			@Override
			public void error(CSSParseException arg0) throws CSSException {}
		});

		setJavaScriptErrorListener(new JavaScriptErrorListener() {
			@Override
			public void timeoutError(HtmlPage htmlPage, long allowedTime, long executionTime) {}
			@Override
			public void scriptException(HtmlPage htmlPage, ScriptException scriptException) {}
			@Override
			public void malformedScriptURL(HtmlPage htmlPage, String url, MalformedURLException malformedURLException) {}
			@Override
			public void loadScriptError(HtmlPage htmlPage, URL scriptUrl, Exception exception) {}
		});

	}

	/**
	 * This will save current cookies to a file (with overwriting). This saves
	 * only the cookies needed for session identifying (cookie named
	 * <tt>sid</tt>).
	 */
	public synchronized void saveCookies() {
		CookieManager cm = getCookieManager();
		for (String name : Config.cookiesToStore) {
			Cookie cookie = cm.getCookie(name);
			if (cookie != null) {
				state.setProperty("cookie[@name='" + name + "']", cookie.getValue());
			}
		}
	}

	/**
	 * This will load cookies from a file.
	 */
	public synchronized void loadCookies() {
		CookieManager cm = getCookieManager();
		String domain = Urls.getDomain();
		for (String name : Config.cookiesToStore) {
			String value = state.getString("cookie[@name='" + name + "']", "0");
			if (value != null) {

				Cookie cookie = new Cookie(domain, name, value);
				cm.addCookie(cookie);
			}
		}
	}

	/**
	 * @return current SID cookie value
	 */
	public String getSid() {
		CookieManager cm = getCookieManager();
		return cm.getCookie("sid").getValue();
	}

	/**
	 * @param sid new SID cookie value
	 */
	public synchronized void setSid(String sid) {
		CookieManager cm = getCookieManager();
		cm.addCookie(new Cookie(Urls.getDomain(), "sid", sid));
	}

	@Override
	public synchronized void closeAllWindows() {
		super.closeAllWindows();
	}

}

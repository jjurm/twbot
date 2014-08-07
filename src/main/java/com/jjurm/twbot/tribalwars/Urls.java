package com.jjurm.twbot.tribalwars;

import java.net.MalformedURLException;
import java.net.URL;

public class Urls {
	private Urls() {} // Prevent instantiating

	static World world;

	public static void setWorld(World world) {
		Urls.world = world;
	}

	/**
	 * This will return given string as <tt>URL</tt> or <tt>null</tt>
	 * 
	 * @param url
	 * @return
	 */
	static URL url(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns domain name of the world in following format:
	 * 
	 * <pre>
	 * sk27.divoke-kmene.sk
	 * </pre>
	 * 
	 * @return
	 */
	public static String getDomain() {
		return world.getName() + "." + world.getHost();
	}

	/**
	 * This decides whether the <tt>URL</tt> specifies sid_wrong page.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isSidWrongUrl(URL url) {
		String path = url.getPath();
		return path.equals("/sid_wrong.php");
	}

	/**
	 * This will return base string in following format:
	 * 
	 * <pre>
	 * http://sk27.divoke-kmene.sk/
	 * </pre>
	 * 
	 * @return String base for urls
	 */
	public static String getBase() {
		return "http://" + getDomain() + "/";
	}

	/**
	 * This will return base string of <tt>game.php</tt> in format:
	 * 
	 * <pre>
	 * http://sk27.divoke-kmene.sk/game.php?village=1331&
	 * 
	 * @param villageId village ID
	 * @return
	 */
	static String getGameBase(int villageId) {
		return getBase() + "game.php?village=" + villageId + "&";
	}

	/**
	 * @return Url for login to TribalWars
	 */
	public static URL getLoginUrl() {
		String url = "";
		url += getBase() + "login.php?user=" + world.getPlayer();
		url += "&password=" + world.getPassword();
		url += "&utf-8";
		return url(url);
	}


	// ========== Player urls ==========


	/**
	 * @return Url of overview of player's villages
	 */
	public static URL getOverviewVillagesUrl() {
		return url(getBase() + "game.php?screen=overview_villages");
	}

	/**
	 * @return Url of report list
	 */
	public static URL getReportUrl() {
		return url(getBase() + "game.php?screen=report");
	}

	/**
	 * @return Url of attack report list
	 */
	public static URL getAttackReportUrl() {
		return url(getBase() + "game.php?mode=attack&screen=report");
	}


	// ========== Village urls ==========


	/**
	 * @param id village ID
	 * @return Url of village overview
	 */
	public static URL getOverviewUrl(int id) {
		return url(getGameBase(id) + "screen=overview");
	}

	/**
	 * @param id village ID
	 * @return Url for training units in current village
	 */
	public static URL getTrainUrl(int id) {
		return url(getGameBase(id) + "screen=train");
	}

	/**
	 * @param id village ID
	 * @return Url of place
	 */
	public static URL getPlaceUrl(int id) {
		return url(getGameBase(id) + "screen=place");
	}

	/**
	 * @param id current village ID
	 * @param target ID of the target village
	 * @return Url of place with the target input field filled
	 * @deprecated Fill in coords inputs in a place page, instead of loading
	 *             pre-filled page, to reduce number of requests.
	 */
	@Deprecated
	public static URL getPlaceWithTargetUrl(int id, int target) {
		return url(getGameBase(id) + "target=" + target + "&screen=place");
	}

}

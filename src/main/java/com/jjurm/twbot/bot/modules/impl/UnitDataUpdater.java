package com.jjurm.twbot.bot.modules.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.PageData;
import com.jjurm.twbot.bot.PageHolder;
import com.jjurm.twbot.bot.modules.AbstractModule;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.bot.modules.ModuleFactory;
import com.jjurm.twbot.tribalwars.TWEnvironment;
import com.jjurm.twbot.tribalwars.Urls;

/**
 * Class that updates counts of units in own village.
 * 
 * @author JJurM
 */
/**
 * @author JJurM
 * 
 */
public class UnitDataUpdater extends AbstractModule {

	/**
	 * Basic constructor
	 * 
	 * @param bot
	 */
	public UnitDataUpdater(Bot bot) {
		super(bot);
	}

	@Override
	public void processOverviewVillages(PageData overview, PageHolder pageHolder) {}

	@Override
	public void processVillage(int villageId, PageData village, PageHolder pageHolder) {
		try {
			HtmlPage trainPage = pageHolder.get(Urls.getTrainUrl(villageId));
			if (trainPage == null)
				return;

			Map<String, Integer> unitsCounts = new HashMap<String, Integer>();

			String xpath1 = "//*[@id='train_form']/table//tr[contains(@class,'row_a') or contains(@class,'row_b')]";
			@SuppressWarnings("unchecked")
			List<HtmlElement> rows = (List<HtmlElement>) trainPage.getByXPath(xpath1);

			for (HtmlElement row : rows) {
				
				// get unit name
				HtmlAnchor a = row.getFirstByXPath("./td[1]/a");
				String js = a.getOnClickAttribute();
				Pattern pattern = Pattern.compile("'([^']+)'");
				Matcher matcher = pattern.matcher(js);
				matcher.find();
				String unitName = matcher.group(1);
				
				// get unit counts
				HtmlElement td = (HtmlElement) row.getByXPath(".//td[not(*)]").get(0);
				String[] parts = td.getTextContent().trim().split("/");
				try {
					int homeCount = Integer.parseInt(parts[0]);
					int totalCount = Integer.parseInt(parts[1]);
					unitsCounts.put("unit_home_" + unitName, homeCount);
					unitsCounts.put("unit_" + unitName, totalCount);
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
					e.printStackTrace();
					continue;
				}
			}

			try (Connection con = bot.getConnection()) {

				updateDb(con, villageId, "unit", toArray(unitsCounts, "unit_"));
				updateDb(con, villageId, "unit_home", toArray(unitsCounts, "unit_home_"));

				con.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			trainPage.cleanUp();
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates data in the database. Note, that this method doesn't trigger
	 * {@link Connection#commit()} method.
	 * 
	 * @param villageId
	 * @param type
	 * @param troops
	 */
	void updateDb(Connection con, int villageId, String type, int[] troops) {
		String query = "CALL update_" + type + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, villageId);
			for (int i = 0; i < TWEnvironment.units.length; i++) {
				stmt.setInt(i + 2, troops[i]);
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a map of counts of units to an array.
	 * 
	 * @param unitsCounts
	 * @param prefix string before units' names
	 * @return
	 */
	public int[] toArray(Map<String, Integer> unitsCounts, String prefix) {
		int[] troops = new int[TWEnvironment.units.length];
		for (int i = 0; i < TWEnvironment.units.length; i++) {
			Integer c = unitsCounts.get(prefix + TWEnvironment.units[i]);
			troops[i] = c == null ? 0 : c;
		}
		return troops;
	}

	/**
	 * Converts an array of counts of units to a map.
	 * 
	 * @param troops
	 * @param prefix string before units' names
	 * @return
	 */
	public Map<String, Integer> toMap(int[] troops, String prefix) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < TWEnvironment.units.length; i++) {
			map.put(prefix + TWEnvironment.units[i], troops[i]);
		}
		return map;
	}

	/**
	 * This will process a page of the place in user's village and update DB
	 * with counts of units.
	 * 
	 * @param page
	 */
	public void processPlace(int villageId, HtmlPage page) {
		Map<String, Integer> unitsCounts = new HashMap<String, Integer>();

		String xpath = "//input[contains(@class,'unitsInput')]/following-sibling::a";
		@SuppressWarnings("unchecked")
		List<HtmlAnchor> as = (List<HtmlAnchor>) page.getByXPath(xpath);

		for (int i = 0; i < TWEnvironment.units.length; i++) {
			String s = as.get(i).getTextContent();
			int count = Integer.parseInt(s.substring(1, s.length() - 1));
			unitsCounts.put("unit_home_" + TWEnvironment.units[i], count);
		}

		try (Connection con = bot.getConnection()) {

			updateDb(con, villageId, "unit_home", toArray(unitsCounts, "unit_home_"));

			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public static class Factory implements ModuleFactory {

		@Override
		public Module createModule(Bot bot) {
			return new UnitDataUpdater(bot);
		}

	}

}

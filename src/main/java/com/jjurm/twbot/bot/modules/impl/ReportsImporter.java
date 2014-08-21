package com.jjurm.twbot.bot.modules.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.InterruptedExecutionException;
import com.jjurm.twbot.bot.PageData;
import com.jjurm.twbot.bot.PageHolder;
import com.jjurm.twbot.bot.modules.AbstractModule;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.bot.modules.ModuleFactory;
import com.jjurm.twbot.control.Command;
import com.jjurm.twbot.tribalwars.TWEnvironment;
import com.jjurm.twbot.tribalwars.Urls;

/**
 * PlayerProcessor module that imports reports.
 * 
 * @author JJurM
 */
public class ReportsImporter extends AbstractModule {
	private static final Logger LOG = LogManager.getLogger();

	HierarchicalConfiguration state;

	{
		commands.put("start", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				PageHolder pageHolder = new PageHolder(bot.getTribalWars());
				try {
					processOverviewVillages(null, pageHolder);
				} catch (InterruptedExecutionException e) {
					e.printStackTrace();
				}
				pageHolder.cleanUp();
			}
		});
	}

	/**
	 * Basic constructor
	 * 
	 * @param bot
	 * @param state
	 */
	public ReportsImporter(Bot bot, HierarchicalConfiguration state) {
		super(bot);
		this.state = state;
	}

	@Override
	public void processOverviewVillages(PageData p, PageHolder pageHolder)
			throws InterruptedExecutionException {
		List<HtmlPage> pages = new ArrayList<HtmlPage>();
		HtmlPage page, report = null;
		try (Connection con = bot.getConnection()) {
			int lastReport = lastReport();
			int id = 0;

			checkInterruption();
			LOG.trace("ReportsImporter: caching reports pages");
			page = pageHolder.get(Urls.getAttackReportUrl());
			pages.add(page);
			while (lastReport < lastReportOnPage(page)) {
				checkInterruption();
				bot.dg().sleepRequestDelay();
				page = nextPage(page);
				if (page == null) {
					break;
				}
				pages.add(page);
			}
			int count = 0;
			checkInterruption();
			LOG.trace("Processing reports");
			try {
				for (int i = pages.size() - 1; i >= 0; i--) {
					List<?> list = pages.get(i)
							.getByXPath("//table[@id='report_list']//tr[not(th)]/td/span");
					for (int j = list.size() - 1; j >= 0; j--) {
						checkInterruption();
						HtmlElement span = (HtmlElement) list.get(j);
						id = getId(span);
						if (id <= lastReport) {
							continue;
						}
						HtmlElement a = span.getFirstByXPath(".//a");

						try {
							bot.dg().sleepRequestDelay();
							report = a.click();
							processReport(report, con);
							report.cleanUp();
							count++;
						} catch (NullPointerException | IOException e) {
							e.printStackTrace();
						} finally {
							if (report != null) {
								report.cleanUp();
							}
						}
					}
				}
			} finally {
				if (count > 0) {
					LOG.info("Imported " + count + " reports.");
				} else {
					LOG.info("No new reports.");
				}
				saveLastReport(id);
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			for (HtmlPage hp : pages) {
				hp.cleanUp();
			}
		}
	}

	HtmlPage nextPage(HtmlPage page) {
		String xpath = "((//table[contains(@class,'main')]//table[contains(@class,'no_spacing')]"
				+ "//table[contains(@class,'vis') and not(contains(@class,'modemenu')) and not(@id='report_list')])[1]/tbody/tr)[last()]"
				+ "//strong/following-sibling::a[1]";
		HtmlElement a = page.getFirstByXPath(xpath);
		if (a == null) {
			return null;
		}
		try {
			return a.click();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	int lastReportOnPage(HtmlPage page) {
		HtmlElement lastSpan = page
				.getFirstByXPath("(//table[@id='report_list']//tr[not(th)]/td/span)[last()]");
		if (lastSpan == null)
			return 0;
		return getId(lastSpan);
	}

	int getId(Element span) {
		String textId = span.getAttribute("data-id");
		int id = Integer.parseInt(textId);
		return id;
	}

	void processReport(HtmlPage page, Connection con) throws SQLException {

		List<String> resources = Arrays.asList(TWEnvironment.resources);
		List<String> buildings = Arrays.asList(TWEnvironment.buildings);

		String xpath1 = "//table[contains(@class,'vis')]//td[contains(@class,'nopad')]/table[contains(@class,'vis')][2]";
		HtmlElement report = page.getFirstByXPath(xpath1);
		//HtmlElement reportContent = report.getFirstByXPath("./tbody/tr[3]/td");


		// ===== Time =====
		HtmlElement td = report.getFirstByXPath("./tbody/tr[2]/td[2]");
		String time = td.getTextContent().trim();
		long timestamp = processTime(time).getMillis();


		// ===== Village ID (defender) =====
		HtmlElement span = page
				.getFirstByXPath("//*[@id='attack_info_def']/tbody/tr[2]/td[2]/span");
		String textId = span.getAttribute("data-id");
		int id = Integer.parseInt(textId);

		// ===== Defender's units =====
		String query1 = "CALL update_unit_home_timestamp(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String query2 = "CALL update_unit_timestamp(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		HtmlElement tbody = page.getFirstByXPath("//*[@id='attack_info_def_units']/tbody");
		if (tbody != null) {
			HtmlElement unit_out = page
					.getFirstByXPath("//*[@id='attack_spy_away']//table[contains(@class,'vis')]/tbody/tr[2]");
			boolean out = unit_out != null;
			try (PreparedStatement stmt_unit_home = con.prepareStatement(query1);
					PreparedStatement stmt_unit = con.prepareStatement(query2)) {
				stmt_unit_home.setInt(1, id);
				stmt_unit_home.setTimestamp(2, new Timestamp(timestamp));
				if (out) {
					stmt_unit.setInt(1, id);
					stmt_unit.setTimestamp(2, new Timestamp(timestamp));
				}
				for (int i = 0; i < TWEnvironment.units.length; i++) {
					DomText start_count_el = tbody.getFirstByXPath(
							"./tr[2]/td[contains(@class,'unit-item')][" + (i + 1) + "]/text()");
					int start_count = Integer.parseInt(start_count_el.getTextContent().trim());
					DomText loss_count_el = tbody.getFirstByXPath(
							"./tr[3]/td[contains(@class,'unit-item')][" + (i + 1) + "]/text()");
					int loss_count = Integer.parseInt(loss_count_el.getTextContent().trim());
					stmt_unit_home.setInt(i + 3, start_count - loss_count);
					if (out) {
						DomText out_count_el = unit_out.getFirstByXPath("./td[" + (i + 1)
								+ "]/text()");
						int out_count = Integer.parseInt(out_count_el.getTextContent().trim());
						stmt_unit.setInt(i + 3, start_count - loss_count + out_count);
					}
				}
				stmt_unit_home.execute();
				if (out) {
					stmt_unit.execute();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// ===== Spy results =====
		resources: { // Resources
			HtmlElement res = page
					.getFirstByXPath("//table[@id='attack_spy_resources']/tbody/tr/td");
			if (res == null)
				break resources;
			HtmlElement el;
			DomText txtNode;
			int[] resValues = new int[3];

			List<String> ress = new ArrayList<String>();
			for (String r : resources) {
				el = res.getFirstByXPath("./span[contains(@class,'" + r + "')]");
				if (el != null)
					ress.add(r);
			}
			for (String r : ress) {
				txtNode = res.getFirstByXPath("./span[contains(@class,'" + r
						+ "')]/following-sibling::text()[1]");
				String t = txtNode.getTextContent().trim();
				el = txtNode
						.getFirstByXPath("./following-sibling::span[contains(@class,'grey') and preceding-sibling::span[1][contains(@class,'"
								+ r + "')]]");
				if (el != null) {
					txtNode = el.getFirstByXPath("./following-sibling::text()[1]");
					t += txtNode.getTextContent().trim();
				}
				resValues[resources.indexOf(r)] = Integer.parseInt(t);
			}

			String sql = "CALL update_res_timestamp(?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setInt(1, id);
				stmt.setTimestamp(2, new Timestamp(timestamp));
				for (int i = 0; i < 3; i++)
					stmt.setInt(i + 3, resValues[i]);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		building: { // Buildings + production + pop_total
			HtmlInput buildings_input = page
					.getFirstByXPath("//input[@id='attack_spy_building_data']");
			if (buildings_input == null)
				break building;
			String json = buildings_input.getAttribute("value");
			JSONArray data = new JSONArray(json);

			String sql1 = "CALL update_building_timestamp(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String sql2 = "CALL update_prod_timestamp(?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = con.prepareStatement(sql1);
					PreparedStatement stmt_prod = con.prepareStatement(sql2)) {
				stmt.setInt(1, id);
				stmt.setTimestamp(2, new Timestamp(timestamp));
				stmt_prod.setInt(1, id);
				stmt_prod.setTimestamp(2, new Timestamp(timestamp));
				int[] levels = new int[TWEnvironment.buildings.length];

				for (int i = 0; i < data.length(); i++) {
					JSONObject obj = data.getJSONObject(i);
					String building = obj.getString("id");
					String slevel = obj.getString("level");
					int level = Integer.parseInt(slevel);
					levels[buildings.indexOf(building)] = level;
				}

				for (int i = 0; i < TWEnvironment.buildings.length; i++) {
					String b = TWEnvironment.buildings[i];
					int level = levels[i];
					stmt.setInt(i + 3, level);
					int rindex = resources.indexOf(b);

					// production
					if (rindex != -1) {
						double production = bot.getTWEnvironment().getProduction(level);
						stmt_prod.setDouble(rindex + 3, production);
					}

					// pop_total
				}

				stmt.execute();
				stmt_prod.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		con.commit();

	}

	DateTime processTime(String time) {
		String[] parts = time.split("[.: ]+");
		DateTime dt = new DateTime(
				Integer.parseInt(parts[2]) + 2000,
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[3]),
				Integer.parseInt(parts[4]),
				Integer.parseInt(parts[5]),
				(parts.length < 7 ? 0 : Integer.parseInt(parts[6]))
				);
		return dt;
	}

	int lastReport() {
		return state.getInt("LastReport");
	}

	void saveLastReport(int id) {
		state.setProperty("LastReport", id);
	}

	@Override
	public void processVillage(int villageId, PageData page, PageHolder pageHolder) {}


	public static class Factory implements ModuleFactory {

		@Override
		public Module createModule(Bot bot) {
			return new ReportsImporter(bot, bot.stateAt("ReportsImporter"));
		}

	}

}

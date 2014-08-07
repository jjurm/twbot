package com.jjurm.twbot.bot.modules.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
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
import com.jjurm.twbot.tribalwars.TribalWarsException;
import com.jjurm.twbot.tribalwars.Urls;

/**
 * Tool for sending attacks.
 * 
 * @author JJurM
 */
public class AttackSender extends AbstractModule {

	{
		Command attack = new Command() { // attack start_id dest_id spear,sword,axe,...,snob
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				int startId = Integer.parseInt(args[0]);
				int destId = Integer.parseInt(args[1]);
				String[] strTroops = args[2].split(",", 12);
				int[] troops = new int[12];
				for (int i = 0; i < 12; i++) {
					troops[i] = Integer.parseInt(strTroops[i]);
				}
				try (Connection con = bot.getConnection()) {
					HtmlPage place = bot.getTribalWars().get(Urls.getPlaceUrl(startId));
					place = sendAttack(startId, destId, troops, place, con);
					place.cleanUp();
				} catch (FailingHttpStatusCodeException | IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		commands.put("attack", attack);
		commands.put("send", attack);
	}

	/**
	 * Basic constructor.
	 */
	public AttackSender(Bot bot) {
		super(bot);
	}

	/**
	 * Method for sending attack.
	 * 
	 * @param startId ID of the start village
	 * @param destId ID of the target village
	 * @param troops array of the troop counts (must have 12 items)
	 * @param place already loaded page of place in the start village
	 * @return HtmlPage of place, loaded after confirming the attack (or
	 *         <tt>null if something went wrong)
	 * @throws IOException
	 * @throws SQLException
	 * @throws InterruptedExecutionException
	 */
	public HtmlPage sendAttack(int startId, int destId, int[] troops, HtmlPage place, Connection con)
			throws IOException, SQLException {
		int[] coords = bot.getTWEnvironment().getCoords(destId, con);
		return sendAttack(startId, coords[0], coords[1], troops, place, con);
	}

	/**
	 * The base method for sending attack.
	 * 
	 * @param startId ID of the start village
	 * @param destX X-coord of the target village
	 * @param destY Y-coord of the target village
	 * @param troops array of the troop counts (must have 12 items)
	 * @param place already loaded page of place in the start village
	 * @return HtmlPage of place, loaded after confirming the attack (or
	 *         <tt>null</tt> if something went wrong)
	 * @throws IOException
	 * @throws SQLException
	 */
	public HtmlPage sendAttack(int startId, int destX, int destY, int[] troops, HtmlPage place, Connection con)
			throws IOException, SQLException {

		int[] troops_home;
		troops_home = bot.getTWEnvironment().getUnitHome(startId, con);
		boolean ok = true;
		for (int i = 0; i < troops.length; i++) {
			if (troops[i] > troops_home[i]) {
				ok = false;
				break;
			}
		}
		if (!ok)
			return null;

		HtmlInput targetX = place.getFirstByXPath("//input[@id='inputx']");
		targetX.setValueAttribute(String.valueOf(destX));
		HtmlInput targetY = place.getFirstByXPath("//input[@id='inputy']");
		targetY.setValueAttribute(String.valueOf(destY));
		for (int i = 0; i < TWEnvironment.units.length; i++) {
			String unit = TWEnvironment.units[i];
			String xpath = "//input[@id='unit_input_" + unit + "']";
			HtmlInput unitEl = place.getFirstByXPath(xpath);
			if (unitEl == null)
				continue;
			unitEl.setValueAttribute(String.valueOf(troops[i]));
		}

		HtmlElement attackBtn = place.getFirstByXPath("//input[@id='target_attack']");
		bot.dg().sleepRequestDelay();
		HtmlPage confirmPage = attackBtn.click();

		HtmlElement confirmBtn = confirmPage.getFirstByXPath("//input[@id='troop_confirm_go']");
		if (confirmBtn == null) {
			throw new TribalWarsException();
		}
		bot.dg().sleepRequestDelay();
		HtmlPage place2 = confirmBtn.click();

		return place2;

	}

	@Override
	public void processOverviewVillages(PageData overview, PageHolder pageHolder) {}

	@Override
	public void processVillage(int villageId, PageData village, PageHolder pageHolder) {}


	public static class Factory implements ModuleFactory {

		@Override
		public Module createModule(Bot bot) {
			return new AttackSender(bot);
		}

	}

}

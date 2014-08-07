package com.jjurm.twbot.bot.modules.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.InterruptedExecutionException;
import com.jjurm.twbot.bot.PageData;
import com.jjurm.twbot.bot.PageHolder;
import com.jjurm.twbot.bot.modules.AbstractModule;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.bot.modules.ModuleFactory;
import com.jjurm.twbot.bot.modules.ModuleManager;
import com.jjurm.twbot.tribalwars.TWEnvironment;
import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.utils.StringUtils;

/**
 * Module for automatic sending of mass attacks. Sends always as many attacks as
 * possible, with settings specified in the module configuration.
 * 
 * @author JJurM
 */
public class MassAttackSender extends AbstractModule {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Basic constructor
	 */
	public MassAttackSender(Bot bot) {
		super(bot);
	}

	@Override
	public void processOverviewVillages(PageData overview, PageHolder pageHolder) {}

	@Override
	public void processVillage(int villageId, PageData village, PageHolder pageHolder)
			throws InterruptedExecutionException {
		HtmlPage place = null;
		ModuleManager mm = bot.getModuleManager();
		AttackSender attackSender = (AttackSender) mm.getModule(AttackSender.class.getSimpleName());
		UnitDataUpdater unitDataUpdater = (UnitDataUpdater) mm.getModule(UnitDataUpdater.class
				.getSimpleName());

		String query = "SELECT id, target, cycle_max, total_max, "
				+ StringUtils.arrayToString(TWEnvironment.units, ", ", "unit_", "")
				+ " FROM MassAttackSender WHERE active = 1 and start = ?"
				+ " and (cycle_max = -1 or cycle_max > 0) and (total_max = -1 or total_max > 0)"
				+ " ORDER BY priority DESC, id ASC";
		try (Connection con = bot.getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, villageId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("id");
					int target = rs.getInt("target");
					int[] troops = new int[TWEnvironment.units.length];
					for (int i = 0; i < troops.length; i++) {
						troops[i] = rs.getInt("unit_" + TWEnvironment.units[i]);
					}
					int cycle_max = rs.getInt("cycle_max");
					boolean cycle_infinite = cycle_max == -1;
					int total_max = rs.getInt("total_max");
					boolean total_infinite = total_max == -1;

					checkInterruption();
					LOG.debug("MassAttack rule " + id);
					place = bot.getTribalWars().get(Urls.getPlaceUrl(villageId));
					int i = 0;
					try {
						while (place != null && (cycle_infinite || i < cycle_max)
								&& (total_infinite || total_max > 0)) {
							unitDataUpdater.processPlace(villageId, place);
							checkInterruption();
							place = attackSender.sendAttack(villageId, target, troops, place, con);
							i++;
							if (place != null)
								total_max--;
							if (i % 10 == 0)
								LOG.trace(String.valueOf(i));
						}
						LOG.debug("Total: " + i);
					} finally {
						if (!total_infinite) {
							query = "UPDATE MassAttackSender SET total_max = ? WHERE id = ?";
							try (PreparedStatement stmt2 = con.prepareStatement(query)) {
								stmt2.setInt(1, total_max);
								stmt2.setInt(2, id);
								stmt2.executeUpdate();
								con.commit();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}

	}


	public static class Factory implements ModuleFactory {

		@Override
		public Module createModule(Bot bot) {
			return new MassAttackSender(bot);
		}

	}

}

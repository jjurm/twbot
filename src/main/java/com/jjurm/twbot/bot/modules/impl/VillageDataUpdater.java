package com.jjurm.twbot.bot.modules.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.PageData;
import com.jjurm.twbot.bot.PageHolder;
import com.jjurm.twbot.bot.modules.AbstractModule;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.bot.modules.ModuleFactory;
import com.jjurm.twbot.utils.ConversionUtils;

import net.sourceforge.htmlunit.corejs.javascript.NativeObject;

/**
 * Class that updates data of own village to the database.
 * 
 * @author JJurM
 */
public class VillageDataUpdater extends AbstractModule {

	/**
	 * Basic constructor
	 * 
	 * @param bot
	 */
	public VillageDataUpdater(Bot bot) {
		super(bot);
	}
	
	@Override
	public void processOverviewVillages(PageData overview, PageHolder pageHolder) {}
	
	@Override
	public void processVillage(int villageId, PageData page, PageHolder pageHolder) {

		NativeObject game_data = page.getGameData();
		NativeObject village = (NativeObject) game_data.get("village");

		try (Connection con = bot.getConnection()) {
			
			processRes(villageId, village, con);
			processProd(villageId, village, con);
			processPop(villageId, village, con);
			processBuilding(villageId, village, con);

			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	void processRes(int id, NativeObject village, Connection con) { /* res */
		int wood = ConversionUtils.safeInteger(village.get("wood"));
		int stone = ConversionUtils.safeInteger(village.get("stone"));
		int iron = ConversionUtils.safeInteger(village.get("iron"));

		String query = "CALL update_res(?, ?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, id);
			stmt.setInt(2, wood);
			stmt.setInt(3, stone);
			stmt.setInt(4, iron);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void processProd(int id, NativeObject village, Connection con) { /* prod */
		double wood_prod = (Double) village.get("wood_prod");
		double stone_prod = (Double) village.get("stone_prod");
		double iron_prod = (Double) village.get("iron_prod");

		String query = "CALL update_prod(?, ?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, id);
			stmt.setDouble(2, wood_prod);
			stmt.setDouble(3, stone_prod);
			stmt.setDouble(4, iron_prod);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void processPop(int id, NativeObject village, Connection con) { /* pop */
		int pop_total = Integer.parseInt((String) village.get("pop_max"));
		int pop_used = Integer.parseInt((String) village.get("pop"));

		String query = "CALL update_pop(?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, id);
			stmt.setDouble(2, pop_total);
			stmt.setDouble(3, pop_used);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void processBuilding(int id, NativeObject village, Connection con) { /* building */
		NativeObject buildings = (NativeObject) village.get("buildings");

		int building_main = Integer.parseInt((String) buildings.get("main"));
		int building_barracks = Integer.parseInt((String) buildings.get("barracks"));
		int building_stable = Integer.parseInt((String) buildings.get("stable"));
		int building_garage = Integer.parseInt((String) buildings.get("garage"));
		int building_church = Integer.parseInt((String) buildings.get("church"));
		int building_church_f = Integer.parseInt((String) buildings.get("church_f"));
		int building_snob = Integer.parseInt((String) buildings.get("snob"));
		int building_smith = Integer.parseInt((String) buildings.get("smith"));
		int building_place = Integer.parseInt((String) buildings.get("place"));
		int building_statue = Integer.parseInt((String) buildings.get("statue"));
		int building_market = Integer.parseInt((String) buildings.get("market"));
		int building_wood = Integer.parseInt((String) buildings.get("wood"));
		int building_stone = Integer.parseInt((String) buildings.get("stone"));
		int building_iron = Integer.parseInt((String) buildings.get("iron"));
		int building_farm = Integer.parseInt((String) buildings.get("farm"));
		int building_storage = Integer.parseInt((String) buildings.get("storage"));
		int building_hide = Integer.parseInt((String) buildings.get("hide"));
		int building_wall = Integer.parseInt((String) buildings.get("wall"));

		String query = "CALL update_building(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, id);
			stmt.setInt(2, building_main);
			stmt.setInt(3, building_barracks);
			stmt.setInt(4, building_stable);
			stmt.setInt(5, building_garage);
			stmt.setInt(6, building_church);
			stmt.setInt(7, building_church_f);
			stmt.setInt(8, building_snob);
			stmt.setInt(9, building_smith);
			stmt.setInt(10, building_place);
			stmt.setInt(11, building_statue);
			stmt.setInt(12, building_market);
			stmt.setInt(13, building_wood);
			stmt.setInt(14, building_stone);
			stmt.setInt(15, building_iron);
			stmt.setInt(16, building_farm);
			stmt.setInt(17, building_storage);
			stmt.setInt(18, building_hide);
			stmt.setInt(19, building_wall);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public static class Factory implements ModuleFactory {

		@Override
		public Module createModule(Bot bot) {
			return new VillageDataUpdater(bot);
		}
		
	}
	
}

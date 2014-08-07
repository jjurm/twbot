package com.jjurm.twbot.tribalwars;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.jjurm.twbot.system.config.FilesMap;
import com.jjurm.twbot.utils.StringUtils;

import snaq.db.ConnectionPool;

/**
 * Class with some constants and methods related to tribal wars environment.
 * 
 * @author JJurM
 */
public class TWEnvironment {

	/**
	 * Ordered array of buildings
	 */
	public static final String[] buildings = new String[] {
			"main",
			"barracks",
			"stable",
			"garage",
			"church",
			"church_f",
			"snob",
			"smith",
			"place",
			"statue",
			"market",
			"wood",
			"stone",
			"iron",
			"farm",
			"storage",
			"hide",
			"wall"
	};

	/**
	 * Ordered array of units
	 */
	public static final String[] units = new String[] {
			"spear",
			"sword",
			"axe",
			"archer",
			"spy",
			"light",
			"marcher",
			"heavy",
			"ram",
			"catapult",
			"knight",
			"snob"
	};

	public static final String[] resources = new String[] {
			"wood",
			"stone",
			"iron"
	};

	ConnectionPool connectionPool;

	HierarchicalConfiguration world_config;
	HierarchicalConfiguration building_info;
	HierarchicalConfiguration unit_info;

	/**
	 * Basic constructor
	 * 
	 * @param world_config
	 * @param con
	 */
	public TWEnvironment(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;

		world_config = FilesMap.getConfig("world_config");
		building_info = FilesMap.getConfig("building_info");
		unit_info = FilesMap.getConfig("unit_info");
	}

	/**
	 * Calculates production according to the building level.
	 * 
	 * @param buildingLevel
	 * @return
	 */
	public double getProduction(int buildingLevel) {
		double speed = world_config.getDouble("speed");
		double baseProduction = world_config.getDouble("game/base_production") / 3600;
		if (buildingLevel == 0) {
			return speed * 5 / 3600;
		}
		double prod = baseProduction * Math.pow(1.163118, buildingLevel - 1) * speed;
		return prod;
	}

	/**
	 * Return an array of units_home in the village.
	 * 
	 * @param villageId
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public int[] getUnitHome(int villageId, Connection con) throws SQLException {
		String query = "SELECT "
				+ StringUtils.arrayToString(TWEnvironment.units, ", ", "unit_home_", "")
				+ " FROM village_info WHERE id = ?";
		int[] troops = new int[12];
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, villageId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Result row expected");
				}
				for (int i = 0; i < TWEnvironment.units.length; i++) {
					troops[i] = rs.getInt("unit_home_" + TWEnvironment.units[i]);
				}
			}
		}
		return troops;
	}

	/**
	 * Returns an array of X,Y coords of the village specified by ID.
	 * 
	 * @param villageId
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public int[] getCoords(int villageId, Connection con) throws SQLException {
		String query = "SELECT x, y FROM village WHERE id = ?";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, villageId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Result row expected");
				}
				int destX = rs.getInt("x");
				int destY = rs.getInt("y");
				return new int[] {destX, destY};
			}
		}
	}
	
	/**
	 * Returns name of the village.
	 * 
	 * @param villageId
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public String getVillageName(int villageId, Connection con) throws SQLException {
		String query = "SELECT name FROM village WHERE id = ?";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, villageId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Result row expected");
				}
				String name = rs.getString("name");
				return name;
			}
		}
	}

}

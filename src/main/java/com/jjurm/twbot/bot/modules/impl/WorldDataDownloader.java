package com.jjurm.twbot.bot.modules.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.jjurm.twbot.system.Main;
import com.jjurm.twbot.system.config.FilesMap;
import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.utils.FileUtils;

/**
 * Class for retrieving world data from server and saving it to the database.
 * 
 * @author JJurM
 */
public class WorldDataDownloader {
	private static final Logger LOG = LogManager.getLogger();
	
	private WorldDataDownloader() {} // Prevent instantiating

	/**
	 * This will calculate the time difference between now and the time of last
	 * update.
	 * 
	 * @return
	 */
	public static long timePassedSinceUpdate() {
		Configuration config = FilesMap.getConfig("state");
		long lastUpdated = config.getLong("WorldDataDownloader/LastUpdate", 0);
		long time = DateTime.now().getMillis();
		return time - lastUpdated;
	}

	/**
	 * This will overwrite <tt>worlddata_last_update</tt> file with current time
	 */
	static void saveTimeUpdated() {
		Configuration config = FilesMap.getConfig("state");
		config.setProperty("WorldDataDownloader/LastUpdate", DateTime.now().getMillis());
	}

	/**
	 * This will force downloading the data and updating the database with them.
	 * 
	 * @param con <tt>Connection</tt> to the database.
	 */
	public static void update() {
		LOG.info("Starting WorldDataDownloader");
		try (Connection con = Main.getConnectionPool().getConnection(5000)) {
			String base = Urls.getBase();
			String filename;
			File file;
			String query;

			// ===== VILLAGE =====
			filename = "village.txt";
			file = new File("tmp/" + filename);
			downloadAndUnzip(base + "map/" + filename + ".gz", filename + ".gz", filename);
			query = "INSERT INTO `village` (id, name, x, y, player, points, rank) VALUES (?, ?, ?, ?, ?, ?, ?)";
			try (BufferedReader br = new BufferedReader(new FileReader(file));
					Statement stmt = con.createStatement();
					PreparedStatement pstmt = con.prepareStatement(query);) {
				stmt.execute("DELETE FROM `village` WHERE 1");

				String line;
				String[] parts;
				while ((line = br.readLine()) != null) {
					parts = line.split(",");
					pstmt.setInt(1, Integer.parseInt(parts[0]));
					pstmt.setString(2, URLDecoder.decode(parts[1], "UTF-8"));
					pstmt.setInt(3, Integer.parseInt(parts[2]));
					pstmt.setInt(4, Integer.parseInt(parts[3]));
					pstmt.setInt(5, Integer.parseInt(parts[4]));
					pstmt.setInt(6, Integer.parseInt(parts[5]));
					pstmt.setInt(7, Integer.parseInt(parts[6]));
					pstmt.executeUpdate();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			file.delete();

			// ===== PLAYER =====
			filename = "player.txt";
			file = new File("tmp/" + filename);
			downloadAndUnzip(base + "map/" + filename + ".gz", filename + ".gz", filename);
			query = "INSERT INTO `player` (id, name, ally, villages, points, rank) VALUES (?, ?, ?, ?, ?, ?)";
			try (BufferedReader br = new BufferedReader(new FileReader(file));
					Statement stmt = con.createStatement();
					PreparedStatement pstmt = con.prepareStatement(query);) {
				stmt.execute("DELETE FROM `player` WHERE 1");

				String line;
				String[] parts;
				while ((line = br.readLine()) != null) {
					parts = line.split(",");
					pstmt.setInt(1, Integer.parseInt(parts[0]));
					pstmt.setString(2, URLDecoder.decode(parts[1], "UTF-8"));
					pstmt.setInt(3, Integer.parseInt(parts[2]));
					pstmt.setInt(4, Integer.parseInt(parts[3]));
					pstmt.setInt(5, Integer.parseInt(parts[4]));
					pstmt.setInt(6, Integer.parseInt(parts[5]));
					pstmt.executeUpdate();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			file.delete();

			// ===== PLAYER =====
			filename = "ally.txt";
			file = new File("tmp/" + filename);
			downloadAndUnzip(base + "map/" + filename + ".gz", filename + ".gz", filename);
			query = "INSERT INTO `ally` (id, name, tag, members, villages, points, all_points, rank) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			try (BufferedReader br = new BufferedReader(new FileReader(file));
					Statement stmt = con.createStatement();
					PreparedStatement pstmt = con.prepareStatement(query);) {
				stmt.execute("DELETE FROM `ally` WHERE 1");

				String line;
				String[] parts;
				while ((line = br.readLine()) != null) {
					parts = line.split(",");
					pstmt.setInt(1, Integer.parseInt(parts[0]));
					pstmt.setString(2, URLDecoder.decode(parts[1], "UTF-8"));
					pstmt.setString(3, URLDecoder.decode(parts[2], "UTF-8"));
					pstmt.setInt(4, Integer.parseInt(parts[3]));
					pstmt.setInt(5, Integer.parseInt(parts[4]));
					pstmt.setInt(6, Integer.parseInt(parts[5]));
					pstmt.setInt(7, Integer.parseInt(parts[6]));
					pstmt.setInt(8, Integer.parseInt(parts[7]));
					pstmt.executeUpdate();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			file.delete();

			// Committing changes
			con.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		saveTimeUpdated();
		LOG.info("WorldDataDownloader finished");
	}

	static void downloadAndUnzip(String remote_file, String tmp_file, String target_file) {
		// Clear and create tmp file
		File tmp = new File("tmp/" + tmp_file);
		File target = new File("tmp/" + target_file);

		try {
			FileUtils.clearFile(tmp);
			FileUtils.downloadFile(remote_file, tmp);

			FileUtils.clearFile(target);
			FileUtils.gunzip(tmp, target);

			tmp.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

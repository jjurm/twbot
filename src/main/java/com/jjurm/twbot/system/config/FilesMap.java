package com.jjurm.twbot.system.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.utils.FileUtils;

/**
 * Class that holds defined paths to various files or directories.
 * 
 * @author JJurM
 */
public class FilesMap {
	private static final Logger LOG = LogManager.getLogger();
	
	private FilesMap() {} // Prevent instantiation

	private static final Map<String, HierarchicalConfiguration> configs = new HashMap<String, HierarchicalConfiguration>();

	/**
	 * This will initialize the <tt>FilesMap</tt> (download missing config
	 * files, create <tt>XMLConfiguration</tt> objects, etc.)
	 */
	public static void init() {
		HierarchicalConfiguration.setDefaultExpressionEngine(new XPathExpressionEngine());
		XMLConfiguration c;

		try {
			
			c = new XMLConfiguration("config/database.xml");
			configs.put("database", c);

			c = new XMLConfiguration("config/tribalwars.xml");
			configs.put("tribalwars", c);

			c = new XMLConfiguration("config/twbot.xml");
			c.setAutoSave(true);
			c.setReloadingStrategy(new FileChangedReloadingStrategy());
			configs.put("twbot", c);

			File f = new File("server/state.xml");
			if (!f.exists()) {
				FileUtils.copyFile(new File("server/state.default.xml"), f);
			}
			c = new XMLConfiguration(f);
			c.setAutoSave(true);
			FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
			reloadingStrategy.setRefreshDelay(3000);
			c.setReloadingStrategy(reloadingStrategy);
			configs.put("state", c);

		} catch (ConfigurationException | IOException e) {
			e.printStackTrace();
		}
		
		File f = new File("tmp");
		if (!f.exists() || !f.isDirectory()) {
			f.mkdir();
		}
	}
	
	/**
	 * This will return configuration specified by name.
	 * 
	 * @param name
	 * @return
	 */
	public static HierarchicalConfiguration getConfig(String name) {
		return configs.get(name);
	}

	public static void initWorldConfig() {
		XMLConfiguration c;
		try {
			checkWorldConfig();

			c = new XMLConfiguration("config/world/config.xml");
			configs.put("world_config", c);

			c = new XMLConfiguration("config/world/building_info.xml");
			configs.put("building_info", c);

			c = new XMLConfiguration("config/world/unit_info.xml");
			configs.put("unit_info", c);
		} catch (ConfigurationException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will check if all required world config file exists and download
	 * them if needed.
	 * 
	 * @throws IOException
	 */
	static void checkWorldConfig() throws IOException {
		File f;

		f = new File("config/world");
		if (!f.exists() || !f.isDirectory()) {
			f.mkdirs();
		}
		
		f = new File("config/world/config.xml");
		if (!f.exists() || f.length() == 0) {
			FileUtils.clearFile(f);
			FileUtils.downloadFile(Urls.getBase() + "interface.php?func=get_config", f);
			LOG.debug("World config downloaded");
		}

		f = new File("config/world/building_info.xml");
		if (!f.exists() || f.length() == 0) {
			FileUtils.clearFile(f);
			FileUtils.downloadFile(Urls.getBase() + "interface.php?func=get_building_info", f);
			LOG.debug("Buildings info downloaded");
		}

		f = new File("config/world/unit_info.xml");
		if (!f.exists() || f.length() == 0) {
			FileUtils.clearFile(f);
			FileUtils.downloadFile(Urls.getBase() + "interface.php?func=get_unit_info", f);
			LOG.debug("Unit info downloaded");
		}
	}

}

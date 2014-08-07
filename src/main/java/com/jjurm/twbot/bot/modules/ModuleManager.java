package com.jjurm.twbot.bot.modules;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.TribalWars;
import com.jjurm.twbot.tribalwars.World;

/**
 * Class that stores modules configuration with methods for getting list of
 * modules.
 * 
 * @author JJurM
 */
public class ModuleManager {
	private static final Logger LOG = LogManager.getLogger();

	Bot bot;
	HierarchicalConfiguration config;

	/**
	 * Map of modules binded to strings
	 */
	Map<String, Module> modules = new TreeMap<String, Module>();

	/**
	 * Basic constructor. Before use, method
	 * {@link #constructModules(World, Connection, TribalWars)} must be called.
	 * 
	 * @param bot Bot
	 * @param config TWBot config file with <tt>modules</tt> as root element
	 */
	public ModuleManager(Bot bot, HierarchicalConfiguration config) {
		this.bot = bot;
		this.config = config;
	}

	/**
	 * Delegated method from <tt>HashMap</tt>. Returns the module to which the
	 * specified name is mapped, or <tt>null</tt> if this map contains no
	 * mapping for the name.
	 * 
	 * @param name
	 * @return
	 */
	public Module getModule(String name) {
		return modules.get(name);
	}

	/**
	 * This will construct bot modules and store them in the map.
	 */
	public void constructModules() {
		LOG.debug("Constructing modules");
		List<HierarchicalConfiguration> configs = config.configurationsAt("module");
		for (int i = 0; i < configs.size(); i++) {
			ConfigurationNode cnode = configs.get(i).getRootNode();
			try {
				String moduleName = cnode.getAttributes("name").get(0).getValue().toString();
				String factoryName = cnode.getAttributes("factory").get(0).getValue().toString();
				try {
					Class<?> c = Class.forName(factoryName);
					if (!ModuleFactory.class.isAssignableFrom(c)) {
						throw new ClassCastException(
								"Provided class does not extend ModuleFactory");
					}
					ModuleFactory factory = (ModuleFactory) c.newInstance(); // This should never fail, casting possibility is already ensured
					Module module = factory.createModule(bot);
					modules.put(moduleName, module);
				} catch (ClassNotFoundException e) {
					LOG.error("Class '" + factoryName + "' (from '" + moduleName
							+ "' module configuration) not found");
				} catch (ClassCastException e) {
					LOG.error("Class '" + factoryName + "' (from '" + moduleName
							+ "' module configuration) does not extend ModuleFactory",
							new ConfigurationException(e));
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error("Problem instantiating factory '" + factoryName + "' (from '"
							+ moduleName + "' module configuration)", new ConfigurationException(e));
				}
			} catch (IndexOutOfBoundsException e) {
				LOG.error("Missing required attributes in modules configuration (module index "
						+ i + ")", new ConfigurationException(e));
			}
		}
	}

	public List<Module> getEnabledModules() {
		List<Module> enabled = new ArrayList<Module>();
		for (Map.Entry<String, Module> entry : modules.entrySet()) {
			if (config.getBoolean("module[@name='" + entry.getKey() + "']/enabled")) {
				enabled.add(entry.getValue());
			}
		}
		return enabled;
	}

}

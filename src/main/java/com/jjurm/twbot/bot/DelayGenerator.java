package com.jjurm.twbot.bot;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.jjurm.twbot.utils.NumberUtils;

/**
 * Class that helps generating random delays. <tt>DelayGenerator</tt> contains
 * methods for returning generated random delays and for sleeping for specified
 * delay.
 * 
 * @author JJurM
 */
public class DelayGenerator {

	// for debugging
	public static long slept = 0;

	/**
	 * Delays configuration (each element has defined its own minimum and
	 * maximum delay)
	 */
	HierarchicalConfiguration delayDefinitions;

	/**
	 * List of delayConfig (mappings of delay definitions specified by name to
	 * the IDs)
	 */
	HierarchicalConfiguration delayConfig;

	/**
	 * Basic constructor
	 * 
	 * @param config
	 */
	public DelayGenerator(HierarchicalConfiguration delayDefinitions,
			HierarchicalConfiguration delayRefs) {
		this.delayDefinitions = delayDefinitions;
		this.delayConfig = delayRefs;
	}

	// ===== Core methods =====

	public int getDelay(String id) {
		String name = delayConfig.getString("delayRef[@id='" + id + "']/@delayName");
		String base = "delay[@name='" + name + "']";
		HierarchicalConfiguration delay = delayDefinitions.configurationAt(base, true);
		int min = delay.getInt("min");
		int max = delay.getInt("max");
		return NumberUtils.rand(min, max + 1);
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
			slept += ms;
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public void sleep(String name) {
		sleep(getDelay(name));
	}

	// ===== Sleep methods =====

	public void sleepCycleDelay() {
		sleep(getDelay("cycle"));
	}

	public void sleepVillageDelay() {
		sleep(getDelay("village"));
	}

	public void sleepRequestDelay() {
		sleep(getDelay("request"));
	}

}

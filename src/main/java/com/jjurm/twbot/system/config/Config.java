package com.jjurm.twbot.system.config;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import com.gargoylesoftware.htmlunit.BrowserVersion;


/**
 * Core configuration of the program.
 * 
 * @author JJurM
 */
public class Config {
	private Config() {} // Prevent instantiation

	/**
	 * Time zone to use. Leave <tt>null</tt> for default.
	 */
	public static final String timeZone = "Europe/Bratislava";
	
	/**
	 * <tt>BrowserVersion</tt> to use in <tt>TribalWars</tt> web client
	 */
	public static final BrowserVersion browserVersion = BrowserVersion.CHROME;

	/**
	 * Interval between updating world data
	 */
	public static final long worlddataUpdateInterval = 2 * 60 * 60 * 1000;
	
	/**
	 * Port using for socket communication
	 */
	public static final int socketPort = 7070;
	
	/**
	 * Number of reports on report page
	 */
	public static final int reportPageEntryCount = 12;
	
	/*
	 * Cookies that should be stored
	 */
	public static final String[] cookiesToStore = new String[] {"sid"};
	
	/*
	 * Translations of building names (required for parsing spy results in reports)
	 */
	public static final SortedMap<String, String> buildingNames = Collections.unmodifiableSortedMap(new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("main", "Hlavná budova");
			put("barracks", "Kasárne");
			put("stable", "Stajne");
			put("garage", "Dielňa");
			put("church", "Kostol");
			put("church_f", "Katedrála");
			put("snob", "Panský dvor");
			put("smith", "Kováčska dielňa");
			put("place", "Nádvorie");
			put("statue", "Socha");
			put("market", "Trhovisko");
			put("wood", "Drevorubač");
			put("stone", "Jama na ťažbu hliny");
			put("iron", "Železná baňa");
			put("farm", "Sedliacky dvor");
			put("storage", "Sklad");
			put("hide", "Skrýša");
			put("wall", "Opevnenie");
		}
	});

}

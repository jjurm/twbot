package com.jjurm.twbot.system.config;

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

}

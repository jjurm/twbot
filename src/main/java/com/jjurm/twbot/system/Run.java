package com.jjurm.twbot.system;

import java.util.Properties;

/**
 * Class that runs the program. This is the only class that has the
 * <tt>main(String[])</tt> method.
 * 
 * @author JJurM
 */
public class Run {
	private Run() {} // Prevent instantiating this class

	public static void main(String[] args) {
		run();
	}

	public static void run() {

		//test();

		setSystemProperties();

		Main.init();
		Main.start();

	}

	public static void setSystemProperties() {

		Properties p = System.getProperties();
		p.setProperty("log4j.configurationFile", "config/log4j2.xml");

	}

	/**
	 * This adds important shutdown hook to the <tt>Runtime</tt>.
	 */
	static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Main.shutdownActions();
			}
		});
	}

	public static void test() {



		System.exit(0);
	}

}

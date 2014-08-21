package com.jjurm.twbot.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class that runs the program. This is the only class that has the
 * <tt>main(String[])</tt> method.
 * 
 * @author JJurM
 */
public class Run {
	private Run() {} // Prevent instantiating this class

	public static final Properties projectProperties = retrieveProjectProperties();
	public static final String projectName = projectProperties.getProperty("project.name");
	public static final String projectVersion = projectProperties.getProperty("project.version");

	public static void main(String[] args) {

		run();

	}

	public static void run() {

		retrieveProjectProperties();

		setSystemProperties();
		registerShutdownHook();

		Main.init();
		Main.start();

	}

	private static Properties retrieveProjectProperties() {

		Properties p = new Properties();
		try (InputStream in = Run.class.getResourceAsStream("/project.properties")) {
			if (in != null)
				p.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;

	}

	private static void setSystemProperties() {

		Properties p = System.getProperties();
		p.setProperty("log4j.configurationFile", "config/log4j2.xml");

	}

	/**
	 * This adds important shutdown hook to the <tt>Runtime</tt>.
	 */
	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Main.shutdownActions();
			}
		});
	}

}

package com.jjurm.twbot.system;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;

import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.DelayGenerator;
import com.jjurm.twbot.bot.TribalWars;
import com.jjurm.twbot.bot.modules.ModuleManager;
import com.jjurm.twbot.control.Commander;
import com.jjurm.twbot.control.ConsoleReader;
import com.jjurm.twbot.control.SocketListener;
import com.jjurm.twbot.database.DatabaseConnector;
import com.jjurm.twbot.system.config.Config;
import com.jjurm.twbot.system.config.FilesMap;
import com.jjurm.twbot.tribalwars.TWEnvironment;
import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.tribalwars.World;

import snaq.db.ConnectionPool;

/**
 * Main class that controls whole program.
 * 
 * @author JJurM
 */
public class Main {
	private static final Logger LOG = LogManager.getLogger();

	static ConnectionPool connectionPool;
	static World world;
	static TWEnvironment twEnvironment;
	static Commander commander;
	static ConsoleReader consoleReader;
	static SocketListener socketListener;
	static ModuleManager moduleManager;

	static Bot bot;

	private static AtomicBoolean shutdownActionsPerformed = new AtomicBoolean(false);

	public static synchronized void init() {

		// Correct time zone
		setTimeZone();

		LOG.fatal("Initializing [" + Run.projectName + "-" + Run.projectVersion + "]");

		// Initialize FilesMap
		LOG.info("Parsing configuration files");
		FilesMap.init();

		// Create World object
		world = new World(FilesMap.getConfig("tribalwars"));
		Urls.setWorld(world);

		// Disable HtmlUnit logger
		TribalWars.disableHtmlUnitWarnings();

		// Create and test database connection
		LOG.info("Connecting to the database");
		DatabaseConnector.loadConfig(FilesMap.getConfig("database"));
		createConnectionPool();
		
		// Initialize world configuration in FilesMap
		FilesMap.initWorldConfig();

		// Create Tribal Wars Environment
		twEnvironment = new TWEnvironment(connectionPool);

		// Construct bot
		LOG.info("Constructing bot");
		bot = new Bot(world, twEnvironment, connectionPool);

		// Catch input from console
		LOG.info("Running ConsoleReader");
		commander = new Commander(bot);
		consoleReader = new ConsoleReader(commander);
		consoleReader.start();

		// Socket listener
		LOG.info("Opening ServerSocket");
		socketListener = new SocketListener(commander);
		socketListener.start();

		LOG.info("Initialization finished.");
	}

	/**
	 * Starts the program
	 */
	public static synchronized void start() {
		DelayGenerator.sleep(1000);
		LOG.fatal("Starting program");

		bot.start();

		System.gc();
	}

	/**
	 * Stops the program
	 * 
	 * @param force If <tt>true</tt>, the <tt>Bot</tt> notifies the modules to
	 *            stop as soon as possible, otherwise the modules should end
	 *            normally.
	 * @param timeout the maximum time [ms] to wait for the modules to end. Has
	 *            effect only when <tt>force==true</tt>.
	 */
	public static synchronized void stop(boolean force, long timeout) {
		LOG.fatal("Stopping program");

		bot.stop(force, timeout);

		socketListener.stop();
		consoleReader.stop();

		connectionPool.release();

		System.exit(0);
	}

	/**
	 * Set time zone to use set in Config class
	 */
	static void setTimeZone() {
		DateTimeZone timeZone;
		if (Config.timeZone != null)
			try {
				timeZone = DateTimeZone.forID(Config.timeZone);
				DateTimeZone.setDefault(timeZone);
			} catch (IllegalArgumentException e) {
				LOG.warn("Incorrect time zone ID, using default");
			}
	}

	/**
	 * This method will be called from the shutdown hook or the
	 * {@link #stop(boolean, long)} method. This method will do the shutdown
	 * actions only once.
	 */
	public static void shutdownActions() {

		if (!shutdownActionsPerformed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Total sleep time: " + DelayGenerator.slept);
		LOG.fatal("Program stopped!");

	}

	/**
	 * This will create new connection to the database with
	 * <tt>DatabaseConnector</tt>
	 * 
	 * @throws SQLException
	 */
	static void createConnectionPool() {
		try {
			Class<?> c = Class.forName("org.mariadb.jdbc.Driver");
			Driver driver = (Driver) c.newInstance();
			DriverManager.registerDriver(driver);

			ConnectionPool pool = DatabaseConnector.newConnectionPool();
			connectionPool = pool;
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException
				| SQLException e) {
			e.printStackTrace();
			LOG.error("Can't create ConnectionPool, exiting program");
			System.exit(1);
		}
	}


	// Getters

	public static ConnectionPool getConnectionPool() {
		return connectionPool;
	}
	
	public static SocketListener getSocketListener() {
		return socketListener;
	}

}

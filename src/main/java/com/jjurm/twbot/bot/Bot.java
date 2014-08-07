package com.jjurm.twbot.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import snaq.db.ConnectionPool;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.bot.modules.ModuleManager;
import com.jjurm.twbot.bot.modules.impl.WorldDataDownloader;
import com.jjurm.twbot.control.Command;
import com.jjurm.twbot.control.CommandGroup;
import com.jjurm.twbot.system.config.Config;
import com.jjurm.twbot.system.config.FilesMap;
import com.jjurm.twbot.tribalwars.TWEnvironment;
import com.jjurm.twbot.tribalwars.TribalWarsException;
import com.jjurm.twbot.tribalwars.Urls;
import com.jjurm.twbot.tribalwars.World;
import com.jjurm.twbot.utils.Timer;

/**
 * The Tribal Wars Bot class commanding modules.
 * 
 * @author JJurM
 */
public class Bot extends CommandGroup {
	private static final Logger LOG = LogManager.getLogger();

	World world;
	TWEnvironment twEnvironment;
	ConnectionPool connectionPool;
	DelayGenerator delayGenerator;
	TribalWars tribalWars;

	HierarchicalConfiguration config;
	HierarchicalConfiguration state;

	ModuleManager moduleManager;

	ScheduledExecutorService scheduler;
	ScheduledFuture<?> worldDataDownloaderFuture;
	ScheduledFuture<?> botProcessFuture;

	final AtomicBoolean run = new AtomicBoolean(false);
	final AtomicBoolean running = new AtomicBoolean(false);
	final AtomicBoolean lastRun = new AtomicBoolean(false);

	final AtomicBoolean stopping = new AtomicBoolean(false);
	final AtomicBoolean stopped = new AtomicBoolean(false);

	{
		commands.put("pause", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				boolean force = args.length >= 1
						&& ("force".equals(args[0]) || "f".equals(args[0]));
				pause(force);
			}
		});
		commands.put("resume", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				resume();
			}
		});
		commands.put("run", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				run();
			}
		});
		commands.put("once", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				once();
			}
		});
	}

	/**
	 * Basic constructor.
	 * 
	 * @param world World to use
	 * @param twEnvironment TWEnvironment
	 * @param con Connection to the database (DB must be already selected)
	 */
	public Bot(World world, TWEnvironment twEnvironment, ConnectionPool conectionPool) {
		this.world = world;
		this.twEnvironment = twEnvironment;
		this.connectionPool = conectionPool;

		this.config = FilesMap.getConfig("twbot");
		this.state = FilesMap.getConfig("state");

		// Initialize DelayGenerator
		delayGenerator = new DelayGenerator(config.configurationAt("delays"),
				config.configurationAt("bot/delayConfig"));

		int threads = config.getInt("bot/threads");
		scheduler = Executors.newScheduledThreadPool(threads, new ThreadFactory() {
			private AtomicInteger atomic = new AtomicInteger(0);
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "Scheduler" + atomic.getAndIncrement());
				return thread;
			}
		});

		moduleManager = new ModuleManager(this, config.configurationAt("modules", true));
		moduleManager.constructModules();

		try {
			HierarchicalConfiguration twConfig = state.configurationAt("TribalWars", true);
			tribalWars = new TribalWars(world, twConfig, delayGenerator, true);
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			return;
		}

	}
	/**
	 * Starts the bot.
	 */
	public synchronized void start() {

		setSchedules();
		//resume();

	}

	/**
	 * This will resume the bot. It runs the bot process only if it is not
	 * already running. This method is also called when starting the bot.
	 */
	public synchronized void resume() {
		if (stopped.get())
			return;
		lastRun.set(false);
		stopping.set(false);
		if (run.compareAndSet(false, true)) {
			scheduler.execute(botProcessRunnable);
		}
	}

	/**
	 * This will pause the bot, after it can be resumed.
	 * 
	 * @param force If <tt>true</tt>, the bot notifies the modules to stop as
	 *            soon as possible, otherwise the modules should end normally.
	 */
	public synchronized void pause(boolean force) {
		if (stopped.get())
			return;
		if (force)
			stopping.set(true);
		if (run.compareAndSet(true, false) && botProcessFuture != null) {
			botProcessFuture.cancel(false);
		}
	}

	/**
	 * Runs the bot (if not running) and renews the scheduler.
	 */
	public synchronized void run() {
		if (stopped.get())
			return;
		pause(false);
		resume();
	}

	/**
	 * Runs the bot once (if not running) and stops the scheduler.
	 */
	public synchronized void once() {
		if (stopped.get())
			return;
		pause(false);
		lastRun.set(true);
		if (run.compareAndSet(false, true)) {
			scheduler.execute(botProcessRunnable);
		}
	}

	/**
	 * Completely stops the bot. If once the bot is stopped, it can never run
	 * again.
	 * 
	 * @param force If <tt>true</tt>, the bot notifies the modules to stop as
	 *            soon as possible, otherwise the modules should end normally.
	 * @param timeout the maximum time [ms] to wait for the modules to end. Has
	 *            effect only when <tt>force==true</tt>.
	 */
	public synchronized void stop(boolean force, long timeout) {
		scheduler.shutdown();
		pause(force);
		try {
			worldDataDownloaderFuture.cancel(false);
			botProcessFuture.cancel(false);
		} catch (NullPointerException e) {
			// ignore
		}
		scheduler.shutdownNow();

		try {
			if (force)
				scheduler.awaitTermination(timeout, TimeUnit.MILLISECONDS);
			else
				scheduler.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		tribalWars.closeAllWindows();

		stopped.set(true);
	}

	/**
	 * @return <tt>true</tt>, if the bot process may continue
	 */
	public boolean mayContinue() {
		return !stopping.get();
	}

	/**
	 * @throws InterruptedExecutionException if the bot process should be
	 *             stopped
	 */
	public void checkInterruption() throws InterruptedExecutionException {
		if (!mayContinue()) {
			throw new InterruptedExecutionException();
		}
	}

	public World getWorld() {
		return this.world;
	}

	public TWEnvironment getTWEnvironment() {
		return this.twEnvironment;
	}

	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection(1000);
	}

	public DelayGenerator dg() {
		return this.delayGenerator;
	}

	public TribalWars getTribalWars() {
		return this.tribalWars;
	}

	public ModuleManager getModuleManager() {
		return this.moduleManager;
	}

	public HierarchicalConfiguration configAt(String key) {
		return config.configurationAt(key, true);
	}

	public HierarchicalConfiguration stateAt(String key) {
		return state.configurationAt(key, true);
	}

	/**
	 * Bot process Runnable
	 */
	Runnable botProcessRunnable = new Runnable() {
		@Override
		public void run() {
			if (run.compareAndSet(true, !lastRun.get()) && running.compareAndSet(false, true)) {
				try { // Ensure that the scheduler won't stop
					doJob();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (run.get()) // One more check after the bot finishes
						scheduleBot();
					running.set(false);
				}
			}
		}
	};

	/**
	 * This will set schedules for some additional modules that run in different
	 * intervals. Only needed to run this method once at start.
	 */
	void setSchedules() {

		// WorldDataDownloader
		worldDataDownloaderFuture = scheduler.scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						try { // Ensure that the scheduler won't stop
							WorldDataDownloader.update();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				},
				Math.max(Config.worlddataUpdateInterval
						- WorldDataDownloader.timePassedSinceUpdate(), 100),
				Config.worlddataUpdateInterval,
				TimeUnit.MILLISECONDS
				);

	}

	/**
	 * This will schedule one next run of the the Bot. Needed to run once at
	 * start, then it will automatically schedule itself.
	 */
	void scheduleBot() {
		long pause = dg().getDelay("cycle");
		if (botProcessFuture != null)
			botProcessFuture.cancel(false);
		botProcessFuture = scheduler.schedule(botProcessRunnable, pause, TimeUnit.MILLISECONDS);
		LOG.info("Bot scheduled to run at "
				+ Timer.getTimeString(DateTime.now().plus(pause)));
	}

	/**
	 * This tells the Bot to do it's job - go through villages and call the
	 * modules.
	 */
	public void doJob() {
		LOG.info("Starting Bot process");
		PageHolder holder = null;
		try (Connection con = getConnection()) {
			List<Module> modules = moduleManager.getEnabledModules();

			holder = new PageHolder(tribalWars);

			PageData pageData = holder.getPageData(Urls.getOverviewVillagesUrl());

			checkInterruption();
			LOG.debug("Running modules on player");
			for (Module module : modules) {
				checkInterruption();
				try {
					module.processOverviewVillages(pageData, holder);
				} catch (TribalWarsException e) {
					e.printStackTrace();
				}
			}

			// get villages
			String xpath1 = "//*[@id='production_table']//tr[contains(@class,'row_a') or contains(@class,'row_b')]";
			@SuppressWarnings("unchecked")
			List<HtmlElement> rows = (List<HtmlElement>) pageData.getPage().getByXPath(xpath1);

			for (HtmlElement row : rows) {
				dg().sleepVillageDelay();

				// get village id
				HtmlElement a = row.getFirstByXPath(".//a[not(contains(@class,'rename-icon'))]");
				String url = a.getAttribute("href");
				Pattern pattern = Pattern.compile("village=([0-9]+)&");
				Matcher matcher = pattern.matcher(url);
				matcher.find();
				String match = matcher.group(1);
				int villageId = Integer.parseInt(match);

				checkInterruption();
				LOG.debug("Village: " + villageId + " ("
						+ twEnvironment.getVillageName(villageId, con) + ")");

				// open village page
				PageData village = holder.add(a.<HtmlPage> click());

				for (Module module : modules) {
					LOG.trace("Running " + module.getClass().getSimpleName());
					checkInterruption();
					try {
						module.processVillage(villageId, village, holder);
					} catch (TribalWarsException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		} catch (InterruptedExecutionException e) {
			LOG.warn("Module execution was interrupted");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (holder != null)
				holder.cleanUp();
			tribalWars.closeAllWindows();
			System.gc();
		}
		LOG.info("Bot process finished.");
	}

}

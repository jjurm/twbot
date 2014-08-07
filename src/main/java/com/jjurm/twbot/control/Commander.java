package com.jjurm.twbot.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.DelayGenerator;
import com.jjurm.twbot.bot.modules.Module;
import com.jjurm.twbot.system.Main;
import com.jjurm.twbot.system.config.FilesMap;

/**
 * Class for communication with clients and commanding system.
 * 
 * @author JJurM
 */
public class Commander {
	private static final Logger LOG = LogManager.getLogger();

	public static final String OK_CHARACTER = ".";
	public static final String WARN_CHARACTER = "!";
	public static final String ERR_CHARACTER = "?";

	Bot bot;

	Map<String, Command> commands = new HashMap<String, Command>();

	/**
	 * Basic constructor
	 * 
	 * @param bot
	 */
	public Commander(Bot bot) {
		this.bot = bot;

		createCommands();
	}

	void createCommands() {
		commands.put("close", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw)
					throws StreamCloseRequest {
				throw new StreamCloseRequest();
			}
		});
		commands.put("stop", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				boolean force = false;
				long timeout = 2 * 60 * 1000;

				if (args.length >= 1 && "force".equals(args[0])) {
					force = true;
					if (args.length >= 2) {
						try {
							timeout = Long.parseLong(args[1]);
						} catch (NumberFormatException e) {
							pw.println(WARN_CHARACTER);
							// and leave the timeout default
						}
					}
				}

				Main.stop(force, timeout);
			}
		});
		commands.put("ready", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				pw.println("ok");
			}
		});
		commands.put("sleep", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				try {
					int ms = Integer.parseInt(args[0]);
					DelayGenerator.sleep(ms);
				} catch (NumberFormatException e) { // the input is NaN
					pw.println("input must be integer");
				}
			}
		});
		commands.put("cookie", new CommandGroup() {
			{
				Command get = new Command() {
					@Override
					public void process(String[] args, BufferedReader br, PrintWriter pw) {
						String sid = bot.getTribalWars().getSid();
						pw.println(sid);
					}
				};
				commands.put("", get);
				commands.put("get", get);
				commands.put("set", new Command() {
					@Override
					public void process(String[] args, BufferedReader br, PrintWriter pw) {
						String sid = args.length >= 1 ? args[0] : "";
						bot.getTribalWars().setSid(sid);
					}
				});
			}
		});
		commands.put("modules", new CommandGroup() {
			void changeState(String[] args, boolean state) {
				if (args.length == 0)
					return;
				String name = args[0];
				HierarchicalConfiguration config = FilesMap.getConfig("twbot");
				if (name.equals("all") || name.equals("*")) {
					List<HierarchicalConfiguration> configs = config
							.configurationsAt("modules//module");
					for (HierarchicalConfiguration c : configs) {
						String n = c.getRootNode().getAttributes("name").get(0).getValue()
								.toString();
						changeModuleState(config, n, state);
					}
				} else {
					changeModuleState(config, name, state);
				}
			}
			void changeModuleState(HierarchicalConfiguration config, String name, boolean state) {
				try {
					config.setProperty("modules//module[@name='" + name + "']/enabled", state);
				} catch (JXPathInvalidSyntaxException e) {
					// no module found
				}
			}

			{
				commands.put("enable", new Command() {
					@Override
					public void process(String[] args, BufferedReader br, PrintWriter pw) {
						changeState(args, true);
					}
				});
				commands.put("disable", new Command() {
					@Override
					public void process(String[] args, BufferedReader br, PrintWriter pw) {
						changeState(args, false);
					}
				});
			}
		});
		commands.put("bot", bot);
		commands.put("module", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw)
					throws StreamCloseRequest {
				if (args.length == 0) {
					return;
				}
				String moduleName = args[0];
				Module module = bot.getModuleManager().getModule(moduleName);
				if (module == null) {
					return;
				}
				String[] args2 = Arrays.copyOfRange(args, 1, args.length);
				module.process(args2, br, pw);
			}
		});
		commands.put("login", new Command() {
			@Override
			public void process(String[] args, BufferedReader br, PrintWriter pw) {
				try {
					bot.getTribalWars().login();
				} catch (FailingHttpStatusCodeException | IOException e) {
					e.printStackTrace(pw);
				}
			}
		});
	}

	/**
	 * This will read command from <tt>InputStream</tt> and perform needed
	 * actions. Another messages may be sent and received with
	 * <tt>OutputStream</tt> and <tt>InputStream</tt>. This streams should not
	 * be in use by other threads while running this method.
	 * 
	 * @param br Input stream
	 * @param pw Output stream (PrintWriter must be set to auto-flush)
	 * @throws StreamCloseRequest
	 */
	public void process(BufferedReader br, PrintWriter pw) throws StreamCloseRequest {
		try {

			String line = br.readLine();
			if (line == null) {
				throw new StreamCloseRequest();
			}
			String[] parts = line.trim().split("\\s+");
			String commandName = parts[0].trim();

			Command command = commands.get(commandName);
			if (commandName.length() >= 1 && command != null) {
				String[] args = Arrays.copyOfRange(parts, 1, parts.length);
				command.process(args, br, pw);
			} else {
				pw.println(ERR_CHARACTER);
			}
			pw.println(OK_CHARACTER);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

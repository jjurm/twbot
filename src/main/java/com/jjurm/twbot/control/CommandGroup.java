package com.jjurm.twbot.control;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Command class that can have multiple sub-commands.
 * 
 * @author JJurM
 */
public class CommandGroup implements Command {

	protected Map<String, Command> commands = new HashMap<String, Command>();

	@Override
	public void process(String[] args, BufferedReader br, PrintWriter pw)
			throws StreamCloseRequest {
		try {
			String commandName;
			String[] args2;
			if (args.length >= 1) {
				commandName = args[0];
				args2 = Arrays.copyOfRange(args, 1, args.length);
			} else {
				commandName = "";
				args2 = new String[0];
			}

			Command command = commands.get(commandName);
			if (command == null) {
				pw.println(Commander.ERR_CHARACTER);
			} else {
				command.process(args2, br, pw);
			}
		} catch (IndexOutOfBoundsException e) {
			// ignore
		}
	}

}

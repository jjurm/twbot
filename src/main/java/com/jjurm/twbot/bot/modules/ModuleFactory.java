package com.jjurm.twbot.bot.modules;

import com.jjurm.twbot.bot.Bot;

/**
 * Interface representing a factory of modules
 * 
 * @author JJurM
 */
public interface ModuleFactory {
	
	/**
	 * Method for creating a module
	 * 
	 * @param bot <tt>Bot</tt> to bind the module to
	 * @return
	 */
	public Module createModule(Bot bot);
	
}

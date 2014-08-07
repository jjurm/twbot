package com.jjurm.twbot.bot.modules;

import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.InterruptedExecutionException;
import com.jjurm.twbot.control.CommandGroup;

/**
 * Abstract class for modules to be simply extended.
 * 
 * @author JJurM
 */
public abstract class AbstractModule extends CommandGroup implements Module {

	protected Bot bot;

	public AbstractModule(Bot bot) {
		this.bot = bot;
	}

	@Deprecated
	@Override
	public String name() {
		return this.getClass().getSimpleName();
	}

	/**
	 * This method will check if the module can continue running and throw an
	 * <tt>InterruptedExecutionException</tt> if not.
	 * 
	 * @throws InterruptedExecutionException
	 */
	public void checkInterruption() throws InterruptedExecutionException {
		bot.checkInterruption();
	}

}

package com.jjurm.twbot.bot.modules;

import com.jjurm.twbot.bot.Bot;
import com.jjurm.twbot.bot.InterruptedExecutionException;
import com.jjurm.twbot.bot.PageData;
import com.jjurm.twbot.bot.PageHolder;
import com.jjurm.twbot.control.Command;

/**
 * Interface representing bot module.
 * <p>
 * Each module can re-throw <tt>InterruptedExecutionException</tt>, which is
 * supposed to be instantiated only in {@link Bot#checkInterruption()}.
 * 
 * @author JJurM
 * 
 * @see InterruptedExecutionException
 */
public interface Module extends Command {

	/**
	 * @return Module name
	 * 
	 * @deprecated module name is specified in configuration file, using this
	 *             method is discouraged and may lead to potential complications
	 */
	@Deprecated
	public String name();

	/**
	 * Method that processes the overview page of the villages. It is enough to
	 * be run only once during data update process.
	 * 
	 * @param overview
	 * @param pageHolder
	 * @throws InterruptedExecutionException
	 */
	public void processOverviewVillages(PageData overview, PageHolder pageHolder)
			throws InterruptedExecutionException;

	/**
	 * Method that processes a village page. It is supposed to be run in every
	 * village that user owns.
	 * 
	 * @param villageId
	 * @param village
	 * @param pageHolder
	 * @throws InterruptedExecutionException
	 */
	public void processVillage(int villageId, PageData village, PageHolder pageHolder)
			throws InterruptedExecutionException;

}

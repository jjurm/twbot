package com.jjurm.twbot.bot;

/**
 * Exception thrown usually by modules when the bot has been marked to stop.
 * Module can check this state and throw this exception if the bot is being
 * stopped.
 * 
 * @author JJurM
 */
public class InterruptedExecutionException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Basic constructor with default visibility to disallow instantiating this
	 * exception by outer classes. This is supposed to be called exclusively in
	 * <tt>Bot</tt>.
	 */
	InterruptedExecutionException() {

	}

}

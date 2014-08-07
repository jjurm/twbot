package com.jjurm.twbot.utils;

/**
 * Class that contains various methods to manipulate with numbers.
 * 
 * @author JJurM
 */
public class NumberUtils {
	private NumberUtils() {} // Prevent instantiating

	/**
	 * Always returns randomly <tt>0</tt> or <tt>1</tt>.
	 * 
	 * @return <tt>0</tt> or <tt>1</tt>
	 */
	public static int rand() {
		return (int) Math.round(Math.random());
	}

	/**
	 * Returns random integer from <tt>0</tt> to <tt>a-1</tt>
	 * 
	 * @param a
	 * @return
	 */
	public static int rand(int a) {
		return (int) Math.floor(Math.random() * a);
	}

	/**
	 * Returns random integer from <tt>a</tt> to <tt>b-1</tt>.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int rand(int a, int b) {
		return rand(b - a) + a;
	}
}

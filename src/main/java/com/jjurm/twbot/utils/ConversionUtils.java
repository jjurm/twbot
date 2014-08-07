package com.jjurm.twbot.utils;

/**
 * This class helps with converting and casting basic types.
 * 
 * @author JJurM
 */
public class ConversionUtils {
	private ConversionUtils() {}

	/**
	 * Casts <tt>Object</tt> to <tt>Double</tt>, then after rounding follows
	 * conversion to <tt>int</tt>.
	 * 
	 * @param object <tt>Object</tt>
	 * @return <tt>int</tt>
	 */
	public static int safeInteger(Object object) {
		return (int) Math.round((Double) object);
	}

}

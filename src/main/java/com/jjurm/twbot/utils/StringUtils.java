package com.jjurm.twbot.utils;

public class StringUtils {
	private StringUtils() {} // Prevent instantiating

	/**
	 * Adds string to the left of the base string.
	 * 
	 * @param base Base string
	 * @param add String to add to the left
	 * @param length Required length
	 * @return String
	 */
	public static String addLeft(String base, String add, int length) {
		String s = base;
		if (add.length() < 1) {
			return base;
		}
		while (s.length() < length) {
			s = add + s;
		}
		return s;
	}

	/**
	 * Turns a string array to a string, each item in the array prepended by a
	 * <tt>prefix</tt> and appended by a <tt>suffix</tt>, items joined together
	 * with a <tt>glue</tt>.
	 * 
	 * @param array
	 * @param glue
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static String arrayToString(String[] array, String glue, String prefix, String suffix) {
		String s = "";
		for (int i = 0; i < array.length; i++) {
			s += prefix + array[i] + suffix + (i == array.length - 1 ? "" : glue);
		}
		return s;
	}

}

package com.jjurm.twbot.utils;

import org.joda.time.DateTime;

/**
 * Basic class for manipulating with time.
 * 
 * @author JJurM
 */
public class Timer {
	public Timer() {} // Prevent instantiating

	/**
	 * Returns string with time in following format:<br>
	 * 
	 * <pre>
	 * 2014-05-13 12:43:25.750
	 * </pre>
	 * 
	 * @return
	 */
	public static String getTimeString() {
		return getTimeString(DateTime.now());
	}

	/**
	 * Returns string with time in following format:<br>
	 * 
	 * <pre>
	 * 2014/05/13 12:43:25.750
	 * </pre>
	 * 
	 * @param dt <tt>DateTime</tt> to use
	 * @return
	 */
	public static String getTimeString(DateTime dt) {
		String s = "";
		s += dt.getYear() + "/" + StringUtils.addLeft(String.valueOf(dt.getMonthOfYear()), "0", 2)
				+ "/" + dt.getDayOfMonth();
		s += " ";
		s += StringUtils.addLeft(String.valueOf(dt.getHourOfDay()), "0", 2) + ":"
				+ StringUtils.addLeft(String.valueOf(dt.getMinuteOfHour()), "0", 2) + ":"
				+ StringUtils.addLeft(String.valueOf(dt.getSecondOfMinute()), "0", 2);
		s += "." + StringUtils.addLeft(String.valueOf(dt.getMillisOfSecond()), "0", 3);
		return s;
	}

}

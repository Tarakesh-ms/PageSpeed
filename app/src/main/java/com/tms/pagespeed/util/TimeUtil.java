/**
 * 
 */
package com.tms.pagespeed.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Tarak
 * @since 1.0
 *
 */
public class TimeUtil {

	/**
	 * @return current date in GMT format
	 */
	public static String getCurrentDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(new Date());
	}
}

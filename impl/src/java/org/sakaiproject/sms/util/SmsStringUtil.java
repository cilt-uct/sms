package org.sakaiproject.sms.util;

public class SmsStringUtil {
	
	/**
	 * Converts all Strings in array to Uppercase
	 * 
	 * @param array
	 * @return {@link String} array with all set to uppercase
	 */
	public static String[] upperCaseArray(String[] array) {
		for (String str : array) {
			if (str != null) {
				str = str.toUpperCase();
			}
		}
		return array;
	}
	
	/**
	 * Find value in String array (case insensitive)
	 * 
	 * @param array
	 * @param str
	 * @return
	 */
	public static String findInArray(String[] array, String str) {
		if (str == null || array == null || array.length == 0) {
			return null;
		} else {
			for (int i=0;i<array.length;i++) {
				if (str.equalsIgnoreCase(array[i])) {
					return array[i];
				}
			}
		}
		return null;
	}
}

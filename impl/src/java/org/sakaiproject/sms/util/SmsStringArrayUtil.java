/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.util;

public class SmsStringArrayUtil {
	
	/**
	 * Converts all Strings in array to Uppercase
	 * 
	 * @param array
	 * @return {@link String} array with all set to uppercase
	 */
	public static String[] upperCaseArray(String[] array) {
		String[] newArray = new String[array.length];
		
		int i=0;
		for (String str : array) {
			if (str != null) {
				str = str.toUpperCase();
				newArray[i] = str;
			}
			i++;
		}
		return newArray;
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
	
	/**
	 * Return copy of array with new size
	 * Can be changed to Array.copyOf in jdk 1.6
	 * 
	 * @param array
	 * @param newSize
	 * @return
	 */
	public static String[] copyOf(String[] array, int newSize) {
		String[] newArray = new String[newSize]; 
		for (int i=0;i < newSize; i++) {
			if (i < array.length) {
				newArray[i] = array[i];
			} else {
				newArray[i] = null;
			}
		}
		return newArray;
	}
}

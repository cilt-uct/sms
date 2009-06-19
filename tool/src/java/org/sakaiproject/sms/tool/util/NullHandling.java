/***********************************************************************************
 * NullHandling.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sms.tool.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NullHandling {

	
	public static String safeToString(String value){
		
		if(value == null)
			return "N/A";
		
		return value;
	}
	
	public static String safeToString(Number integer){
		
		if(integer == null)
			return "N/A";
		
		return integer.toString();
	}
	
	public static String safeToString(Timestamp timestamp){
		
		if(timestamp == null)
			return "N/A";
		
		return timestamp.toString();
	}
	
	public static boolean safeDateCheck(Date from, Date to){
		
		if(from == null || to == null)
			return false;
		
		if(from.compareTo(to) > 0)
			return false;
		
		return true;
	}

	public static String safeToStringFormated(Date dateProcessed) {
		if(dateProcessed == null)
			return "N/A";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/M/dd kk:mm");
		
		return format.format(dateProcessed);
	}

	public static String safeTruncatedToString(String value, int numberOfCharacters) {

		if(value == null)
			return "N/A";

		if(value.length() <= numberOfCharacters)
			return value;
		
		return value.substring(0, numberOfCharacters) + "...";
	}

	
}

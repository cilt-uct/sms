package org.sakaiproject.sms.tool.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SakaiDateFormat {

	SimpleDateFormat sakaiDateFormat;
	
	//TODO this should get the sakai date format from a properties file
	public SakaiDateFormat() {
		super();
		sakaiDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	}

	public SimpleDateFormat getSakaiDateFormat(){
		return sakaiDateFormat;
	}

	public String formatDate(Date date) {

		if(date == null)
			return "";
		
		return sakaiDateFormat.format(date);
	}	
}
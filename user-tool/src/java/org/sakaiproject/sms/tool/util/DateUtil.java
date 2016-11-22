package org.sakaiproject.sms.tool.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.sakaiproject.sms.logic.external.ExternalLogic;

import uk.org.ponder.localeutil.LocaleGetter;

public class DateUtil {	

	private static final String ISO8601_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(ISO8601_DATE_FORMAT_STRING);

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private LocaleGetter localegetter;
	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}

	public String formatDate(Date date) {
		if ( date == null ){
			return "";
		}else{
			// fix for broken en_ZA locale in JRE http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6488119
			Locale M_locale = null;
			String langLoc[] = localegetter.get().toString().split("_");
			if ( langLoc.length >= 2 ) {
				if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1]))
					M_locale = new Locale("en", "GB");
				else
					M_locale = new Locale(langLoc[0], langLoc[1]);
			} else{
				M_locale = new Locale(langLoc[0]);
			}

			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.SHORT, M_locale);
			TimeZone tz = externalLogic.getLocalTimeZone();
			df.setTimeZone(tz);
		
			return df.format(date);
		}
	}	
	
	public String getISO8601FormattedDateStr(Date date) {
		if(date == null){
	        return DATE_FORMAT.format(new Date());
		}
        return DATE_FORMAT.format(date);
    }
}
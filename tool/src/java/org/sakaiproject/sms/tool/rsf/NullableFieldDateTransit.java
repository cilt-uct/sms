/***********************************************************************************
 * NullableFieldDateTransit.java
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
package org.sakaiproject.sms.tool.rsf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.sakaiproject.sms.tool.util.SakaiDateFormat;

import uk.org.ponder.dateutil.DateUtil;
import uk.org.ponder.dateutil.FieldDateTransit;
import uk.org.ponder.dateutil.LocalSDF;
import uk.org.ponder.localeutil.LocaleHolder;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageException;

/**
 * A copy of RSF's {@link uk.org.ponder.dateutil.StandardFieldDateTransit}
 * Needed to change private parseImpl(Operation op) to allow null date values
 * 
 */
public class NullableFieldDateTransit extends LocaleHolder implements
		FieldDateTransit {

	private static class Operation {
		public String text;
		public SimpleDateFormat format;
		public int fields;

		public Operation(String text, SimpleDateFormat format, int fields) {
			this.text = text;
			this.format = format;
			this.fields = fields;
		}
	}

	public void setShortformat(SakaiDateFormat dateFormat) {
		this.shortformat = dateFormat.getSakaiDateFormat();
	}

	private Date date;
	private SimpleDateFormat shortformat;
	private SimpleDateFormat medformat;
	private SimpleDateFormat longformat;
	private SimpleDateFormat timeformat;
	private DateFormat longtimeformat;
	private DateFormat iso8601tz;
	private DateFormat iso8601notz;

	// private DateFormat breakformat;
	private TimeZone timezone = TimeZone.getDefault();

	private boolean isvalid = true;
	private String invalidDateKey = FieldDateTransit.INVALID_DATE_KEY;

	private String invalidTimeKey;

	@SuppressWarnings("unchecked")
	private final List operations = new ArrayList();

	public Date getDate() {
		triggerParse();
		return isvalid ? date : null;
	}

	/** Render an ISO8601-formatted value, including timezone information **/
	public String getISO8601TZ() {
		triggerParse();
		return isvalid ? iso8601tz.format(date) : null;
	}

	public String getLong() {
		triggerParse();
		return isvalid ? longformat.format(date) : null;
	}

	public String getLongTime() {
		triggerParse();
		return isvalid ? longtimeformat.format(date) : null;
	}

	public String getMedium() {
		triggerParse();
		return isvalid ? medformat.format(date) : null;
	}

	public String getShort() {
		triggerParse();
		return isvalid ? shortformat.format(date) : null;
	}

	public String getShortFormat() {
		return shortformat.toLocalizedPattern();
	}

	public String getTime() {
		triggerParse();
		return isvalid ? timeformat.format(date) : null;
	}

	public String getTimeFormat() {
		return timeformat.toLocalizedPattern();
	}

	public int getTZOffset() {
		return timezone.getOffset(date.getTime());
	}

	public void init() {

		Locale locale = getLocale();
		// TODO: Think about sharing these, see LocalSDF for construction costs
		if (shortformat == null) {
			shortformat = (SimpleDateFormat) DateFormat.getDateInstance(
					DateFormat.SHORT, locale);
		}
		shortformat.setLenient(false);
		medformat = (SimpleDateFormat) DateFormat.getDateInstance(
				DateFormat.MEDIUM, locale);
		longformat = (SimpleDateFormat) DateFormat.getDateInstance(
				DateFormat.LONG, locale);
		timeformat = (SimpleDateFormat) DateFormat.getTimeInstance(
				DateFormat.SHORT, locale);
		timeformat.setTimeZone(timezone);
		longtimeformat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
		longtimeformat.setTimeZone(timezone);
		iso8601tz = new SimpleDateFormat(LocalSDF.W3C_DATE_TZ);
		iso8601notz = new SimpleDateFormat(LocalSDF.W3C_DATE_NOTZ);
		iso8601notz.setTimeZone(timezone);
		// breakformat = new SimpleDateFormat(LocalSDF.BREAKER_DATE, locale);
		// do not use new Date(0) because of TZ insanity!!!
		// date = LocalSDF.breakformat.parse("01012000000000");
		date = new Date();
	}

	@SuppressWarnings("unchecked")
	private void parse(SimpleDateFormat format, String datestring,
			int fieldsCode) {
		operations.add(new Operation(datestring, format, fieldsCode));
	}

	private void parseImpl(Operation op) {
		try {
			// To skip applying of fields if date field is empty or null
			if (op.text == null || "".equals(op.text.trim())) {
				date = null;
				isvalid = false;
				return;
			}

			Date ydate = op.format.parse(op.text);
			DateUtil.applyFields(date, ydate, op.fields);
		} catch (Exception e) {
			isvalid = false;
			String key = op.fields == DateUtil.DATE_FIELDS ? invalidDateKey
					: (invalidTimeKey == null ? invalidDateKey : invalidTimeKey);
			throw new TargettedMessageException(new TargettedMessage(key,
					new Object[] { date, op.format.toPattern() }));
		}
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setInvalidDateKey(String invalidDateKey) {
		this.invalidDateKey = invalidDateKey;
	}

	public void setInvalidTimeKey(String invalidTimeKey) {
		this.invalidTimeKey = invalidTimeKey;
	}

	/**
	 * Set an ISO 8601-formatted value for which the timezone is to be firmly
	 * IGNORED.
	 */
	public void setISO8601TZ(String isoform) throws ParseException {
		date = iso8601notz.parse(isoform);
	}

	public void setLong(String longform) {
		parse(longformat, longform, DateUtil.DATE_FIELDS);
	}

	public void setMedium(String medform) {
		parse(medformat, medform, DateUtil.DATE_FIELDS);
	}

	public void setShort(String shortform) {
		parse(shortformat, shortform, DateUtil.DATE_FIELDS);
	}

	public void setTime(String time) {
		parse(timeformat, time, DateUtil.TIME_FIELDS);
	}

	public void setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
	}

	private void triggerParse() {
		for (int i = 0; i < operations.size(); ++i) {
			parseImpl((Operation) operations.get(i));
		}
		operations.clear();
	}

}

/**
 * A Dummy bean to allow RSF to evolve date input fields.
 */
package org.sakaiproject.sms.tool.beans;

import java.util.Date;

public class DummyBean {
	private Date smsDatesScheduleDate;
	public Date getSmsDatesScheduleDate() {
		return smsDatesScheduleDate;
	}

	public void setSmsDatesScheduleDate(Date smsDatesScheduleDate) {
		this.smsDatesScheduleDate = smsDatesScheduleDate;
	}

	private String dummyField;

public String getDummyField() {
	return dummyField;
}

public void setDummyField(String dummyField) {
	this.dummyField = dummyField;
}
}

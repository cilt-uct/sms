/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.sms.tool.beans;

import java.util.Date;
/**
 * A Dummy bean to allow RSF to evolve date input fields.
 */
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

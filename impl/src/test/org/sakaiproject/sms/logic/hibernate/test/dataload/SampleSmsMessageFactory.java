/***********************************************************************************
 * SampleSmsMessageFactory.java
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
package org.sakaiproject.sms.logic.hibernate.test.dataload;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;

public class SampleSmsMessageFactory implements Listable {

	private List<SmsMessage> smsMessages;
	private final RandomUtils randomUtils = new RandomUtils();
	private static int theYear = 2009;

	public SampleSmsMessageFactory() {
		creatSampleSmsMessages();
	}

	public Object getElementAt(int i) {
		return getTestSmsMessage(i);
	}

	public void refreshList() {
		creatSampleSmsMessages();

	}

	private void creatSampleSmsMessages() {
		smsMessages = new ArrayList<SmsMessage>();

		SmsMessage message1 = new SmsMessage("083468221");
		message1.setSakaiUserId("SNK111");
		message1.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		message1.setDateDelivered(randomUtils.getBoundRandomDate(theYear));
		smsMessages.add(message1);
		SmsMessage message2 = new SmsMessage("0823456789");
		message2.setSakaiUserId("BIT111");
		message2.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		message2.setDateDelivered(randomUtils.getBoundRandomDate(theYear));
		smsMessages.add(message2);
		SmsMessage message3 = new SmsMessage("08255552222");
		message3.setSakaiUserId("BAT111");
		message3.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		message3.setDateDelivered(randomUtils.getBoundRandomDate(theYear));
		smsMessages.add(message3);
		SmsMessage message4 = new SmsMessage("08266661122");
		message4.setSakaiUserId("RAT111");
		message4.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		message4.setDateDelivered(randomUtils.getBoundRandomDate(theYear));
		smsMessages.add(message4);
		SmsMessage message5 = new SmsMessage("084444667");
		message5.setSakaiUserId("COW111");
		message5.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		message5.setDateDelivered(randomUtils.getBoundRandomDate(theYear));
		smsMessages.add(message5);
	}

	public List<SmsMessage> getAllTestSmsMessages() {
		return smsMessages;
	}

	public SmsMessage getTestSmsMessage(int index) {

		if (index >= smsMessages.size())
			throw new RuntimeException("The specified index is too high");

		return smsMessages.get(index);
	}

	public int getTotalSmsMessages() {
		return smsMessages.size();
	}

}

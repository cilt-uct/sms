/***********************************************************************************
 * SampleSmsTransactionFactory.java
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

import org.sakaiproject.sms.model.SmsTransaction;

public class SampleSmsTransactionFactory implements Listable {
	private static int theYear = 2009;
	private List<SmsTransaction> smsTransactionList;
	private final RandomUtils randomUtils = new RandomUtils();

	public SampleSmsTransactionFactory() {
		createSampleSmsTransactions();
	}

	public Object getElementAt(int i) {
		return getSmsTransaction(i);
	}

	public void refreshList() {
		createSampleSmsTransactions();
	}

	private void createSampleSmsTransactions() {
		smsTransactionList = new ArrayList<SmsTransaction>();

		SmsTransaction smsTransaction1 = new SmsTransaction(randomUtils
				.getRandomInteger(1000), "JOE001", randomUtils
				.getRandomInteger(1000), randomUtils
				.getBoundRandomDate(theYear), "TC");
		smsTransactionList.add(smsTransaction1);
		SmsTransaction smsTransaction2 = new SmsTransaction(randomUtils
				.getRandomInteger(1000), "BOB001", randomUtils
				.getRandomInteger(1000), randomUtils
				.getBoundRandomDate(theYear), "TC");
		smsTransactionList.add(smsTransaction2);
		SmsTransaction smsTransaction3 = new SmsTransaction(randomUtils
				.getRandomInteger(1000), "GAM003", randomUtils
				.getRandomInteger(1000), randomUtils
				.getBoundRandomDate(theYear), "TC");
		smsTransactionList.add(smsTransaction3);
		SmsTransaction smsTransaction4 = new SmsTransaction(randomUtils
				.getRandomInteger(1000), "MAR008", randomUtils
				.getRandomInteger(1000), randomUtils
				.getBoundRandomDate(theYear), "TC");
		smsTransactionList.add(smsTransaction4);
		SmsTransaction smsTransaction5 = new SmsTransaction(randomUtils
				.getRandomInteger(1000), "CCL005", randomUtils
				.getRandomInteger(1000), randomUtils
				.getBoundRandomDate(theYear), "TC");
		smsTransactionList.add(smsTransaction5);
	}

	public List<SmsTransaction> getAllSmsTransaction() {
		return smsTransactionList;
	}

	public SmsTransaction getSmsTransaction(int index) {

		if (index >= smsTransactionList.size())
			throw new RuntimeException("The specified index is too high");

		return smsTransactionList.get(index);
	}

	public int getTotalSmsTransactionList() {
		return smsTransactionList.size();
	}

}

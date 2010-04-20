/***********************************************************************************
 * SmsSampleDataLoad.java
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

import java.util.List;

import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.SmsTransactionLogic;
import org.sakaiproject.sms.logic.impl.hibernate.SmsAccountLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTransactionLogicImpl;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.SmsTransaction;

/**
 * Use the application to insert test data into the database. Increase the value
 * of NUMBER_OF_REPETITIONS if you want to test the performance of the UI,
 * especially paging in the grids.
 * 
 * 
 */
public class SmsSampleDataLoad {

	public static final int NUMBER_OF_REPETITIONS = 20;

	private static HibernateLogicLocator hibernateLogicLocator = new HibernateLogicLocator();

	private SampleSmsTaskFactory taskFactory;
	private SampleSmsMessageFactory messageFactory;
	private SampleSmsTransactionFactory testSMSTransactionFactory;
	private SampleSmsAccountFactory sampleSmsAccountFactorty;

	public static void main(String[] args) {
		SmsSampleDataLoad sampleDataLoad = new SmsSampleDataLoad();

		sampleDataLoad.persistSmsMessages();
		sampleDataLoad.persistSmsTransactions();
		System.out.println("Done");
	}

	public SmsSampleDataLoad() {

		super();
		hibernateLogicLocator.setSmsAccountLogic(new SmsAccountLogicImpl());
		hibernateLogicLocator
				.setSmsTransactionLogic(new SmsTransactionLogicImpl());
	}

	private void persistSmsTransactions() {

		deleteSmsAccounts(hibernateLogicLocator.getSmsAccountLogic());
		deleteSmsTransactions(hibernateLogicLocator.getSmsTransactionLogic());

		testSMSTransactionFactory = new SampleSmsTransactionFactory();
		System.out.println("Inserting SmsAccounts:");

		persistsSmsAccounts(hibernateLogicLocator.getSmsAccountLogic());

		List<SmsAccount> persistedSmsAccounts = hibernateLogicLocator
				.getSmsAccountLogic().getAllSmsAccounts();

		System.out.println("Inserting SmsTransactions:");

		int index = 0;
		for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {
			List<SmsTransaction> smsTransactions = testSMSTransactionFactory
					.getAllSmsTransaction();

			for (SmsTransaction smsTransaction : smsTransactions) {

				smsTransaction.setSmsAccount(persistedSmsAccounts.get(0));
				smsTransaction.setSmsTaskId(Long.valueOf(index + 1));
				hibernateLogicLocator.getSmsTransactionLogic()
						.insertReserveTransaction(smsTransaction);
				index++;
			}
			testSMSTransactionFactory.refreshList();
		}
	}

	private void persistSmsMessages() {
		taskFactory = new SampleSmsTaskFactory();
		messageFactory = new SampleSmsMessageFactory();

		deleteSmsTasks(hibernateLogicLocator.getSmsTaskLogic());
		deleteSmsMessages(hibernateLogicLocator.getSmsMessageLogic());

		System.out.println("Inserting SmsMessages and Tasks:");

		for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {

			List<SmsMessage> smsMessages = messageFactory
					.getAllTestSmsMessages();
			List<SmsTask> smsTasks = taskFactory.getAllTestSmsTasks();

			for (SmsTask smsTask : smsTasks) {
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			}

			int index = 0;
			for (SmsMessage smsMessage : smsMessages) {
				smsMessage.setSmsTask(smsTasks.get(index));
				hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
						smsMessage);
				index++;
			}

			messageFactory.refreshList();
			taskFactory.refreshList();
		}
	}

	private void persistsSmsAccounts(SmsAccountLogic smsAccountLogic) {
		sampleSmsAccountFactorty = new SampleSmsAccountFactory();

		for (int i = 0; i < 1; i++) {

			List<SmsAccount> smsAccountsToAdd = sampleSmsAccountFactorty
					.getAllTestSmsMessages();
			for (SmsAccount smsAccount : smsAccountsToAdd) {
				smsAccountLogic.persistSmsAccount(smsAccount);
			}

			sampleSmsAccountFactorty.refreshList();
		}
	}

	private void deleteSmsAccounts(SmsAccountLogic smsAccountLogic) {
		System.out.println("Deleting SmsAccounts:");
		List<SmsAccount> smsAccounts = smsAccountLogic.getAllSmsAccounts();

		for (SmsAccount smsAccount : smsAccounts) {
			smsAccountLogic.deleteSmsAccount(smsAccount);
		}
	}

	private void deleteSmsTransactions(SmsTransactionLogic smsTransactionLogic) {
		System.out.println("Deleting SmsTransactions:");
		List<SmsTransaction> smsTransactionsToDelete = smsTransactionLogic
				.getAllSmsTransactions();

		for (SmsTransaction smsTransaction : smsTransactionsToDelete) {
			smsTransactionLogic.deleteSmsTransaction(smsTransaction);
		}
	}

	private void deleteSmsTasks(SmsTaskLogic smsTaskLogic) {
		System.out.println("Deleting SmsTasks:");
		List<SmsTask> smsTasksToDelete = smsTaskLogic.getAllSmsTask();

		for (SmsTask smsTask : smsTasksToDelete) {
			smsTaskLogic.deleteSmsTask(smsTask);
		}
	}

	private void deleteSmsMessages(SmsMessageLogic smsMessageLogic) {
		System.out.println("Deleting SmsMessages:");
		List<SmsMessage> smsMessagesToDelete = smsMessageLogic
				.getAllSmsMessages();

		for (SmsMessage smsMessage : smsMessagesToDelete) {
			smsMessageLogic.deleteSmsMessage(smsMessage);
		}
	}
}

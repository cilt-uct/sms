/**********************************************************************************
 * $URL:$
 * $Id:$
 ************************************************************************************
 * SmsMessageLogic.java
 * Copyright (c) 2008, 2009 Sakai Project/Sakai Foundation
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

package org.sakaiproject.sms.logic;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsMessage;

/**
 * The data service will handle all sms Message database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008
 */
public interface SmsMessageLogic {
	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsMessage(SmsMessage smsMessage);

	/**
	 * Gets a SmsMessage entity for the given id
	 * 
	 * @param Long
	 *            sms Message id
	 * @return sms Message
	 */
	public SmsMessage getSmsMessage(Long smsMessageId);

	/**
	 * Gets all the sms Message records
	 * 
	 * @return List of SmsMessage objects
	 */
	public List<SmsMessage> getAllSmsMessages();

	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updated on the DB.
	 * 
	 * @param sms
	 *            Message to be persisted
	 */
	public void persistSmsMessage(SmsMessage smsMessage);

	/**
	 * Updates messages for the given task from oldStatus to newStatus
	 * @param smsTaskId The task ID
	 * @param oldStatus The old message status
	 * @param newStatus The new message status
	 */
	public void updateStatusForMessages(Long smsTaskId, String oldStatus, String newStatus);

	/**
	 * Returns a message for the given smsc message id and smscID or null if
	 * nothing found.
	 * 
	 * @param smscMessageId
	 * @param smscID
	 * @return
	 */
	public SmsMessage getSmsMessageBySmscMessageId(String smscMessageId,
			String smscID);

	/**
	 * Gets a list of SmsMessage objects for the specified and specified status
	 * code(s)
	 * 
	 * @param sms
	 *            task id
	 * @param statusCode
	 *            (s)
	 * @return List<SmsMessage> - sms messages
	 */
	public List<SmsMessage> getSmsMessagesWithStatus(Long smsTaskId,
			String... statusCodes);

	/**
	 * Gets a search results for all SmsMessages that match the specified
	 * criteria
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public List<SmsMessage> getAllSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException;

	/**
	 * Gets a list of messages in SENT status which were sent before the given time
	 * @param cutoffTime The cutoff time
	 * @return List of messages meeting the criteria
	 */
	public List<SmsMessage> getSmsMessagesForTimeout(Date cutoffTime);
	
	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsMessage> getPagedSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException;

	/**
	 * Gets the new sms message instance test.
	 * 
	 * This method will instantiate and persist a SmsTask and return a
	 * SmsMessage with the associated SmsTask object set on it.
	 * <p>
	 * The message will not be persisted so this will need to be done manually.
	 * 
	 * @param mobileNumber
	 *            the mobile number
	 * @param messageBody
	 *            the message body
	 * 
	 * @return the new sms message instance test
	 */
	public SmsMessage getNewTestSmsMessageInstance(String mobileNumber,
			String messageBody);

	/**
	 * Returns all the messages for the given smstask.
	 * 
	 * @param smsTaskID
	 *            the smsTaskID id
	 */
	public List<SmsMessage> getSmsMessagesForTask(Long smsTaskId);

	/**
	 * Get a new session to be used for row locking to prevent concurrent
	 * message status update
	 * 
	 * @return
	 */
	public Session getNewHibernateSession();

}

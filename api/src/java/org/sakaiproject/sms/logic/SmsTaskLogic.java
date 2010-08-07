/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.sms.logic;

import java.util.List;

import org.hibernate.Session;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsTask;

/**
 * The data service will handle all sms task database transactions for the sms
 * tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008
 */
public interface SmsTaskLogic {

	/**
	 * Delete sms task.
	 * 
	 * @param smsTask
	 *            the sms task
	 */
	public void deleteSmsTask(SmsTask smsTask);

	/**
	 * Gets the sms task.
	 * 
	 * @param smsTaskId
	 *            the sms task id
	 * 
	 * @return the sms task
	 */
	public SmsTask getSmsTask(Long smsTaskId);

	/**
	 * Gets the next sms task to be processed.
	 * 
	 * @return the next sms task
	 */
	public SmsTask getNextSmsTask();

	/**
	 * Gets all the queued MO tasks.
	 * 
	 * @return
	 */
	public List<SmsTask> getAllMOTasks();

	/**
	 * Gets the all mo-sms task.
	 * 
	 * @return the all sms task
	 */
	public List<SmsTask> getAllSmsTask();

	/**
	 * Persist sms task.
	 * 
	 * @param smsTask
	 *            the sms task
	 */
	public void persistSmsTask(SmsTask smsTask);

	/**
	 * Gets a all search results for the specified search criteria
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public List<SmsTask> getAllSmsTasksForCriteria(SearchFilterBean searchBean)
			throws SmsSearchException;

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsTask> getPagedSmsTasksForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException;

	/**
	 * Increments the total messages processed on a task by one.
	 * 
	 * @param smsTask
	 */
	public void incrementMessagesProcessed(SmsTask smsTask);

	/**
	 * Get list of tasks that can be marked as complete. If the total messages
	 * processed equals the actual group size the task is marked as complete.
	 */
	public List<SmsTask> getTasksToMarkAsCompleted();

	/**
	 * Get list of tasks that are in state COMPLETE but require billing adjustments.
	 */
	public List<SmsTask> getTasksWithLateBilling();

	/**
	 * Increments the total messages processed and/or delivered on a task by one.
	 * 
	 * @param smsTask
	 */
	public void incrementMessageCounts(SmsTask smsTask, boolean incrementProcessed, boolean incrementDelivered);

	/**
	 * Get a new session to be used for row locking to prevent concurrent task
	 * status update to status C
	 * 
	 * @return
	 */
	public Session getNewHibernateSession();
	
	/**
	 *  Get tasks that are not complete
	 * @return
	 */
	public List<SmsTask> getTasksNotComplete();
}

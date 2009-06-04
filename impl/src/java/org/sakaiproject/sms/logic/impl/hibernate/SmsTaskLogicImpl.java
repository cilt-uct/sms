/***********************************************************************************
 * SmsTaskLogicImpl.java
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

package org.sakaiproject.sms.logic.impl.hibernate;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * The data service will handle all sms task database transactions for the sms
 * tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008
 */
@SuppressWarnings("unchecked")
public class SmsTaskLogicImpl extends SmsLogic implements SmsTaskLogic {

	private static final Log LOG = LogFactory.getLog(SmsTaskLogicImpl.class);

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsTask(SmsTask smsTask) {
		delete(smsTask);
	}

	/**
	 * Gets a SmsTask entity for the given id
	 * 
	 * @param Long
	 *            sms task id
	 * @return sms task
	 */
	public SmsTask getSmsTask(Long smsTaskId) {
		return (SmsTask) findById(SmsTask.class, smsTaskId);
	}

	/**
	 * Gets all the sms task records
	 * 
	 * @return List of SmsTask objects
	 */
	public List<SmsTask> getAllSmsTask() {
		return smsDao.runQuery("from SmsTask ORDER BY DATE_TO_SEND DESC");
	}

	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updated on the DB.
	 * 
	 * @param sms
	 *            task to be persisted
	 */
	public void persistSmsTask(SmsTask smsTask) {
		persist(smsTask);
	}

	/**
	 * Gets the next sms task to be processed.
	 * 
	 * @return next sms task
	 */
	@SuppressWarnings("unchecked")
	public SmsTask getNextSmsTask() {
		final StringBuilder hql = new StringBuilder();
		hql.append(" from SmsTask task where task.dateToSend <= :today ");
		hql.append(" and task.messageTypeId = (:messageTypeId) ");
		hql.append(" and task.statusCode IN (:statusCodes) ");
		hql.append(" order by task.messageTypeId ,task.dateToSend");
		final List<SmsTask> tasks = smsDao.runQuery(hql.toString(),
				new QueryParameter("today", getDelayedCurrentDate(10),
						Hibernate.TIMESTAMP), new QueryParameter(
						"messageTypeId",
						SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING,
						Hibernate.INTEGER), new QueryParameter("statusCodes",
						new Object[] { SmsConst_DeliveryStatus.STATUS_PENDING,
								SmsConst_DeliveryStatus.STATUS_INCOMPLETE,
								SmsConst_DeliveryStatus.STATUS_RETRY },
						Hibernate.STRING));

		LOG.debug("getNextSmsTask() HQL: " + hql);
		if (tasks != null && !tasks.isEmpty()) {
			// Gets the oldest dateToSend. I.e the first to be processed.
			return tasks.get(0);
		}
		return null;
	}

	/**
	 * Gets a all search results for the specified search criteria
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public List<SmsTask> getAllSmsTasksForCriteria(SearchFilterBean searchBean)
			throws SmsSearchException {
		return getSmsTasksForCriteria(searchBean);
	}

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsTask> getPagedSmsTasksForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {

		final List<SmsTask> tasks = getSmsTasksForCriteria(searchBean);

		final SearchResultContainer<SmsTask> container = new SearchResultContainer<SmsTask>(
				getPageSize());
		container.setTotalResultSetSize(Long.valueOf(tasks.size()));
		container
				.calculateAndSetPageResults(tasks, searchBean.getCurrentPage());

		return container;
	}

	private List<SmsTask> getSmsTasksForCriteria(SearchFilterBean searchBean)
			throws SmsSearchException {
		Criteria crit = smsDao.createCriteria(SmsTask.class);
		try {
			// Message status
			if (searchBean.getStatus() != null
					&& !searchBean.getStatus().trim().equals("")) {
				crit.add(Restrictions.ilike("statusCode", searchBean
						.getStatus()));
			}
			if (searchBean.getMessageTypeId() != null) {
				crit.add(Restrictions.eq("messageTypeId", searchBean
						.getMessageTypeId()));
			}

			// Sakai tool name
			if (searchBean.getToolName() != null
					&& !searchBean.getToolName().trim().equals("")) {
				crit.add(Restrictions.ilike("sakaiToolName", searchBean
						.getToolName(), MatchMode.ANYWHERE));
			}

			// Date to send start
			if (searchBean.getDateFrom() != null) {
				Date date = DateUtil.getDateFromStartDateString(searchBean
						.getDateFrom());
				crit.add(Restrictions.ge("dateToSend", date));
			}

			// Date to send end
			if (searchBean.getDateTo() != null) {
				Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				crit.add(Restrictions.le("dateToSend", date));
			}

			// Sender name
			if (searchBean.getSender() != null
					&& !searchBean.getSender().trim().equals("")) {
				crit.add(Restrictions.ilike("senderUserName", searchBean
						.getSender(), MatchMode.ANYWHERE));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {
				crit.addOrder((searchBean.sortAsc() ? Order.asc(searchBean
						.getOrderBy()) : Order.desc(searchBean.getOrderBy())));
			}

			crit.setMaxResults(SmsConstants.READ_LIMIT);

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}

		LOG.debug(crit.toString());

		return crit.list();
	}

	private int getPageSize() {
		SmsConfig smsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						externalLogic.getCurrentSiteId());
		if (smsConfig == null) {
			return SmsConstants.DEFAULT_PAGE_SIZE;
		} else {
			return smsConfig.getPagingSize();
		}
	}

	/**
	 * Increments the total messages processed on a task by one.
	 * 
	 * @param smsTask
	 */
	public void incrementMessagesProcessed(SmsTask smsTask) {

		// The < test because very late delivery reports can change a message
		// status from Timed out to Delivered, causing another increase of
		// MESSAGES_PROCESSED called from MessageReceiverListenerImpl
		String hql = "update SmsTask set MESSAGES_PROCESSED = MESSAGES_PROCESSED + 1  where TASK_ID = :smsTaskID and MESSAGES_PROCESSED < GROUP_SIZE_ACTUAL";
		smsDao.executeUpdate(hql, new QueryParameter("smsTaskID", smsTask
				.getId(), Hibernate.LONG));

	}

	/**
	 * Increments the total messages delivered on a task by one.
	 * 
	 * @param smsTask
	 */
	public void incrementMessagesDelivered(SmsTask smsTask) {

		String hql = "update SmsTask set MESSAGES_DELIVERED = MESSAGES_DELIVERED+1  where TASK_ID = :smsTaskID";
		smsDao.executeUpdate(hql, new QueryParameter("smsTaskID", smsTask
				.getId(), Hibernate.LONG));
	}

	/**
	 * Checks for tasks that can be marked as complete. If the total messages
	 * processed equals the actual group size the task is marked as complete.
	 */
	public List<SmsTask> checkAndSetTasksCompleted() {

		List<SmsTask> smsTasks = smsDao
				.runQuery(
						"from SmsTask mes where MESSAGES_PROCESSED = GROUP_SIZE_ACTUAL and STATUS_CODE NOT IN (:smsTaskStatus)",
						new QueryParameter("smsTaskStatus", new Object[] {
								SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED,
								SmsConst_DeliveryStatus.STATUS_FAIL },

						Hibernate.STRING));

		String hql = "update SmsTask set STATUS_CODE = :doneStatus where MESSAGES_PROCESSED =GROUP_SIZE_ACTUAL and STATUS_CODE NOT IN (:smsTaskStatus)";
		smsDao.executeUpdate(hql,
				new QueryParameter("doneStatus",
						SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED,
						Hibernate.STRING), new QueryParameter("smsTaskStatus",
						new Object[] {
								SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED,
								SmsConst_DeliveryStatus.STATUS_FAIL },
						Hibernate.STRING));
		return smsTasks;
	}

	public List<SmsTask> getAllMOTasks() {
		StringBuilder hql = new StringBuilder();
		hql.append(" from SmsTask task where task.dateToSend <= :today ");
		hql.append(" and task.statusCode IN (:statusCodes) ");
		hql.append(" and task.messageTypeId = (:messageTypeId) ");
		hql.append(" order by task.messageTypeId ,task.dateToSend");
		List<SmsTask> tasks = smsDao.runQuery(hql.toString(),
				new QueryParameter("today", getDelayedCurrentDate(10),
						Hibernate.TIMESTAMP), new QueryParameter("statusCodes",
						new Object[] {

						SmsConst_DeliveryStatus.STATUS_RETRY },

						Hibernate.STRING), new QueryParameter("messageTypeId",
						SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING,
						Hibernate.INTEGER));

		LOG.debug("processMOTasks() HQL: " + hql);
		if (tasks != null && !tasks.isEmpty()) {
			// Gets the oldest dateToSend. I.e the first to be processed.
			return tasks;
		}
		return null;
	}

}

/**********************************************************************************
 * $URL: $
 * $Id: $
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
package org.sakaiproject.sms.logic.impl.hibernate;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.QueryParameter;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.DateUtil;
import org.sakaiproject.sms.util.GsmCharset;

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

		externalLogic.postEvent(ExternalLogic.SMS_EVENT_TASK_DELETE,
				"/sms-task/" + smsTask.getId(), smsTask.getSakaiSiteId());

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
		try {
			return (SmsTask) findById(SmsTask.class, smsTaskId);
		} catch (NumberFormatException numEx) {
			throw new IllegalArgumentException(
					"Supplied task parameter is invalid: " + smsTaskId);
		}
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
		//the site Id can't be null
		if (smsTask.getSakaiSiteId() == null) {
			throw new IllegalArgumentException("SakaiSiteId can't be null");
		}
		if (smsTask.getSmsAccountId() == null) {
			throw new IllegalArgumentException("SmsAccountId can't be null");
		}
		
		//message can't be longer than 160 chars
		if (smsTask.getMessageBody() != null && smsTask.getMessageBody().length() > 160) {
			throw new IllegalArgumentException("Message body can't be longer than 160 chars!");
		}
		
		//check the GSM encoded length is > 160
		if (smsTask.getMessageBody() != null) {
			GsmCharset charSet = new GsmCharset();
			byte[] encoded = charSet.utfToGsm(smsTask.getMessageBody());
			LOG.debug("message " + smsTask.getMessageBody() + " length is " + encoded.length);
			if (encoded.length > 160) {
				throw new IllegalArgumentException("Message body can't be longer than 160 chars!");
			}

		}
		
		if (smsTask.getAttemptCount() == null) {
			smsTask.setAttemptCount(0);
		}
		
		
		
		persist(smsTask);
	}

	/**
	 * Gets the next sms task to be processed, and changes its status to BUSY.
	 * 
	 * @return next sms task, or null if there is no next task to be processed.
	 */
	public SmsTask getNextSmsTask() {

		// Gets the oldest dateToSend, i.e. the first to be processed
		
		final StringBuilder hql = new StringBuilder();
		hql.append(" from SmsTask task where task.dateToSend <= :today ");
		hql.append(" and task.messageTypeId = (:messageTypeId) ");
		hql.append(" and task.statusCode IN (:statusCodes) ");
		hql.append(" and (task.nextRetryTime < now() or task.nextRetryTime is null)");
		hql.append(" order by task.dateToSend, task.id");
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
			
			// Find the first task which can be locked and changed to STATUS_BUSY
			
			for (SmsTask nextSmsTask : tasks) {
					
				Session session = null;
				Transaction tx = null;
				try {
					session = getHibernateLogicLocator().getSmsTaskLogic()
							.getNewHibernateSession();
					tx = session.beginTransaction();
					SmsTask smsTask = (SmsTask) session.get(SmsTask.class, nextSmsTask.getId(),
							LockMode.UPGRADE);
					
					if (!SmsConst_DeliveryStatus.STATUS_PENDING.equals(smsTask.getStatusCode()) &&
						!SmsConst_DeliveryStatus.STATUS_INCOMPLETE.equals(smsTask.getStatusCode()) &&
						!SmsConst_DeliveryStatus.STATUS_RETRY.equals(smsTask.getStatusCode())) {
						// Another thread or app server has changed this task's status, so we ignore it
						tx.rollback();
						session.close();
					} else {
						smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
						session.update(smsTask);
						tx.commit();
						session.close();
						return smsTask;
					}
				} catch (HibernateException e) {
					LOG.error("Error processing next task", e);
					if (tx != null) {
						tx.rollback();
					}
					if (session != null) {
						session.close();
					}
				}
			}
		}
		
		// Didn't find a task
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
		Search search = new Search();
		try {
			// Message status
			if (searchBean.getStatus() != null
					&& !searchBean.getStatus().trim().equals("")) {
				search.addRestriction(new Restriction("statusCode", searchBean
						.getStatus()));
			}
			if (searchBean.getMessageTypeId() != null) {
				search.addRestriction(new Restriction("messageTypeId",
						searchBean.getMessageTypeId()));
			}

			// Sakai site Id
			if (searchBean.getSakaiSiteId() != null) {
				search.addRestriction(new Restriction("sakaiSiteId", searchBean
						.getSakaiSiteId()));
			}

			// Sender user Id
			if (searchBean.getSenderUserId() != null) {
				search.addRestriction(new Restriction("senderUserId",
						searchBean.getSenderUserId()));
			}

			// Sakai tool name
			if (searchBean.getToolName() != null
					&& !searchBean.getToolName().trim().equals("")) {
				search.addRestriction(new Restriction("sakaiToolName",
						searchBean.getToolName()));
			}

			// Sakai tool ID
			if (searchBean.getToolId() != null
					&& !searchBean.getToolId().trim().equals("")) {
				search.addRestriction(new Restriction("sakaiToolId",
						searchBean.getToolId()));
			}

			// Date to send start
			if (searchBean.getDateFrom() != null) {
				Date date = DateUtil.getDateFromStartDateString(searchBean
						.getDateFrom());
				search.addRestriction(new Restriction("dateToSend", date,
						Restriction.GREATER));
			}

			// Date to send end
			if (searchBean.getDateTo() != null) {
				Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				search.addRestriction(new Restriction("dateToSend", date,
						Restriction.LESS));
			}

			// Sender name
			if (searchBean.getSender() != null
					&& !searchBean.getSender().trim().equals("")) {
				search.addRestriction(new Restriction("senderUserName",
						searchBean.getSender()));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {
				/*
				 * crit.addOrder((searchBean.sortAsc() ? Order.asc(searchBean
				 * .getOrderBy()) : Order.desc(searchBean.getOrderBy())));
				 */

				search.addOrder(new Order(searchBean.getOrderBy(), searchBean
						.sortAsc()));
			}

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}

		return smsDao.findBySearch(SmsTask.class, search);
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
	public void incrementMessageCounts(SmsTask smsTask, boolean incrementProcessed, boolean incrementDelivered) {

		// The < test because very late delivery reports can change a message
		// status from Timed out to Delivered, causing another increase of
		// MESSAGES_PROCESSED called from MessageReceiverListenerImpl
		
		if (!(incrementProcessed || incrementDelivered))
			return;
		
		StringBuilder hql = new StringBuilder("update SmsTask set ");
	
		if (incrementProcessed) {
			hql.append("MESSAGES_PROCESSED = MESSAGES_PROCESSED + 1");
		}
		
		if (incrementProcessed && incrementDelivered) {
			hql.append(", ");
		}
		
		if (incrementDelivered) {
			hql.append("MESSAGES_DELIVERED = MESSAGES_DELIVERED + 1");
		}
		
		hql.append(" where TASK_ID = ?");
		
		smsDao.executeUpdate(hql.toString(), smsTask.getId());
	}

	/**
	 * Increments the total messages delivered on a task by one.
	 * 
	 * @param smsTask
	 */
	public void incrementMessagesProcessed(SmsTask smsTask) {
		incrementMessageCounts(smsTask, true, false);
	}

	/**
	 * Checks for tasks that can be marked as complete. If the total messages
	 * processed equals the actual group size the task is marked as complete.
	 * Limit to batch size of 100.
	 */
	public List<SmsTask> getTasksToMarkAsCompleted() {

		String sql = "from SmsTask where MESSAGES_PROCESSED = GROUP_SIZE_ACTUAL and STATUS_CODE NOT IN (?,?)";
		Object[] params1 = new Object[2];
		params1[0] = SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED;
		params1[1] = SmsConst_DeliveryStatus.STATUS_FAIL;
		return smsDao.executeQuery(sql, params1, 0, 100);
	}

	public List<SmsTask> getTasksWithLateBilling() {

		String sql = "from SmsTask where STATUS_CODE = ? and CREDITS_ACTUAL <> BILLED_CREDITS";
		Object[] params1 = new Object[1];
		params1[0] = SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED;
		return smsDao.executeQuery(sql, params1, 0, 100);
	}


	/**
	 * Checks for tasks that are candidates for complete because the outstanding messages
	 * have timed out waiting for delivery reports.
	 */
	public List<SmsTask> getTasksNotComplete() {
		Session session = null;
		Transaction tx = null;
		List<SmsTask> ret = new ArrayList<SmsTask>();
		session = getHibernateLogicLocator().getSmsTaskLogic()
		.getNewHibernateSession();
		tx = session.beginTransaction();
		String sql = "from SmsTask where MESSAGES_PROCESSED < GROUP_SIZE_ACTUAL and STATUS_CODE NOT IN (?,?)";
		Object[] params1 = new Object[2];
		params1[0] = SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED;
		params1[1] = SmsConst_DeliveryStatus.STATUS_FAIL;
		ret = smsDao.executeQuery(sql, params1, 0, 100);
		tx.commit();
		session.close();

		return ret;

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

	public Session getNewHibernateSession() {
		return smsDao.getTheHibernateTemplate().getSessionFactory()
				.openSession();
	}
}

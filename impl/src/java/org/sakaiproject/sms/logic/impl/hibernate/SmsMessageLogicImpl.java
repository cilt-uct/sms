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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.QueryParameter;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * The data service will handle all sms Message database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008
 */
@SuppressWarnings("unchecked")
public class SmsMessageLogicImpl extends SmsLogic implements SmsMessageLogic {

	private static final Log LOG = LogFactory.getLog(SmsMessageLogicImpl.class);

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
	public void deleteSmsMessage(SmsMessage smsMessage) {
		delete(smsMessage);
	}

	/**
	 * Gets all the sms Message records
	 * 
	 * @return List of SmsMessage objects
	 */
	public List<SmsMessage> getAllSmsMessages() {
		return smsDao.runQuery("from SmsMessage");
	}

	/**
	 * Gets a search results for all SmsMessages that match the specified
	 * criteria
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public List<SmsMessage> getAllSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		return getSmsMessagesForCriteria(searchBean);
	}

	/**
	 * ======================== ONLY FOR TESTING ==================== A new sms
	 * message factory method. Only used for testing.
	 * 
	 * This method will instantiate a SmsTask and return a SmsMessage with the
	 * associated SmsTask object set on it.
	 * <p>
	 * The message will not be persisted.
	 * 
	 * @param mobileNumber
	 *            the mobile number
	 * @param messageBody
	 *            the message body
	 * 
	 * @return the new sms message instance test
	 */
	public SmsMessage getNewTestSmsMessageInstance(String mobileNumber,
			String messageBody) {
		final SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSmsAccountId(1l);
		smsTask.setDateCreated(new Date(System.currentTimeMillis()));
		smsTask.setDateToSend(new Date(System.currentTimeMillis()));
		smsTask.setDateProcessed(new Date(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setGroupSizeEstimate(1);
		smsTask.setGroupSizeActual(1);
		smsTask.setDeliveryUserId("sakaiUserID");
		smsTask.setDeliveryGroupId("SakaiGroupID");
		smsTask.setDeliveryGroupName("SakaiGroupName");
		smsTask.setCreditEstimate(1);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());

		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		final SmsMessage smsMessage = new SmsMessage();
		smsMessage.setSmsTask(smsTask);
		smsMessage.setMobileNumber(mobileNumber);
		smsMessage.setMessageBody(messageBody);
		smsMessage.setSmscMessageId("smscMessageId");
		smsMessage.setSakaiUserId("sakaiUserId");
		smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

		smsTask.getSmsMessages().add(smsMessage);

		return smsMessage;
	}

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsMessage> getPagedSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {

		final List<SmsMessage> messages = getSmsMessagesForCriteria(searchBean);
		SearchResultContainer<SmsMessage> con = new SearchResultContainer<SmsMessage>(
				getPageSize());
		con.setTotalResultSetSize(Long.valueOf(messages.size()));
		con.calculateAndSetPageResults(messages, searchBean.getCurrentPage());
		LOG.debug(con.toString());

		return con;
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
	 * Gets a SmsMessage entity for the given id
	 * 
	 * @param Long
	 *            sms Message id
	 * @return sms Message
	 */
	public SmsMessage getSmsMessage(Long smsMessageId) {
		return (SmsMessage) findById(SmsMessage.class, smsMessageId);
	}

	/**
	 * Returns a message for the given smsc message id or null if nothing found
	 * 
	 * @param smsc
	 *            message id
	 * @return sms message
	 */
	public SmsMessage getSmsMessageBySmscMessageId(String smscMessageId,
			String smscID) {
		String hql = "from SmsMessage mes where mes.smscMessageId = :smscMessageId and mes.smscId = :smscID";

		List<SmsMessage> messages = smsDao.runQuery(hql, new QueryParameter(
				"smscMessageId", smscMessageId, Hibernate.STRING),
				new QueryParameter("smscID", smscID, Hibernate.STRING));
		if (messages != null && !messages.isEmpty()) {
			return messages.get(0);
		}
		return null;
	}

	/**
	 * Returns all the messages for the given smstask.
	 * 
	 * @param smsTaskID
	 *            the smsTaskID id
	 */
	public List<SmsMessage> getSmsMessagesForTask(Long smsTaskId) {
		StringBuilder hql = new StringBuilder();
		hql.append(" from SmsMessage message where 1=1  ");
		if (smsTaskId != null) {
			hql.append(" and message.smsTask.id = :smsTaskId ");
		}
		LOG
				.debug("getSmsTasksFilteredByMessageStatus() HQL: "
						+ hql.toString());
		QueryParameter[] queryParameters = { new QueryParameter("smsTaskId",
				smsTaskId, Hibernate.LONG) };

		List theMessages = smsDao.runQuery(hql.toString(), queryParameters);
		if (theMessages != null && !theMessages.isEmpty()) {
			return theMessages;
		}
		return new ArrayList<SmsMessage>();
	}

	private List<SmsMessage> getSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		Search search = new Search();
		LOG.debug(searchBean.toString());
		try {
			// Message status
			if (searchBean.getStatus() != null
					&& !searchBean.getStatus().trim().equals("")) {
				search.addRestriction(new Restriction("statusCode", searchBean
						.getStatus()));
			}

			// Task Id
			if (searchBean.getTaskId() != null
					&& !"".equals(searchBean.getTaskId().trim())) {
				search.addRestriction(new Restriction("smsTask.id", Long
						.valueOf(searchBean.getTaskId())));
			}

			// Date to send start
			if (searchBean.getDateFrom() != null) {
				Date date = DateUtil.getDateFromStartDateString(searchBean
						.getDateFrom());
				search.addRestriction(new Restriction("dateDelivered", date,
						Restriction.GREATER));
			}

			// Date to send end
			if (searchBean.getDateTo() != null) {
				Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				search.addRestriction(new Restriction("dateDelivered", date,
						Restriction.LESS));
			}

			/*
			 * // Sender name if (searchBean.getSender() != null &&
			 * !searchBean.getSender().trim().equals("")) {
			 * search.addRestriction(new Restriction("smsTask.senderUserName",
			 * searchBean.getSender())); }
			 */

			// Mobile number
			if (searchBean.getNumber() != null
					&& !searchBean.getNumber().trim().equals("")) {
				search.addRestriction(new Restriction("mobileNumber",
						searchBean.getNumber()));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {
				/*
				 * crit.addOrder((searchBean.sortAsc() ? Order.asc(searchBean
				 * .getOrderBy()) : Order.desc(searchBean.getOrderBy())));
				 */
				search
						.addOrder(new org.sakaiproject.genericdao.api.search.Order(
								searchBean.getOrderBy(), searchBean.sortAsc()));
			}

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}
		return smsDao.findBySearch(SmsMessage.class, search);
	}

	/**
	 * Gets a list of SmsMessage objects for the specified and specified status
	 * code(s).
	 * 
	 * It will ignore the smsTaskId if it is passed as null and return all
	 * smsMessages with the specified status code(s).
	 * 
	 * @param sms
	 *            task id
	 * @param statusCode
	 *            (s)
	 * @return List<SmsMessage> - sms messages
	 */
	public List<SmsMessage> getSmsMessagesWithStatus(Long smsTaskId,
			String... statusCodes) {
		List<SmsMessage> messages = new ArrayList<SmsMessage>();

		// Return empty list if no status codes were passed in
		if (statusCodes.length > 0) {

			StringBuilder hql = new StringBuilder();
			hql.append(" from SmsMessage message where 1=1  ");
			if (smsTaskId != null) {
				hql.append(" and message.smsTask.id = :smsTaskId ");
			}
			hql.append(" and message.statusCode IN (:statusCodes) ");

			LOG.debug("getSmsTasksFilteredByMessageStatus() HQL: "
					+ hql.toString());

			QueryParameter[] queryParameters;

			if (smsTaskId == null) {
				queryParameters = new QueryParameter[1];
			} else {
				queryParameters = new QueryParameter[2];
			}

			queryParameters[0] = new QueryParameter("statusCodes", statusCodes,
					Hibernate.STRING);

			if (smsTaskId != null) {
				queryParameters[1] = new QueryParameter("smsTaskId", smsTaskId,
						Hibernate.LONG);
			}
			messages = smsDao.runQuery(hql.toString(), queryParameters);
		}
		return messages;
	}

	public List<SmsMessage> getSmsMessagesForTimeout(Date cutoffTime) {

		String hql = " from SmsMessage message where message.statusCode = :statusCode and message.dateSent < :cutoffTime";
	
		QueryParameter[] queryParameters = new QueryParameter[2];

		queryParameters[0] = new QueryParameter("statusCode", 
				SmsConst_DeliveryStatus.STATUS_SENT, Hibernate.STRING);

		queryParameters[1] = new QueryParameter("cutoffTime", 
				cutoffTime, Hibernate.DATE);

		return smsDao.runQuery(hql, queryParameters);
	}
	
	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updated on the DB.
	 * 
	 * @param sms
	 *            Message to be persisted
	 */
	public void persistSmsMessage(SmsMessage smsMessage) {
		// Preserve DB integrity by setting sakaiUserId to an empty string
		if (smsMessage.getSakaiUserId() == null) {
			smsMessage.setSakaiUserId("");
		}
		if (smsMessage.getDateQueued() == null) {
			smsMessage.setDateQueued(new Date());
		}

		persist(smsMessage);
	}

	public Session getNewHibernateSession() {
		return smsDao.getTheHibernateTemplate().getSessionFactory()
				.openSession();
	}

	public void updateStatusForMessages(Long smsTaskId, String oldStatus, String newStatus) {
		
		if (smsTaskId == null || oldStatus == null || newStatus == null)
			return;
		
		String hql = "update SmsMessage set statusCode = ? where TASK_ID = ? and statusCode = ?";
		ArrayList<Object> parms = new ArrayList<Object>();
		parms.add(newStatus);
		parms.add(smsTaskId);
		parms.add(oldStatus);

		smsDao.executeUpdate(hql, parms);
	}
}

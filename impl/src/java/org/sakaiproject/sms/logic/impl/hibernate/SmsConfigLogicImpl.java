/***********************************************************************************
 * SmsAccountLogicImpl.java
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

import java.util.List;

import org.hibernate.Hibernate;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;
import org.sakaiproject.sms.logic.hibernate.SmsConfigLogic;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;

/**
 * The data service will handle all sms config database transactions for the sms
 * tool in Sakai.
 *
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
@SuppressWarnings("unchecked")
public class SmsConfigLogicImpl extends SmsLogic implements SmsConfigLogic {

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsConfig(SmsConfig smsConfig) {
		delete(smsConfig);
	}

	/**
	 * Gets a SmsConfig entity for the given id
	 *
	 * @param Long
	 *            sms configuration id
	 * @return sms congiguration
	 */
	public SmsConfig getSmsConfig(Long smsConfigId) {
		return (SmsConfig) findById(SmsConfig.class, smsConfigId);
	}

	/**
	 * Gets all the sms configuration records
	 *
	 * @return List of SmsConfig objects
	 */
	public List<SmsConfig> getAllSmsConfig() {
		List<SmsConfig> configs = smsDao.runQuery("from SmsConfig");
		return configs;
	}

	/**
	 * This method will persists the given object.
	 *
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updates on the DB.
	 *
	 * @param sms
	 *            confuguration to be persisted
	 */
	public  void persistSmsConfig(SmsConfig smsConfig) {
		persist(smsConfig);
	}

	/**
	 * Gets the sms config by sakai site id. If no entry can be found for the
	 * specified site, then a site config is created with default values.
	 *
	 * @param sakaiSiteId
	 *
	 * @return the sms config by sakai site id
	 */
	public  SmsConfig getOrCreateSmsConfigBySakaiSiteId(
			String sakaiSiteId) {
		List<SmsConfig> configs = smsDao.runQuery(
				"from SmsConfig conf where conf.sakaiSiteId = :id",
				new QueryParameter("id", sakaiSiteId, Hibernate.STRING));
		SmsConfig config = null;
		if (configs.size() == 1) {
			config = configs.get(0);
		}

		if (config == null) {
			config = createDefaultSmsConfig(sakaiSiteId);
			persistSmsConfig(config);

		}
		return config;
	}

	public SmsConfig getOrCreateSystemSmsConfig() {
		return getOrCreateSmsConfigBySakaiSiteId(SmsHibernateConstants.SMS_SYSTEM_SAKAI_SITE_ID);
	}

	public SmsConfig createDefaultSmsConfig(String sakaiSiteId) {
		SmsConfig config = new SmsConfig();

		// Settings for the Sakai instance
		if (sakaiSiteId != null && sakaiSiteId.equals("")) {
			config.setSakaiToolId("");
			config

			.setSmsRetryMaxCount(SmsHibernateConstants.MAXIMUM_RETRY_COUNT);

			config
					.setDelReportTimeoutDuration(SmsHibernateConstants.DEL_REPORT_TIMEOUT_DURATION);

			config
					.setSmsRetryScheduleInterval(SmsHibernateConstants.RETRY_SCHEDULE_INTERVAL);
			config
					.setSmsTaskMaxLifeTime(SmsHibernateConstants.MAXIMUM_TASK_LIFETIME);
			config
					.setSchedulerInterval(SmsHibernateConstants.SCHEDULER_INTERVAL);

			config.setCreditCost(SmsHibernateConstants.COST_OF_CREDIT);

			config
					.setUseSiteAcc(SmsHibernateConstants.DEFAULT_ACCOUNT_USE_SITE_ACCOUNT);
		} else {
			// Setting for each site
			config.setSakaiToolId("DummyToolId");
			config.setOverdraftLimit(SmsHibernateConstants.OVERDRAFT_LIMIT);
			config
					.setNotificationEmail(SmsHibernateConstants.NOTIFICATION_EMAIL);
			config
					.setNotificationEmailBilling("notificationBilling@instution.com");
			config.setNotificationEmailSent("notificationSent@instution.com");
			config.setPagingSize(SmsHibernateConstants.DEFAULT_PAGE_SIZE);
			config.setReceiveIncomingEnabled(true);
			config.setSendSmsEnabled(true);

		}
		config.setSakaiSiteId(sakaiSiteId);

		return config;

	}

	/**
	 * Gets the sms config by sakai tool id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return the sms config by sakai tool id
	 */
	public SmsConfig getSmsConfigBySakaiToolId(String id) {
		String hql = "from SmsConfig conf where conf.sakaiToolId = :id";

		List<SmsConfig> configs = smsDao.runQuery(hql, new QueryParameter("id",
				id, Hibernate.STRING));
		SmsConfig config = null;

		if (configs.size() == 1) {
			config = configs.get(0);
		}
		return config;
	}

}

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

import java.util.List;

import org.hibernate.Hibernate;
import org.sakaiproject.sms.logic.QueryParameter;
import org.sakaiproject.sms.logic.SmsConfigLogic;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.constants.SmsConstants;

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
		return smsDao.runQuery("from SmsConfig");

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
	public void persistSmsConfig(SmsConfig smsConfig) {
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
	public SmsConfig getOrCreateSmsConfigBySakaiSiteId(String sakaiSiteId) {
		
		final List<SmsConfig> configs = smsDao.runQuery(
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
		return getOrCreateSmsConfigBySakaiSiteId(SmsConstants.SMS_SYSTEM_SAKAI_SITE_ID);
	}

	public SmsConfig createDefaultSmsConfig(String sakaiSiteId) {
		final SmsConfig config = new SmsConfig();

		// Settings for the Sakai instance
		if (sakaiSiteId != null && "".equals(sakaiSiteId)) {
			config.setSakaiToolId("");
			config.setSmsRetryMaxCount(SmsConstants.MAXIMUM_RETRY_COUNT);
			config.setMaxActiveThreads(SmsConstants.DEFAULT_MAX_ACTIVE_THREADS);
			config
					.setDelReportTimeoutDuration(SmsConstants.DEL_REPORT_TIMEOUT_DURATION);
			config
					.setSmsRetryScheduleInterval(SmsConstants.RETRY_SCHEDULE_INTERVAL);
			config.setSmsTaskMaxLifeTime(SmsConstants.MAXIMUM_TASK_LIFETIME);
			config.setSchedulerInterval(SmsConstants.SCHEDULER_INTERVAL);
			config.setCreditCost(SmsConstants.COST_OF_CREDIT);
			config.setUseSiteAcc(SmsConstants.DEFAULT_ACCOUNT_USE_SITE_ACCOUNT);

		} else {
			// Setting for each site
			// config.setSakaiToolId("DummyToolId");
			config.setOverdraftLimit(SmsConstants.OVERDRAFT_LIMIT);
			config.setPagingSize(SmsConstants.DEFAULT_PAGE_SIZE);
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
		final String hql = "from SmsConfig conf where conf.sakaiToolId = :id";

		final List<SmsConfig> configs = smsDao.runQuery(hql,
				new QueryParameter("id", id, Hibernate.STRING));
		SmsConfig config = null;

		if (configs.size() == 1) {
			config = configs.get(0);
		}
		return config;
	}

}

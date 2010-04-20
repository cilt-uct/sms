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

import org.sakaiproject.sms.model.SmsConfig;

/**
 * SMS configuration is stored in SMS_CONFIG. It caters for site-specific
 * settings as well as system-wide settings. System settings are stored in the
 * record with SAKAI_SITE_ID="". Settings could also be specific to a Sakai tool
 * by setting SAKAI_TOOL_ID.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
public interface SmsConfigLogic {

	/**
	 * Gets the sms config.
	 * 
	 * @param smsConfigId
	 *            the sms config id
	 * 
	 * @return the sms config
	 */
	public SmsConfig getSmsConfig(Long smsConfigId);

	/**
	 * Persist sms config.
	 * 
	 * @param smsConfig
	 *            the sms config
	 */
	public void persistSmsConfig(SmsConfig smsConfig);

	/**
	 * Gets the all sms config.
	 * 
	 * @return the all sms config
	 */
	public List<SmsConfig> getAllSmsConfig();

	/**
	 * Delete sms congif.
	 * 
	 * @param smsConfig
	 *            the sms config
	 */
	public void deleteSmsConfig(SmsConfig smsConfig);

	/**
	 * Gets the sms config by sakai site id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the sms config by sakai site id
	 */
	public SmsConfig getOrCreateSmsConfigBySakaiSiteId(String sakaiSiteId);

	/**
	 * Gets the sms config by sakai tool id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the sms config by sakai tool id
	 */
	public SmsConfig getSmsConfigBySakaiToolId(String sakaiToolId);

	/**
	 * Creates a SmsConfig object with default values.
	 * 
	 * @param sakaiSiteId
	 * @return
	 */
	public SmsConfig createDefaultSmsConfig(String sakaiSiteId);

	/**
	 * Gets the system sms config.
	 * 
	 * @return
	 */
	public SmsConfig getOrCreateSystemSmsConfig();

}
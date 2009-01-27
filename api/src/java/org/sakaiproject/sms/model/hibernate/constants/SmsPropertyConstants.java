/***********************************************************************************
 * SmsHibernateConstants.java
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
package org.sakaiproject.sms.model.hibernate.constants;

/**
 * Constants class for sms properties
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 20-Jan-2009
 */
public class SmsPropertyConstants {

	/**
	 * The Constant ACCOUNT_CHECK_SITE_ID_BEFORE_USER_ID. Used when identyfying
	 * an account. Value of true will try to find the account by first using the
	 * sakai site. If not found it will try with the sakai user id.
	 * */
	public static final String ACCOUNT_CHECK_SITE_ID_BEFORE_USER_ID = "account.checkSiteIdBeforeUserId";
}

/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.sms.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

public interface SmsTaskEntityProvider extends EntityProvider {
	public final static String ENTITY_PREFIX = "sms-task";
	/**
	 * Custom action locator for method processing cost properties of a phantom task based on recipient selections
	 */
	public final static String CUSTOM_ACTION_CALCULATE = "calculate";
	/**
	 * Custom action locator for method retrieving a list of site users only with mobile numbers
	 */
	public final static String CUSTOM_ACTION_USERS = "memberships";
}

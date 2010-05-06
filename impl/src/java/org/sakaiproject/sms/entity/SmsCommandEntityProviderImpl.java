/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/sms/sms/trunk/impl/src/java/org/sakaiproject/sms/entity/SmsAccountEntityProviderImp.java $
 * $Id: SmsAccountEntityProviderImp.java 63384 2009-09-17 17:31:44Z stephen.marquard@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation
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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sms.logic.command.SmsRestCommand;

public class SmsCommandEntityProviderImpl implements SmsCommandEntityProvider,
		RESTful, AutoRegisterEntityProvider {

	private static final Log LOG = LogFactory.getLog(SmsCommandEntityProviderImpl.class);

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	private DeveloperHelperService developerHelperService;

	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage commands");	
        }
        
		// Success
		
		return "newcmd";		
	}

	/**
	 * entity methods
	 */

	public Object getSampleEntity() {
		return new SmsRestCommand();
	}

	public Object getEntity(EntityReference ref) {
		
		String id = ref.getId();
		if (id == null) {
			return new SmsRestCommand();
		}

        if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }
		
        // ShortMessageCommand cmd = RegisteredCommands.getCommand(id);

		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage commands");	
        }
		
		final String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for updates (id is currently null)");
		}
		
		
		// Update command
		
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		
        if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage commands");	
        }
		
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for deletes (id is currently null)");
		}
		
	}

	public List<?> getEntities(EntityReference ref, Search search) {

		if (!securityService.isSuperUser()) {
			throw new SecurityException("Only admin users may manage accounts");	
		}

		// Return all commands
				
		return null;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

}

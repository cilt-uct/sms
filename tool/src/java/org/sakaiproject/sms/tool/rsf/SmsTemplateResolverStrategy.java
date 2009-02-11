/***********************************************************************************
 * SmsTemplateResolverStrategy.java
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
package org.sakaiproject.sms.tool.rsf;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.tool.producers.PermissionDeniedProducer;

import uk.org.ponder.rsf.templateresolver.support.CRITemplateResolverStrategy;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.stringutil.StringList;

/**
 * Extension of {@link CRITemplateResolverStrategy} to check for administrators
 *
 */
public class SmsTemplateResolverStrategy extends CRITemplateResolverStrategy {
	
	private ExternalLogic externalLogic;
	
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	/**
	 * Checks if user is administrator, if not forward to PermissionDeniedProducer
	 */
	@Override
	public StringList resolveTemplatePath(ViewParameters viewparams) {
		if (!viewparams.viewID.equals(PermissionDeniedProducer.VIEW_ID) && externalLogic.isUserAdmin(externalLogic.getCurrentUserId())) {
			return super.resolveTemplatePath(viewparams);	
		} else {
			// TODO: Go to screen
			viewparams.viewID = PermissionDeniedProducer.VIEW_ID;
			return super.resolveTemplatePath(viewparams);
		}
	}

}

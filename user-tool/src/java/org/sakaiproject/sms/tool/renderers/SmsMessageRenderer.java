/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.util.DateUtil;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

public class SmsMessageRenderer {
	
	private DateUtil dateUtil;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}
	
	public void renderMessage(SmsTask smsTask, UIContainer tofill, String parentDiv){
		UIJointContainer message = new UIJointContainer(tofill, parentDiv, "renderMessage:");
		UIMessage.make(message, "message-title", "ui.sent.sms.title");
		UIOutput.make(message, "message", smsTask.getMessageBody())
		//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
				.decorate(new UIFreeAttributeDecorator("rel", smsTask.getId().toString()));
		UIMessage.make(message, "sms-id", "ui.sent.sms.id", new Object[] { smsTask.getId() });
		UIMessage.make(message, "sms-created", "ui.sent.sms.created", new Object[] { dateUtil.formatDate(smsTask.getDateCreated()) });
	}
}

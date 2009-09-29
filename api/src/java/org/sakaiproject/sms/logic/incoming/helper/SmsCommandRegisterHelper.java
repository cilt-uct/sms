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
package org.sakaiproject.sms.logic.incoming.helper;

import java.util.List;

import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;

public class SmsCommandRegisterHelper {
	
	private String toolKey;
	private SmsIncomingLogicManager incomingLogicManager;
	private List<ShortMessageCommand> commands;
	
	public void init() {
		if (incomingLogicManager == null) {
			throw new IllegalStateException("SmsIncomingLogicManager must be set");
		}
		if (toolKey == null) {
			throw new IllegalStateException("ToolKey must be set");
		}
			
		incomingLogicManager.clearCommands(toolKey);
		for (ShortMessageCommand command : commands) {
			incomingLogicManager.register(toolKey, command);
		}
	}
	
	public void setToolKey(String toolKey) {
		this.toolKey = toolKey;
	}
	
	public void setIncomingLogicManager(
			SmsIncomingLogicManager incomingLogicManager) {
		this.incomingLogicManager = incomingLogicManager;
	}
	public void setCommands(List<ShortMessageCommand> commands) {
		this.commands = commands;
	}
	
	
}

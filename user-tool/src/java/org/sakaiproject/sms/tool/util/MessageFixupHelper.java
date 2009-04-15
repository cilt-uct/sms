/***********************************************************************************
 * MessageFixupHelper.java
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
package org.sakaiproject.sms.tool.util;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class MessageFixupHelper {

	private TargettedMessageList messages;

	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	/**
	 * If current id is on the list of ids to fix
	 * 
	 * @param currentid
	 * @param targetids
	 * @return if current if must be fixed
	 */
	private boolean mustFix(String currentid, String... idsToFix) {
		if (currentid == null) {
			return false;
		}
		for (String idToFix : idsToFix) {
			if (currentid.equals(idToFix)) {
				return true;
			}
		}
		return false;
	}

	// DataConverter still tries to bind invalid number to bean which causes
	// another message on list. This is to remove the extra messages.
	public void fixupMessages(String... idsToFix) {
		if (messages.size() > 1) {
			for (int i = 1; i < messages.size(); i++) {
				TargettedMessage message = messages.messageAt(i);
				// If the message is a UniversalRuntimeException for one of the
				// numeric fields
				if (message.args != null && mustFix(message.targetid, idsToFix)) {
					messages.removeMessageAt(i);
					i--;
				}
			}
		}
	}
}

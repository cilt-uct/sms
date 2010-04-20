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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsTask;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;

public class SavedSelectionsRenderer {

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void renderSelections(SmsTask smsTask, UIContainer tofill,
			String parentDiv) {
		UIJointContainer parent = new UIJointContainer(tofill, parentDiv,
				"renderSelections:");

		UIMessage.make(parent, "recipients", "ui.failed.sms.recipients",
				new Object[] { smsTask.getGroupSizeEstimate() });

		// Insert original user selections
		List<String> smsEntities = smsTask.getDeliveryEntityList();

		List<String> groups = new ArrayList<String>();
		List<String> roles = new ArrayList<String>();
		if (smsEntities != null && smsEntities.size() > 0) {
			for (String entity : smsEntities) {
				// in the format /site/123/role/something
				if ("site".equals(externalLogic.getEntityPrefix(entity))
						&& externalLogic.getEntityRealIdFromRefByKey(entity,
								"role") != null) {
					roles.add(externalLogic.getEntityRealIdFromRefByKey(entity,
							"role"));
				} else if ("site".equals(externalLogic.getEntityPrefix(entity))
						&& externalLogic.getEntityRealIdFromRefByKey(entity,
								"group") != null) {
					groups.add(externalLogic.getSakaiGroupNameFromId(
							externalLogic.getEntityRealIdFromRefByKey(entity,
									"site"), externalLogic
									.getEntityRealIdFromRefByKey(entity,
											"group")));
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		int count = 1;
		if (roles.size() > 0) {
			for (String role : roles) {
				if (role != null) {
					sb.append(role);
					if (count != roles.size()) {
						sb.append(", ");
					}
					count++;
				}
			}
			if (!"".equals(sb.toString())) {
				UIMessage.make(parent, "selections1",
						"ui.inprogress.selections.roles",
						new String[] { sb.toString() + "."});
			}
		}
		count = 1;
		sb.setLength(0);
		if (groups.size() > 0) {
			for (String group : groups) {
				sb.append(group);
				if (count < groups.size()) {
					sb.append(", ");
				}
				count++;
			}
			if (!"".equals(sb.toString())) {
				UIMessage.make(parent, "selections2",
						"ui.inprogress.selections.groups",
						new String[] { sb.toString() + "."});
			}
		}

		Set<String> sakaiUserIds = smsTask.getSakaiUserIdsList();
		count = 1;
		sb.setLength(0);
		if (sakaiUserIds != null && sakaiUserIds.size() > 0) {
			Map<String, String> sortNamesMap = externalLogic.getSakaiUserDisplayNames(sakaiUserIds);
			Collection<String> sortNames = sortNamesMap.values();
			for (String name : sortNames) {
				sb.append(name);
				if (count != sakaiUserIds.size()) {
					sb.append("; ");
				}
				count++;
			}
			if (!"".equals(sb.toString())) {
				UIMessage.make(parent, "selections3",
						"ui.inprogress.selections.names",
						new String[] { sb.toString() + "."});
			}
		}

		Set<String> numbers = smsTask.getDeliveryMobileNumbersSet();
		count = 1;
		sb.setLength(0);
		if (numbers != null && numbers.size() > 0) {
			for (String num : numbers) {
				sb.append(num);
				if (count != numbers.size()) {
					sb.append(", ");
				}
				count++;
			}
			if (!"".equals(sb.toString())) {
				UIMessage.make(parent, "selections4",
						"ui.inprogress.selections.numbers",
						new String[] { sb.toString() + "."});
			}
		}
		sb.setLength(0);
	}
}

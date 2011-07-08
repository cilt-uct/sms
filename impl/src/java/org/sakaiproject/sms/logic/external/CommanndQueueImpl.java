/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 The Sakai Foundation
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

package org.sakaiproject.sms.logic.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

public class CommanndQueueImpl implements SmsCommandQueue {
	
	private Map<ShortMessageCommand, CommandInfo> queue = new HashMap<ShortMessageCommand, CommandInfo>();
	
	private int maxTries = 100;
	private int pauseMinutes = 5;
	
	
	public void addUpdateCommand(ShortMessageCommand command) {
		CommandInfo info = null;
		if (queue.containsKey(command)) {
			info = queue.get(command);
		} else {
			info = new CommandInfo();
			info.setAttempts(0);
		}
		
		info.setLastTry(new DateTime());
		info.setNextTry(new DateTime().plusMinutes(pauseMinutes));
		info.setAttempts(info.getAttempts() + 1);
		
		//remove the object
		removeSmsCommand(command);
		
		queue.put(command, info);
	}

	public List<ShortMessageCommand> getCommandsAwaitingDelivery() {
		List<ShortMessageCommand> ret = new ArrayList<ShortMessageCommand>();
		
		Iterator<Entry<ShortMessageCommand, CommandInfo>> entries = queue.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<ShortMessageCommand, CommandInfo> entry = entries.next();
			ShortMessageCommand command = entry.getKey();
			CommandInfo info = entry.getValue();
			
			//if tries is more than max remove it from the list
			if (info.getAttempts() >= maxTries) {
				removeSmsCommand(command);
				continue;
			}
			
			
			if (info.getNextTry().isBefore(new DateTime())) {
				ret.add(command);
			}
			
		}
			
		
		return ret;
	}

	public void removeSmsCommand(ShortMessageCommand command) {
		queue.remove(command);
		
	}
	
	
	private static class CommandInfo {
		private DateTime lastTry;
		private DateTime nextTry;
		private int attempts;
		
		public DateTime getLastTry() {
			return lastTry;
		}
		public void setLastTry(DateTime lasyTry) {
			this.lastTry = lasyTry;
		}
		public DateTime getNextTry() {
			return nextTry;
		}
		public void setNextTry(DateTime nextTry) {
			this.nextTry = nextTry;
		}
		public int getAttempts() {
			return attempts;
		}
		public void setAttempts(int attempts) {
			this.attempts = attempts;
		}
		
	}

}

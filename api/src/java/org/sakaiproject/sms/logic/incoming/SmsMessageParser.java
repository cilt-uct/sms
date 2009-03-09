/***********************************************************************************
 * SmsMessageParser.java
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
package org.sakaiproject.sms.logic.incoming;

import org.sakaiproject.sms.logic.parser.exception.ParseException;

/**
 * This Class will handle all validation and parsing of MO(Mobile Originating)
 * messages Tool specific parsing and validation will be done from the tool
 * itself. A typical command might be: QNA SITE1 POST The message to post
 *
 * To use this class: (1) instantiate with a SmsTask, (2) call
 * parseMessageGeneral (3) call validateMessageGeneral
 *
 *
 * @author wilhelm@psybergate.co.za, louis@psybergate.co.za
 */
public interface SmsMessageParser {

	/**
	 * Parses the text of the message. Try to figure out the sakai site and user.
	 * Usually called from the Sakai Sms service itself.
	 * 
	 * FORMAT: <tool> <site> <command> <body>
	 */
	public ParsedMessage parseMessage(String msgText) throws ParseException;

}

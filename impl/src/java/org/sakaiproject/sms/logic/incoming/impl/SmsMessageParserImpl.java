/***********************************************************************************
 * SmsMessageParserImpl.java
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
package org.sakaiproject.sms.logic.incoming.impl;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.parser.exception.ParseException;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;

public class SmsMessageParserImpl implements SmsMessageParser {

	private static final String DELIMITERS = " \t\r\n\f";

	/**
	 * Parses the message general. FORMAT: <command> <site> <body>
	 */
	public ParsedMessage parseMessage(String msgText) throws ParseException {
		if (msgText == null) {
			throw new ParseException("null message supplied");
		}

		String[] params = StringUtils.split(msgText, DELIMITERS, 3);

		// Must at least contain command + site (except for HELP command)
		if (params.length < 2
				&& !(params.length == 1 && params[0]
						.equalsIgnoreCase(SmsConstants.HELP))) {
			throw new ParseException("Invalid number of tokens: "
					+ params.length);
		}

		if (params.length == 1) {
			return new ParsedMessage(params[0]);
		} else if (params.length == 2) {
			// command + site
			return new ParsedMessage(params[0], params[1]);
		} else {
			// command + site + body
			return new ParsedMessage(params[0], params[1], params[2]);

		}

	}

}

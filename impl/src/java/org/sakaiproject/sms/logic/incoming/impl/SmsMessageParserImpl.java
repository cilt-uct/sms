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

public class SmsMessageParserImpl implements SmsMessageParser {

	private static final String DELIMITERS = " \t\r\n\f";

	// 1st parameter
	public void parseCommand(ParsedMessage message) throws ParseException {
		
		String msgText = message.getBody();
		
		if (msgText == null) {
			throw new ParseException("null message supplied");
		}
		
		if ("".equals(msgText.trim())) {
			throw new ParseException("empty message supplied");
		}

		final String[] params = StringUtils.split(msgText, DELIMITERS, 2);
		
		// SMS-216 Trim leading spaces or punctuation

		int cmdstart = 0;
		
		while (cmdstart < params[0].length() && !Character.isLetterOrDigit(params[0].charAt(cmdstart))) {
			cmdstart++;
		}
		
		message.setCommand(params[0].substring(cmdstart));
		
		return;
	}

	// 2nd parameter (optional)
	public void parseSite(ParsedMessage message) throws ParseException {
		
		final String[] params = StringUtils.split(message.getBody(), DELIMITERS, 3);

		if (params.length >= 2) {
			message.setSite(params[1]);
		} else {
			throw new ParseException("no site found");
		}
	}

	// 2nd or 3rd parameter on, depending on presence of site
	public void parseBody(ParsedMessage message, int parameterCount, boolean requiresSiteId)
			throws ParseException {

		String text = message.getBody();
		
		if (text == null && parameterCount != 0) {
			throw new ParseException("No body specified");
		}
		
		if (text == null && parameterCount == 0) {
			// no parameters expected
			message.setBodyParameters(new String[0]);
			return;
		}
		
		if (text != null) {
			text = text.trim();
		}
		
		if (parameterCount == 0 && !"".equals(text.trim())) {
			throw new ParseException("No parameters expected");
		}

		// Eliminate the command and site id if present
		int partCount = requiresSiteId ? 3 : 2;
		final String[] preParam = StringUtils.split(text, DELIMITERS, partCount);
		
		if (preParam.length != partCount) {
			throw new ParseException(parameterCount
					+ " parameters expected but none found");			
		}
		
		String params = preParam[preParam.length-1];
		
		final String[] bodyParams = StringUtils.split(params, DELIMITERS, parameterCount);
		
		if (bodyParams.length < parameterCount) {
			throw new ParseException(parameterCount
					+ " parameters expected but " + bodyParams.length
					+ " received");
		}

		message.setBodyParameters(bodyParams);

		return;
	}

}

/**********************************************************************************
 * $URL: $
 * $Id: $
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
package org.sakaiproject.sms.logic.smpp.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.sakaiproject.sms.model.constants.SmsConstants;

/**
 * Tool for obtaining mesages from message bundles
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 15-Jan-2009
 */
public abstract class MessageCatalog {

	/**
	 * Gets the message.
	 * 
	 * @param messageKey
	 *            the message key
	 * 
	 * @return the message
	 */
	public static String getMessage(String messageKey) {
		return getMessage(messageKey, (String[]) null);
	}

	/**
	 * Gets the message.
	 * 
	 * @param messageKey
	 *            the message key
	 * @param parameters
	 *            the parameters
	 * 
	 * @return the message
	 */
	public static String getMessage(String messageKey, String... parameters) {
		return get(messageKey, parameters);
	}

	/**
	 * Gets the.
	 * 
	 * @param key
	 *            the key
	 * @param parameters
	 *            the parameters
	 * 
	 * @return the string
	 */
	private static String get(String key, Object[] parameters) {
		final String message = get(key);
		if ((message == null) || (parameters == null)
				|| (parameters.length == 0)) {
			return message;
		} else {
			return MessageFormat.format(message, parameters);
		}
	}

	/**
	 * Gets the.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the string
	 */
	private static String get(String key) {
		final String bundleName = key.substring(0, key.indexOf('.'));
		final String messageKey = key.substring(key.indexOf('.') + 1);
		String message = null;
		final ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
		if (bundle != null) {
			try {
				message = bundle.getString(messageKey);
			} catch (MissingResourceException ex) {
				message = SmsConstants.CATALOG_MESSAGE_NOT_FOUND;
			}
		}
		return message;
	}

}

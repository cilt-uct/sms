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
package org.sakaiproject.sms.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.model.constants.SmsConstants;

/**
 * Util class for reading properties from property files
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 20-Jan-2009
 */
public abstract class SmsPropertyReader {

	private static final Log LOG = LogFactory.getLog(SmsPropertyReader.class);

	/** The property file name. */
	private static Properties properties = new Properties();

	/**
	 * Gets the property.
	 * 
	 * @param propertyKey
	 *            the property key
	 * 
	 * @return the property
	 */
	public static String getProperty(String propertyKey) {

		try {
			loadProperties();
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
			return SmsConstants.PROPERTY_FILE_NOT_FOUND;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return SmsConstants.PROPERTY_FILE_NOT_FOUND;
		}

		return properties.getProperty(propertyKey,
				SmsConstants.PROPERTY_NOT_FOUND);
	}

	/**
	 * Load properties.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void loadProperties() throws FileNotFoundException,
			IOException {
		final InputStream inputStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("sms.properties");

		if (inputStream == null) {
			final FileInputStream fileInputStream = new FileInputStream(
					"smpp.properties");
			properties.load(fileInputStream);

			if (fileInputStream != null) {
				fileInputStream.close();
			}
		} else {
			properties.load(inputStream);
			inputStream.close();
		}

	}
}

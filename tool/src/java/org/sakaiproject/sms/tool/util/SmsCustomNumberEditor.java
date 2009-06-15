/***********************************************************************************
 * SmsCustomNumberEditor.java
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

import org.springframework.beans.propertyeditors.CustomNumberEditor;

/**
 * Modifies Spring's {@link CustomNumberEditor} to set value as null if
 * whitespace and throw specific message with exception
 * 
 * @see {@link CustomNumberEditor}
 * 
 */
public class SmsCustomNumberEditor extends CustomNumberEditor {

	private final String field;
	private final Class<? extends Number> numberClass;

	/**
	 * Constructor sets NumberClass as {@link Float}
	 * 
	 * @param allowEmpty
	 *            if null values are allowed
	 * @throws IllegalArgumentException
	 */
	public SmsCustomNumberEditor(Class<? extends Number> numberClass,
			boolean allowEmpty, String field) throws IllegalArgumentException {
		super(numberClass, allowEmpty);
		this.field = field;
		this.numberClass = numberClass;

	}

	/**
	 * Set value as null if exception is thrown when parsing
	 * 
	 * @see CustomNumberEditor#setAsText(String)
	 */
	@Override
	public void setAsText(String text) {
		if (text == null) {
			super.setAsText(null);
		} else if ("".equals(text.trim())) {
			super.setAsText(null);
		} else {
			try {
				Number value;
				if (Float.class.equals(numberClass)) {
					value = Float.valueOf(text);
				} else if (Double.class.equals(numberClass)) {
					value = Double.valueOf(text);
				} else if (Integer.class.equals(numberClass)) {
					value = Integer.valueOf(text);
				} else {
					// Default to Float
					value = Float.valueOf(text);
				}
				super.setValue(value);
			} catch (NumberFormatException e) {
				// Throw specific message
				throw new NumberFormatException("sms.errors." + field
						+ ".invalid");
			}
		}
	}
}
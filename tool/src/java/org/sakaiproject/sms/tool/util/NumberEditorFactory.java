/***********************************************************************************
 * FloatEditorFactory.java
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

import java.beans.PropertyEditor;

import uk.org.ponder.mapping.PropertyEditorFactory;

/**
 * Factory class to return PropertyEditor for Float values
 * 
 */
public class NumberEditorFactory implements PropertyEditorFactory {

	private String field;
	private Class<? extends Number> numberClass;

	public PropertyEditor getPropertyEditor() {
		if (numberClass == null) {
			numberClass = Float.class;
		}
		return new SmsCustomNumberEditor(numberClass, true, field);
	}

	public void setField(String field) {
		this.field = field;
	}

	/**
	 * NumberClass to set. Defaults to Float
	 * 
	 * @param numberClass
	 */
	@SuppressWarnings("unchecked")
	public void setNumberClass(String numberClass) {
		try {
			this.numberClass = (Class<? extends Number>) Class
					.forName(numberClass);
		} catch (ClassNotFoundException e) {
			this.numberClass = Float.class;
		}

	}
}

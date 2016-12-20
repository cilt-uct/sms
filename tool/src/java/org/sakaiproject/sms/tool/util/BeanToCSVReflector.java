/***********************************************************************************
 * BeanToCSVReflector.java
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;

/**
 * A Utility class to convert a List of java beans to a CSV list, does not fetch
 * nested objects
 * 
 */
public class BeanToCSVReflector {

	private static Log log = LogFactory.getLog(BeanToCSVReflector.class);
	private static final String NOT_FOUND = "N/A";
	private static final String IS = "is";
	private static final String GET = "get";
	private ExternalLogic externalLogic;
	
	


	/**
	 * Returns a CSV list of all the java accessors
	 * 
	 * @param list
	 *            ist a list of objects
	 * @return a string containing a CSV report
	 */
	public String toCSV(List<?> list) {
		return toCSV(list, (String[]) null);
	}

	/**
	 * Returns a CSV list of only the specified field
	 * 
	 * @param list
	 *            a list of objects
	 * @param fieldNames
	 *            the Fields to reflect on
	 * @return a string containing a CSV report
	 */
	public String toCSV(List<?> list, String[] fieldNames) {

		StringBuffer buffer = new StringBuffer();

		if (list.isEmpty()) {
			return "There is no data for this report";
		}

		Object objectToReflectOn = list.get(0);
		Method[] methodArray = getAllPublicAccesorMethods(objectToReflectOn);
		methodArray = createOrderedMethodArray(methodArray, fieldNames);

		//create
		for (int i = 0; i < methodArray.length; i++) {
			String fieldName = convertMethodNameToFieldName(methodArray[i]
					.getName());

			if (i == methodArray.length - 1) {
				buffer.append(fieldName + "\n");
			} else {
				buffer.append(fieldName + ", ");
			}
		}

		for (Object object : list) {
			for (int i = 0; i < methodArray.length; i++) {
				try {
					String value = NOT_FOUND;
					Object fieldValue = methodArray[i].invoke(object);

					if (fieldValue != null) {
						value = fieldValue.toString();
						//this may need conversion or lookup (e.g id to eid conversion)
						value = convertFieldValue(methodArray[i], value);
						String escape = value.contains(",") ? "\"" : "";
						value = escape
								+ StringUtils.replace(value, "\"", "\"\"")
								+ escape;
					}
					if (i == methodArray.length - 1) {
						buffer.append(value + "\n");
					} else {
						buffer.append(value + ",");
					}

				} catch (Exception e) {
					throw new RuntimeException(
							"Failed to obtain value from method "
									+ methodArray[i].toString() + " cause "
									+ e.toString(), e);
				}
			}
		}

		return buffer.toString();
	}

	private String convertFieldValue(Method method, String value) {
		log.trace("got method "  + method.getName() + " for value: " + value);
		if (externalLogic == null ) {
			log.error("external logic is null!");
		} else {
			String methodName = method.getName();
			if ("getSakaiUserId".equals(methodName)) {
				return externalLogic.getUserEidFromId(value);
			}
		}
		return value;
	}

	private Method[] getAllPublicAccesorMethods(Object objectToReflectOn) {

		Method[] allMethods = objectToReflectOn.getClass().getMethods();
		ArrayList<Method> accesorMethods = new ArrayList<Method>();

		for (int i = 0; i < allMethods.length; i++) {

			String methodName = allMethods[i].getName();

			if (methodName.startsWith(GET))
				accesorMethods.add(allMethods[i]);
			if (methodName.startsWith(IS))
				accesorMethods.add(allMethods[i]);
		}

		Method[] methodList = new Method[accesorMethods.size()];

		for (int i = 0; i < methodList.length; i++) {
			methodList[i] = accesorMethods.get(i);
		}

		return methodList;
	}

	private Method[] createOrderedMethodArray(Method[] methodArray,
			String[] fieldNames) {

		if (fieldNames == null) {
			return methodArray;
		}

		Map<String, Method> methodMap = new TreeMap<String, Method>();
		for (int i = 0; i < methodArray.length; i++) {

			String methodName = methodArray[i].getName();
			String fieldName = convertMethodNameToFieldName(methodName);
			methodMap.put(fieldName, methodArray[i]);
		}

		Method[] methodsToInvoke = new Method[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {

			Method tempMethod = methodMap.get(fieldNames[i]);

			if (tempMethod == null) {
				throw new RuntimeException("The Field value [" + fieldNames[i]
						+ "] does not exsist in the supplied Object List");
			}

			methodsToInvoke[i] = tempMethod;
		}

		return methodsToInvoke;
	}

	private String convertMethodNameToFieldName(String methodName) {
		// remove "get" or "is"
		if (methodName.startsWith(GET) && methodName.length() > 0)
			methodName = methodName.substring(3);
		if (methodName.startsWith(IS) && methodName.length() > 0)
			methodName = methodName.substring(2);

		if (methodName.length() >= 2)
			methodName = methodName.substring(0, 1).toLowerCase()
					+ methodName.substring(1);
		else
			methodName = methodName.toLowerCase();
		return methodName;
	}

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	
}

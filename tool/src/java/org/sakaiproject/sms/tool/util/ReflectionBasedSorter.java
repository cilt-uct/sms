/***********************************************************************************
 * ReflectionBasedSorter.java
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.sakaiproject.sms.tool.constants.SortDirection;

public class ReflectionBasedSorter {

	public static <T> List<T> sortByName(List<T> list, final String fieldToSort, final SortDirection sortDirection){

		Collections.sort(list, new Comparator<T>(){

			public int compare(T o1, T o2) {
				
				Object valueLeft, valueRight;
				try {
					valueLeft = BeanUtils.getProperty(o1, fieldToSort);
					valueRight = BeanUtils.getProperty(o2, fieldToSort);
				} 
				catch (Exception e) {
					throw new RuntimeException(e);
				}
						
				Integer compare;
				try {
					
					if(sortDirection == SortDirection.ASC)
						compare = (Integer) MethodUtils.invokeExactMethod(valueLeft, "compareTo", valueRight);
					else
						compare = (Integer) MethodUtils.invokeExactMethod(valueRight, "compareTo", valueLeft);
						
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				return compare.intValue();
			}	
		});

		return list;
	}
	
}

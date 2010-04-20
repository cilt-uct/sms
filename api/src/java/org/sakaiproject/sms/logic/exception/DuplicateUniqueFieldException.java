/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.sms.logic.exception;

public class DuplicateUniqueFieldException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Unique field on which duplicate was fount
	 */
	private final String field;

	/**
	 * Instantiates a DuplicateUniqueFieldException
	 * 
	 * @param msg
	 *            the msg
	 */
	public DuplicateUniqueFieldException(String field) {
		super(field + " should be unique");
		this.field = field;
	}

	/**
	 * Instantiates a DuplicateUniqueFieldException
	 * 
	 * @param e
	 *            the exception
	 */
	public DuplicateUniqueFieldException(Exception e, String field) {
		super(e);
		this.field = field;
	}

	/**
	 * Returns the field which caused the DuplicateUniqueFieldException
	 * 
	 * @return the field
	 */
	public String getField() {
		return field;
	}

}

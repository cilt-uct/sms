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
package org.sakaiproject.sms.logic;

import org.hibernate.HibernateException;
import org.sakaiproject.sms.dao.SmsDao;
import org.sakaiproject.sms.model.BaseModel;

/**
 * Base logic to retrieve from db
 */
abstract public class BaseLogic {
	
	protected SmsDao smsDao;
	
	public void setSmsDao(SmsDao smsDao) {
		this.smsDao = smsDao;
	}
	
	/**
	 * Persists the given instance to the database. The save or update operation
	 * is done over a transaction. In case of any errors the transaction is
	 * rolled back.
	 * 
	 * @param object
	 *            object instance to be persisted
	 * @exception HibernateException
	 *                if any error occurs while saving or updating data in the
	 *                database
	 */
	protected void persist(BaseModel object) throws HibernateException {
		smsDao.save(object);
	}

	/**
	 * Deletes the given instance from the database.
	 * 
	 * @exception HibernateException
	 *                if any error occurs while saving or updating data in the
	 *                database
	 */
	protected void delete(Object object) throws HibernateException {
		smsDao.delete(object);
	}

	/**
	 * Find by id.
	 * 
	 * @param className
	 *            the class name
	 * @param id
	 *            the id
	 * 
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	protected Object findById(Class className, Long id) {
		return smsDao.findById(className, id);
	}
	
	
}
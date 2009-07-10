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
package org.sakaiproject.sms.dao;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;

public interface SmsDao extends GeneralGenericDao {

	public List runQuery(String hql, QueryParameter... queryParameters);

	public int executeUpdate(String hql, Collection<Object> values);

	public int executeUpdate(String hql, Object value);

	/*
	 * public Criteria createCriteria(Class className); public void save(Object
	 * obj);
	 * 
	 * public void delete(Object obj);
	 * 
	 * public Object findById(Class className, Serializable id);
	 */
}

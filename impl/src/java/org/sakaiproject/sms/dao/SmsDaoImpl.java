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
package org.sakaiproject.sms.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.sms.logic.QueryParameter;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SmsDaoImpl extends HibernateGeneralGenericDao implements SmsDao {

	private PlatformTransactionManager transactionManager = null;

	private final DefaultTransactionDefinition defaultTransDefinition = new DefaultTransactionDefinition(
			TransactionDefinition.PROPAGATION_REQUIRED);

	private final DefaultTransactionDefinition readOnlyDefaultTransDefinition = new DefaultTransactionDefinition(
			defaultTransDefinition);

	public SmsDaoImpl() {
		readOnlyDefaultTransDefinition.setReadOnly(true);
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@SuppressWarnings("unchecked")
	public List executeQuery(String hql, Object[] params, int start, int limit) {
		return executeHqlQuery(hql, params, start, limit);

	}

	@SuppressWarnings("unchecked")
	public List runQuery(final String hql, QueryParameter... queryParameters) {

		Map<String, Object> paramsMap = paramToMap(queryParameters);
		final List retrieved = executeHqlQuery(hql, paramsMap, 0, 0);
		return retrieved;
	}

	private Map<String, Object> paramToMap(QueryParameter[] queryParameters) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (QueryParameter queryParameter : queryParameters) {
			ret.put(queryParameter.name, queryParameter.val);
		}
		return ret;
	}

	public int executeUpdate(String hql, Collection<Object> values) {
		return getHibernateTemplate().bulkUpdate(hql, values.toArray());
	}

	@SuppressWarnings("unused")
	private Query buildQuery(String hql, QueryParameter... queryParameters) {
		Query query = getSession().createQuery(hql);

		for (QueryParameter queryParameter : queryParameters) {
			if (queryParameter.val instanceof Object[]) {
				query.setParameterList(queryParameter.name,
						(Object[]) queryParameter.val, queryParameter.type);
			} else {
				query.setParameter(queryParameter.name, queryParameter.val,
						queryParameter.type);
			}

		}
		return query;
	}

	@SuppressWarnings("unused")
	private void rollback(TransactionStatus transaction) {
		if (!transaction.isCompleted()) {
			transactionManager.rollback(transaction);
		}
	}

	public int executeUpdate(String hql, Object value) {
		return getHibernateTemplate().bulkUpdate(hql, value);

	}

	public HibernateTemplate getTheHibernateTemplate() {
		return getHibernateTemplate();

	}

}

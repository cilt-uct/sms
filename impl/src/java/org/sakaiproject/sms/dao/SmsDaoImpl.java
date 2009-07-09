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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SmsDaoImpl extends HibernateGeneralGenericDao implements SmsDao {

	private final static Log LOG = LogFactory.getLog(SmsDaoImpl.class);
	
	private PlatformTransactionManager transactionManager;

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


	public List runQuery(final String hql, QueryParameter... queryParameters) {
				
		Map<String, Object> paramsMap = paramToMap(queryParameters);
		final List retrieved = executeHqlQuery(hql, paramsMap, 0, 0);
		return retrieved;
	}

	private Map<String, Object> paramToMap(QueryParameter[] queryParameters) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (QueryParameter queryParameter : queryParameters) {
			if (queryParameter.val instanceof Object[]) {
				ret.put(queryParameter.name, queryParameter.val);
			} else {
				ret.put(queryParameter.name, queryParameter.val);
			}

		}
		return ret;
	}

	public int executeUpdate(String hql, QueryParameter... queryParameters) {
		Map<String, Object> paramsMap = paramToMap(queryParameters);
		
		int affected = 0;
		
		if (paramsMap.size() > 1) {
			Collection<Object> values = paramsMap.values();
			System.out.println("we got: " + values.toArray().length + " paramaters");
			affected = getHibernateTemplate().bulkUpdate(hql, values.toArray());
		} else {
			System.out.println("updating task: " + paramsMap.get(0));
			affected = getHibernateTemplate().bulkUpdate(hql, paramsMap.get(0));
		}
		return affected;
	}

	public int executeUpdate(String hql, Object queryParameter) {
		System.out.println(hql);
		return getHibernateTemplate().bulkUpdate(hql, queryParameter);
	}




	
	/*

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






	private void rollback(TransactionStatus transaction) {
		if (!transaction.isCompleted()) {
			transactionManager.rollback(transaction);
		}
	}
	
	public Criteria createCriteria(Class className) {
		return getSession().createCriteria(className);
	}
	@Override
	public void save(final Object obj) {
		final TransactionStatus transaction = transactionManager
				.getTransaction(defaultTransDefinition);
		try {
			super.save(obj);
			transactionManager.commit(transaction);
		} catch (HibernateException ex) {
			LOG.error(ex.getMessage(), ex);
			rollback(transaction);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			rollback(transaction);
		}
	}
	
	@Override
	public Object findById(Class className, Serializable id) {
		return super.findById(className, id);
	}
	
	
	@Override
	public void delete(final Object obj) {
		final TransactionStatus transaction = transactionManager
				.getTransaction(defaultTransDefinition);
		try {
			super.delete(obj);
			transactionManager.commit(transaction);
		} catch (HibernateException ex) {
			LOG.error(ex.getMessage(), ex);
			rollback(transaction);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			rollback(transaction);
		}
	}

	*/
}

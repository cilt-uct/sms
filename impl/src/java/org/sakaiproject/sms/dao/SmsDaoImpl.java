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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@SuppressWarnings("unchecked")
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

	public List runQuery(final String hql, QueryParameter... queryParameters) {
		final TransactionStatus transaction = transactionManager
				.getTransaction(readOnlyDefaultTransDefinition);

		Query query = buildQuery(hql, queryParameters);
		final List retrieved = query.list();
		transactionManager.commit(transaction);
		return retrieved;
	}

	public int executeUpdate(String hql, QueryParameter... queryParameters) {
		TransactionStatus transaction = transactionManager
				.getTransaction(defaultTransDefinition);
		int affected = 0;
		try {
			Query query = buildQuery(hql, queryParameters);
			affected = query.executeUpdate();
			transactionManager.commit(transaction);
		} catch (HibernateException ex) {
			LOG.error(ex.getMessage(), ex);
			rollback(transaction);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			rollback(transaction);
		}

		return affected;
	}

	@Override
	public Object findById(Class className, Serializable id) {
		return super.findById(className, id);
	}

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

	public Criteria createCriteria(Class className) {
		return getSession().createCriteria(className);
	}

	private void rollback(TransactionStatus transaction) {
		if (!transaction.isCompleted()) {
			transactionManager.rollback(transaction);
		}
	}

}

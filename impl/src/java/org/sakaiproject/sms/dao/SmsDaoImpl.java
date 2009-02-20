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
	
	private static Log LOG = LogFactory.getLog(SmsDaoImpl.class);

	private PlatformTransactionManager transactionManager;
	
	private final DefaultTransactionDefinition defaultTransDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public void save(Object obj) {
		TransactionStatus transaction = transactionManager.getTransaction(defaultTransDefinition);
		try {
			super.save(obj);
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
			transactionManager.rollback(transaction);
		}
		
		transactionManager.commit(transaction);
	}

	@Override
	public void delete(Object obj) {
		TransactionStatus transaction = transactionManager.getTransaction(defaultTransDefinition);
		try {
			super.delete(obj);
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
			transactionManager.rollback(transaction);
		}
		transactionManager.commit(transaction);
		
	}

	public List runQuery(String hql, QueryParameter... queryParameters) {
		TransactionStatus transaction = transactionManager.getTransaction(defaultTransDefinition); 
		Query query = buildQuery(hql, queryParameters);
		List retrieved = query.list();
		transactionManager.commit(transaction);
		return retrieved;
	}
	
	public int executeUpdate(String hql, QueryParameter... queryParameters) {
		TransactionStatus transaction = transactionManager.getTransaction(defaultTransDefinition);
		int affected = 0;
		try {
			Query query = buildQuery(hql, queryParameters);
			affected = query.executeUpdate();
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
			transactionManager.rollback(transaction);
		}
		transactionManager.commit(transaction);
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
				query.setParameterList(queryParameter.name, (Object[])queryParameter.val, queryParameter.type);
			} else {
				query.setParameter(queryParameter.name, queryParameter.val, queryParameter.type);	
			}
			
			
		}
		return query;
	}

	public Criteria createCriteria(Class className) {
		return getSession().createCriteria(className);
	}

}

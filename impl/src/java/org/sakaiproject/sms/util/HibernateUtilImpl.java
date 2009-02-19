package org.sakaiproject.sms.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class HibernateUtilImpl implements HibernateUtil {
	
	private static Log LOG = LogFactory.getLog(HibernateUtilImpl.class);

	private SessionFactory sessionFactory;
	
	private PlatformTransactionManager transactionManager;
	
	/**
	 * Container for thread-scoped sessions.
	 */
	private final static ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
	
	private final static ThreadLocal<TransactionStatus> threadTransactionStatus = new ThreadLocal<TransactionStatus>();
	
	public void beginTransaction() {
		TransactionStatus ts = threadTransactionStatus.get();
		if (ts == null) {
			TransactionStatus trans = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
			threadTransactionStatus.set(trans);	
		}
		
	}

	public void commitTransaction() {
		TransactionStatus ts = threadTransactionStatus.get();
		try {
			if (ts != null && !ts.isCompleted() && !ts.isRollbackOnly()) {
				transactionManager.commit(ts);
			}
			threadTransactionStatus.set(null);
		} catch (HibernateException ex) {
			rollbackTransaction();
			LOG.error("HibernateException: " + ex);
		}
	}

	public Session getSession() {
		Session s = threadSession.get();
		if (s == null) {
			s = sessionFactory.openSession();
			threadSession.set(s);
		}
		return s;
	
	}

	public void rollbackTransaction() {
		TransactionStatus ts = threadTransactionStatus.get();
		try {
			threadTransactionStatus.set(null);
			if (ts != null && !ts.isCompleted()) {
				ts.setRollbackOnly();
			}
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		} finally {
			closeSession();
		}
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void closeSession() {
		Session s = threadSession.get();
		threadSession.set(null);
		if (s != null && s.isOpen()) {
			s.close();
		}
	}
}

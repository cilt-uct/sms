package org.sakaiproject.sms.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class HibernateUtilImpl implements HibernateUtil {
	
	private static Log LOG = LogFactory.getLog(HibernateUtilImpl.class);

	private SessionFactory sessionFactory;
	
	/**
	 * Container for thread-scoped sessions.
	 */
	private final static ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
	
	private final static ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();
	
	public void beginTransaction() {
		Transaction tx = threadTransaction.get();
		if (tx == null) {
			tx = getSession().beginTransaction();
			threadTransaction.set(tx);
		}
	}

	public void commitTransaction() {
		Transaction tx = threadTransaction.get();
		try {
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				tx.commit();
			}
			threadTransaction.set(null);
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
		Transaction tx = threadTransaction.get();
		try {
			threadTransaction.set(null);
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				tx.rollback();
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

	public void closeSession() {
		Session s = threadSession.get();
		threadSession.set(null);
		if (s != null && s.isOpen()) {
			s.close();
		}
	}
}

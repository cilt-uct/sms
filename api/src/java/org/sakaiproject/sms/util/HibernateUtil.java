package org.sakaiproject.sms.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface HibernateUtil {
	
	public Session getSession();

	public void closeSession();
	
	public void beginTransaction();
	
	public void commitTransaction();
	
	public void rollbackTransaction();
	
	public void setSessionFactory(SessionFactory sessionFactory);
	
}	

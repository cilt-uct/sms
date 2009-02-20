package org.sakaiproject.sms.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.sakaiproject.sms.logic.hibernate.QueryParameter;

@SuppressWarnings("unchecked")
public interface SmsDao {
	
	public void save(Object obj);
	
	public void delete(Object obj);
	
	public List runQuery(String hql, QueryParameter... queryParameters);
	
	public int executeUpdate(String hql, QueryParameter... queryParameters);
		
	public Object findById(Class className, Serializable id);
	
	public Criteria createCriteria(Class className);

}	

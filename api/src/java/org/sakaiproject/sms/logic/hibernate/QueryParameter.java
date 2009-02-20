package org.sakaiproject.sms.logic.hibernate;

import org.hibernate.type.Type;

public class QueryParameter {
	public String name;
	public Object val;
	public Type type;
	
	public QueryParameter(String name,Object val, Type type) {
		this.name = name;
		this.val = val;
		this.type = type;
	}
}

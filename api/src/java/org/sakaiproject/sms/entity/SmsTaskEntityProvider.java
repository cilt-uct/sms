package org.sakaiproject.sms.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

public interface SmsTaskEntityProvider extends EntityProvider {
	public final static String ENTITY_PREFIX = "sms-task";
	public final static String CUSTOM_ACTION = "calculate";
}

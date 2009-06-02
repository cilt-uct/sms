package org.sakaiproject.sms.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

public interface SmsTaskEntityProvider extends EntityProvider {
	public final static String ENTITY_PREFIX = "sms-task";
	/*
	 * Custom action locator for method processing cost properties of a phantom task based on recipient selections
	 */
	public final static String CUSTOM_ACTION_CALCULATE = "calculate";
	/**
	 * Custom action locator for method retrieving a list of site users only with mobile numbers
	 */
	public final static String CUSTOM_ACTION_USERS = "memberships";
}

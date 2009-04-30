package org.sakaiproject.sms.tool.renderers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.hibernate.SmsTask;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;

public class SavedSelectionsRenderer {
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void renderSelections(SmsTask smsTask, UIContainer tofill, String parentDiv){
		UIJointContainer parent = new UIJointContainer(tofill, parentDiv, "renderSelections:");
		
		UIMessage.make(parent, "recipients", "ui.failed.sms.recipients", new Object[] { smsTask.getGroupSizeEstimate() });
		
		//Insert original user selections
		List<String> smsEntities = smsTask.getDeliveryEntityList();
		
		List<String> groups = new ArrayList<String>();
		List<String> roles = new ArrayList<String>();
		if(smsEntities != null && smsEntities.size() > 0){
			for ( String entity : smsEntities){
				//in the format /site/123/role/something
				if ("site".equals(externalLogic.getEntityPrefix(entity)) && externalLogic.getEntityRealIdFromRefByKey(entity, "role") != null) {
					roles.add(externalLogic.getEntityRealIdFromRefByKey(entity, "role"));
				}else if ("site".equals(externalLogic.getEntityPrefix(entity)) && externalLogic.getEntityRealIdFromRefByKey(entity, "group") != null){
					groups.add(externalLogic.getSakaiGroupNameFromId(externalLogic.getEntityRealIdFromRefByKey(entity, "site"), externalLogic.getEntityRealIdFromRefByKey(entity, "group")));
				}
			}
		}
		StringBuffer rolesSb = new StringBuffer();
		StringBuffer groupsSb = new StringBuffer();
		StringBuffer usersSb = new StringBuffer();
		StringBuffer numbersSb = new StringBuffer();
		int count = 1;
		if(roles.size() > 0){
			for ( String role : roles){
				if ( role != null){
					rolesSb.append(role);
					 if( count != roles.size()){
						 rolesSb.append(", "); 
					 }else{
						 rolesSb.append(".");
					 }
					count ++;
				}
			}
			if (! "".equals(rolesSb.toString()) ){
				UIMessage.make(parent, "selections1", "ui.inprogress.selections.roles", new String[] {rolesSb.toString()});
			}
		}
		count = 1;
		if(groups.size() > 0){
			for ( String group : groups ) {
	        	groupsSb.append(group);
	        	if( count < groups.size()){
	        		groupsSb.append(", "); 
				 }else{
					 groupsSb.append(".");
				 }
	        	count ++;
			}
			if (! "".equals(groupsSb.toString()) ){
				UIMessage.make(parent, "selections2", "ui.inprogress.selections.groups", new String[] {groupsSb.toString()});
			}
		}
		
		Set<String> sakaiUserIds = smsTask.getSakaiUserIds();
		count = 1;
		if(sakaiUserIds != null && sakaiUserIds.size() > 0){
			for ( String user : sakaiUserIds){
				usersSb.append(externalLogic.getSakaiUserSortName(user));
	        	if( count != sakaiUserIds.size()){
	        		usersSb.append(", "); 
				 }else{
					 usersSb.append(".");
				 }
	        	count ++;
			}
			if(! "".equals(usersSb.toString()) ){
				UIMessage.make(parent, "selections3", "ui.inprogress.selections.names", new String[] {usersSb.toString()});
			}
		}
			
		Set<String> numbers = smsTask.getDeliveryMobileNumbersSet();
		count = 1;
		if(numbers != null && numbers.size() > 0){
			for ( String num : numbers){
				numbersSb.append(num);
				if( count != numbers.size()){
					numbersSb.append(", "); 
				 }else{
					 numbersSb.append(".");
				 }
	        	count ++;
			}
			if(! "".equals(numbersSb.toString()) ){
				UIMessage.make(parent, "selections4", "ui.inprogress.selections.numbers", new String[] {numbersSb.toString()});
			}
		}
	}
}

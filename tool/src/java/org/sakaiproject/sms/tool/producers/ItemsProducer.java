/******************************************************************************
 * ItemsProducer.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.sms.tool.producers;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.sms.logic.ExternalLogic;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.model.SmsItem;
import org.sakaiproject.sms.tool.params.AddItemViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * This is the view producer for the Items html template
 * @author Sakai App Builder -AZ
 */
public class ItemsProducer implements ViewComponentProducer, DefaultView {

	// The VIEW_ID must match the html template (without the .html)
	public static final String VIEW_ID = "Items";
	public String getViewID() {
		return VIEW_ID;
	}

	private SmsLogic logic;
	public void setLogic(SmsLogic logic) {
		this.logic = logic;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "page-title", "items_list.page_title");

		UIInternalLink.make(tofill, "add-item-link", UIMessage.make("project.add_item_link"),
				new AddItemViewParameters(AddItemProducer.VIEW_ID, null) );

		UIMessage.make(tofill, "hello-user-name", "project.greet_user", 
				new Object[] {externalLogic.getUserDisplayName(externalLogic.getCurrentUserId())});

		UIForm listform = UIForm.make(tofill, "listItemsForm");

		// header titles
		UIMessage.make(listform, "items-title-header", "items_list.title");
		UIMessage.make(listform, "items-hidden-header", "items_list.hidden");
		UIMessage.make(listform, "items-date-header", "items_list.date");

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

		List<SmsItem> l = logic.getAllVisibleItems(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId());
		for (Iterator<SmsItem> iter = l.iterator(); iter.hasNext();) {
			SmsItem item = iter.next();
			UIBranchContainer itemrow = UIBranchContainer.make(listform, 
					"item-row:", item.getId().toString() );
			if (logic.canWriteItem(item, externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId())) {
				UIBoundBoolean.make(itemrow, "select-item", 
						"#{itemsBean.selectedIds." + item.getId() + "}", Boolean.FALSE);
				UIInternalLink.make(itemrow, "item-update", item.getTitle(), 
						new AddItemViewParameters(AddItemProducer.VIEW_ID, item.getId()) );
			} else {
				UIOutput.make(itemrow, "item-title", item.getTitle() );
			}
			UIBoundBoolean.make(itemrow, "item-hidden", item.getHidden() );
			UIOutput.make(itemrow, "item-dateCreated", df.format(item.getDateCreated()) );
		}

		UICommand.make(listform, "delete-items", UIMessage.make("project.items_delete"),
				"#{itemsBean.processActionDelete}");
	}

}

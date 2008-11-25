/******************************************************************************
 * AddItemProducer.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.sms.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.logic.ExternalLogic;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.model.SmsItem;
import org.sakaiproject.sms.tool.params.AddItemViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This is the view producer for the AddItem html template
 * @author Sakai App Builder -AZ
 */
public class AddItemProducer
	implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	// The VIEW_ID must match the html template (without the .html)
	public static final String VIEW_ID = "AddItem";
	public String getViewID() {
		return VIEW_ID;
	}

	private final String DEFAULT_TITLE = "";
	private final Boolean DEFAULT_HIDDEN = Boolean.TRUE;

	private SmsLogic logic;
	public void setLogic(SmsLogic logic) {
		this.logic = logic;
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "page-title", "item_add.page_title");

		UIInternalLink.make(tofill, "list-items-link", UIMessage.make("project.list_items_link"),
				new SimpleViewParameters(ItemsProducer.VIEW_ID) );

		UIMessage.make(tofill, "hello-user-name", "project.greet_user", 
				new Object[] {externalLogic.getUserDisplayName(externalLogic.getCurrentUserId())});

		UIForm addupdateitem = UIForm.make(tofill, "addUpdateItemForm");

		UIMessage.make(tofill, "item-title-text", "item_add.title");
		UIMessage.make(tofill, "item-hidden-text", "item_add.hidden");

		AddItemViewParameters aivp = (AddItemViewParameters) viewparams;
		SmsItem item = null;
		if(aivp.id != null) {
			// passed in an id so we should be modifying an item if we can find it
			item = logic.getItemById( aivp.id );
		}

		// defaults for if we are creating a new item
		String item_title = DEFAULT_TITLE;
		Boolean item_hidden = DEFAULT_HIDDEN;
		if (item != null) {
			// we are modifying an existing item
			addupdateitem.parameters.add(new UIELBinding("#{itemsBean.newItem.id}", item.getId()) );
			item_title = item.getTitle();
			item_hidden = item.getHidden();
		}

		UIInput.make(addupdateitem, "item-title", 
				"#{itemsBean.newItem.title}", item_title );
		UIBoundBoolean hiddenCheckbox = UIBoundBoolean.make(addupdateitem, "item-hidden", 
				"#{itemsBean.newItem.hidden}", item_hidden );
		// add the tooltip to the checkbox
		hiddenCheckbox.decorators = new DecoratorList( 
				new UITooltipDecorator( UIMessage.make("item_add.hidden.tooltip") ) );

		UICommand.make(addupdateitem, "add-edit-item", UIMessage.make("item_add.add_button"),
				"#{itemsBean.processActionAdd}");
	}

   @SuppressWarnings("unchecked")
	public List reportNavigationCases() {
		List l = new ArrayList();
		l.add(new NavigationCase("added", new SimpleViewParameters(ItemsProducer.VIEW_ID)) );
		return l;
	}

	public ViewParameters getViewParameters() {
		return new AddItemViewParameters();
	}
}

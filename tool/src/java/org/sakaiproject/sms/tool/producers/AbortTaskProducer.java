/***********************************************************************************
 * AbortTaskProducer.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.beans.ActionResults;
import org.sakaiproject.sms.tool.otp.SmsTaskLocator;
import org.sakaiproject.sms.tool.params.IdParams;
import org.sakaiproject.sms.tool.renderers.NavBarRenderer;

import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class AbortTaskProducer implements ViewComponentProducer,
		NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "abort_task";

	private NavBarRenderer navBarRenderer;
	private BeanGetter ELEvaluator;

	public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

	public void setELEvaluator(BeanGetter ELEvaluator) {
		this.ELEvaluator = ELEvaluator;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		UIMessage.make(tofill, "page-title", "sms.abort-task.title");
		UIMessage
				.make(tofill, "sms-abort-task-heading", "sms.abort-task.title");

		UIForm form = UIForm.make(tofill, "abort-task-form");

		IdParams params = (IdParams) viewparams;
		SmsTask task = (SmsTask) ELEvaluator
				.getBean(SmsTaskLocator.LOCATOR_NAME + "." + params.id);

		if (task != null) {
			UIMessage.make(form, "confirm-msg", "sms.abort-task.confirm",
					new Object[] { task.getId(), task.getDeliveryGroupName(),
							task.getMessageBody() });

			UICommand.make(form, "abort-button", UIMessage
					.make("sms.abort-task.abort"),
					"AbortTaskActionBean.abortTask");

			form.addParameter(new UIELBinding(
					"AbortTaskActionBean.taskToAbort", task.getId()));
		} else {
			UIMessage.make(form, "invalid-task", "sms.abort-task.invalid-task");
		}

		UICommand.make(form, "cancel-button",
				UIMessage.make("sms.general.cancel")).setReturn(
				ActionResults.CANCEL);
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase(ActionResults.CANCEL,
				new SimpleViewParameters(TaskListProducer.VIEW_ID)));
		list.add(new NavigationCase(ActionResults.SUCCESS,
				new SimpleViewParameters(TaskListProducer.VIEW_ID)));
		return list;
	}

	public ViewParameters getViewParameters() {
		return new IdParams();
	}

}

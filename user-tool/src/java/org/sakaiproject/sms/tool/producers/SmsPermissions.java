package org.sakaiproject.sms.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.logic.external.ExternalLogic;

import uk.ac.cam.caret.sakai.rsf.helper.HelperViewParameters;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class SmsPermissions implements ViewComponentProducer,ViewParamsReporter,NavigationCaseReporter{
		
	public static final String HELPER = "sakai.permissions.helper"; //The Helper ID should be the ID of the helper, in this case sakai.permissions. helper
	public static final String VIEW_ID = "sms-permissions";
	public static final String perm_Prefix = "sms.";
	
	public MessageLocator messageLocator;
	
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	public String getViewID() {
	  return VIEW_ID;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	
	  externalLogic.setUpSessionPermissions(perm_Prefix); 
	  UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
	  UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);
	}
	
	public ViewParameters getViewParameters() {
		//This is what tells SakaiRSF that we are a stub for a Sakai Helper. If you don't report HelperViewParameters (or a subclass of it), RSF will try to render this as a normal page.
	  return new HelperViewParameters();
	}
	
	@SuppressWarnings("unchecked")
	public List reportNavigationCases() {
	  List l = new ArrayList();
	  l.add(new NavigationCase(null, new SimpleViewParameters(MainProducer.VIEW_ID)));
	  return l;
	}

}
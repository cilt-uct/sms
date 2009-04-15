/***********************************************************************************
 * MsgBodyEditorTest.java
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
package org.sakaiproject.sms.tool.test;

import java.beans.PropertyEditor;

import junit.framework.TestCase;

import org.sakaiproject.sms.tool.util.MsgBodyEditorFactory;

public class MsgBodyEditorTest extends TestCase {
	MsgBodyEditorFactory msgBodyEditorFactory;

	@Override
	protected void setUp() throws Exception {
		msgBodyEditorFactory = new MsgBodyEditorFactory();
	}

	/**
	 * Test that a property editor is retrieved
	 */
	public void testMsgBodyEditorFactory() {
		PropertyEditor editor = msgBodyEditorFactory.getPropertyEditor();
		assertNotNull(editor);
	}

	/**
	 * Test of \r\n is replaced correctly
	 */
	public void testPropertyEditorModify() {
		PropertyEditor editor = msgBodyEditorFactory.getPropertyEditor();
		editor.setAsText("\r\n");
		assertEquals("\n", editor.getValue());
	}

	/**
	 * Test of \r\n is replaced correctly with multiple lines
	 */
	public void testPropertyEditorModifyMultiple() {
		PropertyEditor editor = msgBodyEditorFactory.getPropertyEditor();
		editor.setAsText("a\r\nb\r\nc\r\n");
		assertEquals("a\nb\nc\n", editor.getValue());
	}
}

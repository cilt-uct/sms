/***********************************************************************************
 * FloatEditorTest.java
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

import org.sakaiproject.sms.tool.util.NumberEditorFactory;
import org.sakaiproject.sms.tool.util.SmsCustomNumberEditor;

/**
 * Tests associated with {@link NumberEditorFactory} and
 * {@link SmsCustomNumberEditor}
 * 
 */
public class NumberEditorTest extends TestCase {
	NumberEditorFactory numberEditorFactory;

	@Override
	protected void setUp() throws Exception {
		numberEditorFactory = new NumberEditorFactory();
		numberEditorFactory.setField("test");

	}

	/**
	 * Test PropertyEditor retrieved from FloatEditorFactory
	 */
	public void testFloatEditorFactory() {
		numberEditorFactory.setNumberClass("java.lang.Float");
		PropertyEditor editor = numberEditorFactory.getPropertyEditor();
		assertNotNull(editor);
		assertTrue(editor instanceof SmsCustomNumberEditor);
	}

	/**
	 * Test empty + whitespace
	 */
	public void testSmsCustomFloatEditor_empty() {
		numberEditorFactory.setNumberClass("java.lang.Float");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		editor.setAsText("");
		assertNull(editor.getValue());

		editor.setAsText(" ");
		assertNull(editor.getValue());
	}

	/**
	 * Test invalid number
	 */
	public void testSmsCustomFloatEditor_invalid() {
		numberEditorFactory.setNumberClass("java.lang.Float");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		try {
			editor.setAsText("xXxxX");
			fail("NumberFormatException should be thrown");
		} catch (NumberFormatException nfe) {
			// Test message
			assertEquals("sms.errors.test.invalid", nfe.getLocalizedMessage());
		}
		assertNull(editor.getValue());
	}

	/**
	 * Test null
	 */
	public void testSmsCustomFloatEditor_null() {
		numberEditorFactory.setNumberClass("java.lang.Float");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		editor.setAsText(null);
		assertNull(editor.getValue());
	}

	/**
	 * Test Float valid numbers
	 */
	public void testSmsCustomFloatEditor_valid() {
		numberEditorFactory.setNumberClass("java.lang.Float");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();

		editor.setAsText("200");
		assertNotNull(editor.getValue());
		assertTrue(editor.getValue() instanceof Float);
		Float value = (Float) editor.getValue();
		assertEquals(200f, value);

		editor.setAsText("123.01");
		value = (Float) editor.getValue();
		assertEquals(123.01f, value);

		editor.setAsText("456.2");
		value = (Float) editor.getValue();
		assertEquals(456.2f, value);
	}

	/**
	 * Test Integer valid numbers
	 */
	public void testSmsCustomIntegerEditor_valid() {
		numberEditorFactory.setNumberClass("java.lang.Integer");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();

		editor.setAsText("200");
		assertNotNull(editor.getValue());
		assertTrue(editor.getValue() instanceof Integer);
		Integer value = (Integer) editor.getValue();
		assertEquals(Integer.valueOf(200), value);
	}

	/**
	 * Test empty + whitespace
	 */
	public void testSmsCustomIntegerEditor_empty() {
		numberEditorFactory.setNumberClass("java.lang.Integer");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		editor.setAsText("");
		assertNull(editor.getValue());

		editor.setAsText(" ");
		assertNull(editor.getValue());
	}

	/**
	 * Test invalid number
	 */
	public void testSmsCustomFloatInteger_invalid() {
		numberEditorFactory.setNumberClass("java.lang.Integer");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		try {
			editor.setAsText("xXxxX");
			fail("NumberFormatException should be thrown");
		} catch (NumberFormatException nfe) {
			// Test message
			assertEquals("sms.errors.test.invalid", nfe.getLocalizedMessage());
		}
		assertNull(editor.getValue());
	}

	/**
	 * Test null
	 */
	public void testSmsCustomIntegerEditor_null() {
		numberEditorFactory.setNumberClass("java.lang.Integer");
		SmsCustomNumberEditor editor = (SmsCustomNumberEditor) numberEditorFactory
				.getPropertyEditor();
		editor.setAsText(null);
		assertNull(editor.getValue());
	}

}

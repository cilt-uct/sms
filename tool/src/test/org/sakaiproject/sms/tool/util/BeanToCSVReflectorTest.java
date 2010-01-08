/***********************************************************************************
 * BeanToCSVReflectorTest.java
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
package org.sakaiproject.sms.tool.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;

import junit.framework.TestCase;

public class BeanToCSVReflectorTest extends TestCase {

	private BeanToCSVReflector beanToCSVReflector;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		beanToCSVReflector = new BeanToCSVReflector();
		beanToCSVReflector.setExternalLogic(new ExternalLogicStub());
	
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSimpleResultSetNoOrder() throws Exception {

		List<SimpleResultSet> list = createSimpleTestRow();

		String csvHeaderAndRow = beanToCSVReflector.toCSV(list);

		assertTrue(csvHeaderAndRow.contains(","));
		assertTrue(csvHeaderAndRow.contains("label"));
		assertTrue(csvHeaderAndRow.contains("Row 1"));
		assertTrue(csvHeaderAndRow.contains("2000"));
		assertTrue(csvHeaderAndRow.contains("255"));
	}

	public void testSimpleResultSetWithOrder() throws Exception {
		List<SimpleResultSet> list = createSimpleTestRow();
		String[] fieldNames = new String[] { "longValue", "label" };

		String csvHeaderAndRow = beanToCSVReflector.toCSV(list, fieldNames);
		assertTrue(csvHeaderAndRow.length() > 0);

		StringTokenizer stringTokenizer = new StringTokenizer(csvHeaderAndRow,
				"\n");

		assertEquals("longValue, label", stringTokenizer.nextElement());
		assertEquals("2000,Row 1", stringTokenizer.nextElement());
	}

	public void testExtendedResultNoOrder() throws Exception {

		List<ExtendedResultSet> list = createExtendedTestRow();
		String extendedCsvHeaderAndRow = beanToCSVReflector.toCSV(list);

		assertTrue(extendedCsvHeaderAndRow.contains("Row 1"));
		assertTrue(extendedCsvHeaderAndRow.contains("More info"));
	}

	public void testResultSetWithBoolean() {

		List<BooleanResultSet> booleanResults = new ArrayList<BooleanResultSet>();
		BooleanResultSet booleanResult1 = new BooleanResultSet(false);
		booleanResults.add(booleanResult1);

		String csvResults = beanToCSVReflector.toCSV(booleanResults,
				new String[] { "endOfTime" });

		assertTrue(csvResults.contains("endOfTime"));
		assertTrue(csvResults.contains("false"));
	}

	private List<ExtendedResultSet> createExtendedTestRow() {
		ExtendedResultSet extendedResultSet = new ExtendedResultSet("Row 1",
				Long.valueOf(2000), 255, "More info");
		List<ExtendedResultSet> list = new ArrayList<ExtendedResultSet>();
		list.add(extendedResultSet);
		return list;
	}

	private List<SimpleResultSet> createSimpleTestRow() {
		SimpleResultSet simpleResultSet1 = new SimpleResultSet("Row 1", Long
				.valueOf(2000), 255);
		List<SimpleResultSet> list = new ArrayList<SimpleResultSet>();
		list.add(simpleResultSet1);
		return list;
	}

	static class BooleanResultSet {

		private boolean endOfTime;

		public BooleanResultSet(boolean endOfTime) {
			super();
			this.endOfTime = endOfTime;
		}

		public boolean isEndOfTime() {
			return endOfTime;
		}

		public void setEndOfTime(boolean endOfTime) {
			this.endOfTime = endOfTime;
		}
	}

	static class SimpleResultSet {

		private String label;
		private int number;
		private Long longValue;

		public SimpleResultSet(String label, Long longValue, int number) {
			super();
			this.label = label;
			this.longValue = longValue;
			this.number = number;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public Long getLongValue() {
			return longValue;
		}

		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}
	}

	static class ExtendedResultSet extends SimpleResultSet {

		public String additionalInfo;

		public ExtendedResultSet(String label, Long longValue, int number,
				String addtionalInfo) {
			super(label, longValue, number);
			this.additionalInfo = addtionalInfo;
		}

		public String getAdditionalInfo() {
			return additionalInfo;
		}

		public void setAdditionalInfo(String additionalInfo) {
			this.additionalInfo = additionalInfo;
		}
	}

}

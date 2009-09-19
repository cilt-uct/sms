/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.model.smpp;

import java.util.ArrayList;
import java.util.List;

public class SmsPatternSearchResult {

	public static final String NO_MATCHES = "0";

	public static final String ONE_MATCH = "1";

	public static final String MORE_THAN_ONE_MATCH = "2";

	private List<String> possibleMatches = new ArrayList<String>();

	private String pattern = null;

	private String matchResult = null;

	public String getMatchResult() {
		return matchResult;
	}

	public SmsPatternSearchResult() {

	}

	public void setMatchResult(String matchResult) {
		this.matchResult = matchResult;
	}

	public SmsPatternSearchResult(String pattern) {
		this.pattern = pattern;
		this.matchResult = SmsPatternSearchResult.ONE_MATCH;
		this.possibleMatches.add(pattern);
	}

	public List<String> getPossibleMatches() {
		return possibleMatches;
	}

	public void setPossibleMatches(List<String> possibleMatches) {
		this.possibleMatches = possibleMatches;
		if (possibleMatches.isEmpty()) {
			setMatchResult(SmsPatternSearchResult.NO_MATCHES);
		} else if (possibleMatches.size() == 1) {
			setMatchResult(SmsPatternSearchResult.ONE_MATCH);
			setPattern(possibleMatches.get(0));
		} else {
			setMatchResult(SmsPatternSearchResult.MORE_THAN_ONE_MATCH);
		}
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}

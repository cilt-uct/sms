package org.sakaiproject.sms.model.smpp;

import java.util.ArrayList;

public class SmsPatternSearchResult {

	public static final String NO_MATCHES = "0";

	public static final String ONE_MATCH = "1";

	public static final String MORE_THEN_ONE_MATCH = "2";

	private ArrayList<String> possibleMatches = new ArrayList<String>();

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

	public ArrayList<String> getPossibleMatches() {
		return possibleMatches;
	}

	public void setPossibleMatches(ArrayList<String> possibleMatches) {
		this.possibleMatches = possibleMatches;
		if (possibleMatches.isEmpty()) {
			setMatchResult(SmsPatternSearchResult.NO_MATCHES);
		} else if (possibleMatches.size() == 1) {
			setMatchResult(SmsPatternSearchResult.ONE_MATCH);
			setPattern(possibleMatches.get(0));
		} else {
			setMatchResult(SmsPatternSearchResult.MORE_THEN_ONE_MATCH);
		}
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}

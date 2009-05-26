package org.sakaiproject.sms.tool.util;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {
	
	public String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
	private DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	/**
	 * Convert a value and round it up to 2 decimal places
	 * @param value Value to be converted
	 * @return fully formated currency eg. R51.65
	 */
	public String toServerLocale(Float value) {
		return currency + decimalFormat.format(value);
	}
	
	/**
	 * Convert a value and round it up to 2 decimal places
	 * @param value Value to be converted
	 * @return fully formated currency eg. R51.65
	 */
	public String toServerLocale(double value) {
		return currency + decimalFormat.format(value);
	}
}
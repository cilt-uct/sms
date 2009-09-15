package org.sakaiproject.sms.logic.hibernate.test.dataload;

import java.util.Date;
import java.util.Random;

public class RandomUtils {

	Random random = new Random();

	public RandomUtils() {
		super();
	}

	public Float getRandomFloat(int maxSize) {
		return random.nextFloat() * random.nextInt(maxSize);
	}

	@SuppressWarnings("deprecation")
	public Date getBoundRandomDate(int year) {

		int month = random.nextInt(11) + 1;
		int day = random.nextInt(27) + 1;
		int hour = random.nextInt(23);
		int min = random.nextInt(59);
		int sec = random.nextInt(59);

		return new Date(year - 1900, month, day, hour, min, sec);
	}

	public Integer getRandomInteger(int maxSize) {
		return random.nextInt(maxSize);
	}


}

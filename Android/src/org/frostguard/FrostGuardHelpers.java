package org.frostguard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.util.Log;

public class FrostGuardHelpers {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm EEE");
	public static TempDate getLowestTemp(final List<TempDate> datalist) {
		TempDate lowest = null;
		Calendar  cal = GregorianCalendar.getInstance();
		cal.roll(Calendar.DAY_OF_MONTH , 1);
		for (TempDate data : datalist ) {
			Log.i("Widget","Date:"+data.getFromTime()+" cal:"+cal.getTime());
			if (data.getFromTime().before(cal.getTime())) {
				if (lowest == null || lowest.getTemperature() > data.getTemperature()) {
					lowest = data;
				}
			}
			
		}
		return lowest;
	}
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}
}

package com.cocoatest.st.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.os.Build;

@SuppressLint("SimpleDateFormat")
public class DateTimeUtils {

	public static String getDateTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String mDateTime;
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk")))
			mDateTime = formatter.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
		else
			mDateTime = formatter.format(cal.getTime().getTime());
		return mDateTime;
	}
}

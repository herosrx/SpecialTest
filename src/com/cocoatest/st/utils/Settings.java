package com.cocoatest.st.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Parameters in Setting Activity
 * 
 */
public final class Settings {

	public static final String KEY_SELECTED_PACKAGE = "selected_package";
	
	// capability页面配置
	public static final String KEY_HOME_CPU = "home_cpu";
	public static final String KEY_HOME_MEMORY = "home_memory";
	public static final String KEY_HOME_TRAFFIC = "home_traffic";
	public static final String KEY_HOME_BATTERY = "home_battery";
	public static final String KEY_HOME_INTERVAL = "home_interval";

	// 设置页面配置
	public static final String KEY_RECIPIENTS = "recipients";
	public static final String KEY_ISFLOAT = "isfloat";
	public static final String KEY_ROOT = "root";

	// monkey页面配置
	public static final String KEY_MONKEY_COUNTS = "monkey_counts";
	public static final String KEY_MONKEY_SEED = "monkey_seed";
	public static final String KEY_MONKEY_THROTTLE = "monkey_throttle";
	public static final String KEY_MONKEY_CRASH = "monkey_ignore_crashes";
	public static final String KEY_MONKEY_TIMEOUT = "monkey_ignore_timeouts";
	public static final String KEY_MONKEY_LOGLEVEL = "monkey_log_level";

	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}

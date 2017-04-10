package com.cocoatest.st.activity;

import java.lang.reflect.Field;

import com.cocoatest.st.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class BaseActivity extends Activity {

	private Long mExitTime = (long) 0;
	
	public static final String path =  "/sdcard/STReports/";
	
	public void exitBy2Click(){
		if ((System.currentTimeMillis() - mExitTime) > 2000) {
			Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
			mExitTime = System.currentTimeMillis();
		} else {
			finish();
		}
	}
	
	
	
	/**
	 * 点击dialog的按钮，true为可关闭，false为不可关闭
	 * 
	 * @param dialog
	 * @param status
	 */
	public  void controlDialog(DialogInterface dialog, boolean status) {

		Field field;
		try {
			field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, status);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private  long lastClickTime = 0;

	public  boolean isFastClick(long intervalTime) {
		if (System.currentTimeMillis() - lastClickTime < intervalTime) {
			return true;
		}
		lastClickTime = System.currentTimeMillis();
		return false;
	}

	
}

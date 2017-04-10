package com.cocoatest.st.activity;

import java.io.DataOutputStream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoatest.st.utils.Settings;
import com.cocoatest.st.R;

public class SettingsActivity extends BaseActivity implements OnClickListener {

	private static final String LOG_TAG = "SpecialTest-" + SettingsActivity.class.getSimpleName();

	private ImageView btnSave;
	private LinearLayout layGoBack;
	private TextView title;

	private RelativeLayout floatingItem;
	private LinearLayout layHeapItem;

	private CheckBox chkFloat;
	private CheckBox chkRoot;

	private LinearLayout basicInfo;
	private LinearLayout about;
	private LinearLayout mailSettings;
	private LinearLayout direction;

	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		boolean isfloat = preferences.getBoolean(Settings.KEY_ISFLOAT, true);
		boolean isRoot = preferences.getBoolean(Settings.KEY_ROOT, false);
		initViews();
		chkFloat.setChecked(isfloat);
		chkRoot.setChecked(isRoot);
		floatingItem.setOnClickListener(this);
		layHeapItem.setOnClickListener(this);
		mailSettings.setOnClickListener(this);
		direction.setOnClickListener(this);
		about.setOnClickListener(this);
		basicInfo.setOnClickListener(this);
	}

	private void initViews() {
		chkFloat = (CheckBox) findViewById(R.id.floating);
		chkRoot = (CheckBox) findViewById(R.id.is_root);
		basicInfo = (LinearLayout) findViewById(R.id.info_device_basic);
		about = (LinearLayout) findViewById(R.id.about);
		direction = (LinearLayout) findViewById(R.id.direction);
		mailSettings = (LinearLayout) findViewById(R.id.mail_settings);
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		floatingItem = (RelativeLayout) findViewById(R.id.floating_item);
		layHeapItem = (LinearLayout) findViewById(R.id.heap_item);

		layGoBack.setVisibility(View.INVISIBLE);
		title.setText(R.string.setting);
		btnSave.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.floating_item:
			boolean isChecked_float = chkFloat.isChecked();
			chkFloat.setChecked(!isChecked_float);
			preferences.edit().putBoolean(Settings.KEY_ISFLOAT, !isChecked_float).commit();
			break;
		case R.id.heap_item:
			boolean isChecked_heap = chkRoot.isChecked();
			if (isChecked_heap) {
				chkRoot.setChecked(!isChecked_heap);
				preferences.edit().putBoolean(Settings.KEY_ROOT, !isChecked_heap).commit();
			} else {
				boolean root = upgradeRootPermission(getPackageCodePath());
				if (root) {
					Log.d(LOG_TAG, "root succeed");
					chkRoot.setChecked(!isChecked_heap);
					preferences.edit().putBoolean(Settings.KEY_ROOT, !isChecked_heap).commit();
				} else {
					// if root failed, tell user to check if phone is rooted
					Toast.makeText(getBaseContext(), getString(R.string.root_failed_notification), Toast.LENGTH_LONG).show();
				}
			}
			break;

		case R.id.mail_settings:
			Intent mailSettingIntent = new Intent();
			mailSettingIntent.setClass(SettingsActivity.this, MailSettingActivity.class);
			startActivity(mailSettingIntent);
			break;

		case R.id.direction:
			Intent directionIntent = new Intent();
			directionIntent.setClass(SettingsActivity.this, DirectionActivity.class);
			startActivity(directionIntent);
			break;

		case R.id.about:
			Intent aboutIntent = new Intent();
			aboutIntent.setClass(SettingsActivity.this, AboutActivity.class);
			startActivity(aboutIntent);
			break;
		case R.id.info_device_basic:
			Intent basicInfoIntent = new Intent();
			basicInfoIntent.setClass(SettingsActivity.this, BasicInfoActivity.class);
			startActivity(basicInfoIntent);
			break;
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * upgrade app to get root permission
	 * 
	 * @return is root successfully
	 */
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			int existValue = process.waitFor();
			if (existValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.d(LOG_TAG, "upgradeRootPermission exception=" + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			exitBy2Click();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}

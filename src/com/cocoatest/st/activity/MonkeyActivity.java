package com.cocoatest.st.activity;

import java.io.File;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.DateTimeUtils;
import com.cocoatest.st.utils.Settings;
import com.cocoatest.st.utils.ShellUtils;
import com.cocoatest.st.utils.ShellUtils.CommandResult;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class MonkeyActivity extends BaseActivity implements OnClickListener {

	private static final String LOG_TAG = "SpecialTest-" + MonkeyActivity.class.getSimpleName();
	public static final int MONKEY_REQUSET = 2;

	private LinearLayout layGoBack;
	private TextView title;
	private ImageView btnSave;
	private TextView monkeyAppName;

	private String selectProcessName;

	private EditText et_count;
	private EditText et_seed;
	private EditText et_throttle;

	private Button btMonkeyTest;

	private String selectPackageName;
	private long counts;
	private long seed;
	private long throttle;

	private RelativeLayout rl_ignoreCrashes;
	private RelativeLayout rl_ignoreTimeouts;
	private CheckBox cb_ignoreCrashes;
	private CheckBox cb_ignoreTimeouts;

	private Spinner sp_loglevel;
	private ArrayAdapter<String> adapter;
	private String[] logLevels;
	private String value_loglevel;
	private int key_loglevel;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.monkey);
		initViews();
		getIntentInfo();
		monkeyAppName.setText(selectProcessName);
		getMonkeySetting();
		setLogLevelValue();
		rl_ignoreCrashes.setOnClickListener(this);
		rl_ignoreTimeouts.setOnClickListener(this);
		btMonkeyTest.setOnClickListener(this);
		layGoBack.setOnClickListener(this);
	}

	private void initViews() {
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		title.setText(R.string.monkey);
		btnSave.setVisibility(View.INVISIBLE);
		// 初始化选择app框的属性
		monkeyAppName = (TextView) findViewById(R.id.monkey_app_name);

		et_count = (EditText) findViewById(R.id.item_et_count);
		et_seed = (EditText) findViewById(R.id.item_et_seed);
		et_throttle = (EditText) findViewById(R.id.item_et_throttle);

		rl_ignoreCrashes = (RelativeLayout) findViewById(R.id.item_ignore_crashes);
		cb_ignoreCrashes = (CheckBox) findViewById(R.id.item_cb_ignore_crashes);
		rl_ignoreTimeouts = (RelativeLayout) findViewById(R.id.item_ignore_timeouts);
		cb_ignoreTimeouts = (CheckBox) findViewById(R.id.item_cb_ignore_timeouts);

		sp_loglevel = (Spinner) findViewById(R.id.sp_monkey_log_level);

		btMonkeyTest = (Button) findViewById(R.id.monkey_test);

	}

	private void getIntentInfo() {
		Intent intent = getIntent();
		selectProcessName = intent.getStringExtra("selectProcessName");
		selectPackageName = intent.getStringExtra("selectPackageName");
	}

	private void getMonkeySetting() {
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		counts = preferences.getLong(Settings.KEY_MONKEY_COUNTS, 1000);
		seed = preferences.getLong(Settings.KEY_MONKEY_SEED, 999);
		throttle = preferences.getLong(Settings.KEY_MONKEY_THROTTLE, 500);
		key_loglevel = preferences.getInt(Settings.KEY_MONKEY_LOGLEVEL, 0);
		et_count.setText(counts + "");
		et_seed.setText(seed + "");
		et_throttle.setText(throttle + "");
		cb_ignoreCrashes.setChecked(preferences.getBoolean(Settings.KEY_MONKEY_CRASH, true));
		cb_ignoreTimeouts.setChecked(preferences.getBoolean(Settings.KEY_MONKEY_TIMEOUT, true));
	}

	private void setLogLevelValue() {
		logLevels = new String[] { "简单", "一般", "详细" };
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logLevels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_loglevel.setAdapter(adapter);
		sp_loglevel.setSelection(key_loglevel);
		sp_loglevel.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				value_loglevel = logLevels[arg2];
				preferences.edit().putInt(Settings.KEY_MONKEY_LOGLEVEL, arg2).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.lay_go_back:
			finish();
			break;
		case R.id.item_ignore_crashes:
			boolean isChecked_crashes = cb_ignoreCrashes.isChecked();
			cb_ignoreCrashes.setChecked(!isChecked_crashes);
			preferences.edit().putBoolean(Settings.KEY_MONKEY_CRASH, !isChecked_crashes).commit();
			break;
		case R.id.item_ignore_timeouts:
			boolean isChecked_timeouts = cb_ignoreTimeouts.isChecked();
			cb_ignoreTimeouts.setChecked(!isChecked_timeouts);
			preferences.edit().putBoolean(Settings.KEY_MONKEY_TIMEOUT, !isChecked_timeouts).commit();
			break;
		case R.id.monkey_test:
			if (isFastClick(2000)) {
				return;
			}
			String str_counts = et_count.getText().toString();
			String str_seed = et_seed.getText().toString();
			String str_throttle = et_throttle.getText().toString();
			if (str_counts.length() != 0 && str_seed.length() != 0 && str_throttle.length() != 0) {
				boolean isRoot = ShellUtils.checkRootPermission();
				if (isRoot) {
					String loglevel = "";
					counts = Long.valueOf(str_counts);
					seed = Long.valueOf(str_seed);
					throttle = Long.valueOf(str_throttle);
					if ("简单".equals(value_loglevel)) {
						loglevel = " -v ";
					} else if ("一般".equals(value_loglevel)) {
						loglevel = " -v -v ";
					} else {
						loglevel = " -v -v -v ";
					}
					preferences.edit().putLong(Settings.KEY_MONKEY_COUNTS, counts).commit();
					preferences.edit().putLong(Settings.KEY_MONKEY_SEED, seed).commit();
					preferences.edit().putLong(Settings.KEY_MONKEY_THROTTLE, throttle).commit();

					String isIgnoreCrashes = cb_ignoreCrashes.isChecked() ? " --ignore-crashes" : "";
					String isIgnoreTimeouts = cb_ignoreTimeouts.isChecked() ? " --ignore-timeouts" : "";

					String mDateTime = DateTimeUtils.getDateTime();
					String logPath = path + "ST_MonkeyLog_" + mDateTime + ".txt";
					File dirFile = new File(path);
					if (!dirFile.exists()) {
						dirFile.mkdir();
					}
					String cmd_monkey = "monkey -p " + selectPackageName + " -s " + seed + " --throttle " + throttle + isIgnoreCrashes
							+ isIgnoreTimeouts + " --pct-trackball 0 " + "--pct-nav 0 " + "--pct-majornav 0 " + "--pct-syskeys 0 "
							+ "--ignore-security-exceptions " + loglevel + counts + " >" + logPath;
					Log.d(LOG_TAG, "cmd_monkey = " + cmd_monkey);
					CommandResult result = ShellUtils.execCommand(cmd_monkey, true, true);
					Log.d(LOG_TAG, "result---" + result.result);
					if (result.result == 0) {
						String cmd_killprg = "am force-stop " + selectPackageName;
						Log.d(LOG_TAG, "cmd_killprg = " + cmd_killprg);
						ShellUtils.execCommand(cmd_killprg, true, false);
						Toast.makeText(this, R.string.test_finish_toast, Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MonkeyActivity.this, R.string.root_failed_notification, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(MonkeyActivity.this, "请填写参数", Toast.LENGTH_LONG).show();
			}
			break;
		}
	}
}

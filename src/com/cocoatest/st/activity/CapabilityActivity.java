package com.cocoatest.st.activity;

import java.io.IOException;
import java.util.List;

import com.cocoatest.st.R;
import com.cocoatest.st.service.STService;
import com.cocoatest.st.utils.ProcessInfo;
import com.cocoatest.st.utils.Programe;
import com.cocoatest.st.utils.Settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class CapabilityActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + CapabilityActivity.class.getSimpleName();
	private static final int TIMEOUT = 20000;

	private LinearLayout layGoBack;
	private TextView title;
	private ImageView btnSave;
	private TextView capabilityAppName;

	private RelativeLayout rl_cpu;
	private RelativeLayout rl_memory;
	private RelativeLayout rl_traffic;
	private RelativeLayout rl_battery;
	private CheckBox cb_cpu;
	private CheckBox cb_memory;
	private CheckBox cb_traffic;
	private CheckBox cb_battery;
	private boolean isCpu;
	private boolean isMemory;
	private boolean isTraffic;
	private boolean isBattery;

	private List<Programe> processList;
	private ProcessInfo processInfo;
	private UpdateReceiver receiver;

	private Spinner sp_frequency;
	private ArrayAdapter<CharSequence> adapter;

	private Button btHomeTest;

	public static final int HOME_REQUSET = 1;
	private String selectProcessName;
	private String selectPackageName;
	private int pid, uid, interval;
	private boolean isServiceStop = false;

	private Intent monitorService;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.capability);
		initViews();
		getIntentInfo();
		capabilityAppName.setText(selectProcessName);
		setMonitorInfo();
		processInfo = new ProcessInfo();
		setCollectingFrequencyVlaue();
		rl_cpu.setOnClickListener(new CbCpuClickListener());
		rl_memory.setOnClickListener(new CbMemoryClickListener());
		rl_traffic.setOnClickListener(new CbTrafficClickListener());
		rl_battery.setOnClickListener(new CbBatteryClickListener());
		btHomeTest.setOnClickListener(new HomeTestClickListener());
		layGoBack.setOnClickListener(new GoBackClickListener());
		receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(STService.SERVICE_ACTION);
		registerReceiver(receiver, filter);
	}

	private void initViews() {
		// 初始化titile的属性
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		title.setText(R.string.capability);
		btnSave.setVisibility(View.INVISIBLE);
		// 初始化选择app框的属性
		capabilityAppName = (TextView) findViewById(R.id.capability_app_name);
		// 初始化监控项
		rl_cpu = (RelativeLayout) findViewById(R.id.item_cpu);
		rl_memory = (RelativeLayout) findViewById(R.id.item_memory);
		rl_traffic = (RelativeLayout) findViewById(R.id.item_traffic);
		rl_battery = (RelativeLayout) findViewById(R.id.item_battery);
		cb_cpu = (CheckBox) findViewById(R.id.item_cb_cpu);
		cb_memory = (CheckBox) findViewById(R.id.item_cb_memory);
		cb_traffic = (CheckBox) findViewById(R.id.item_cb_traffic);
		cb_battery = (CheckBox) findViewById(R.id.item_cb_battery);

		sp_frequency = (Spinner) findViewById(R.id.sp_collecting_frequency);
		btHomeTest = (Button) findViewById(R.id.home_test);
	}

	private void getIntentInfo() {
		Intent intent = getIntent();
		selectProcessName = intent.getStringExtra("selectProcessName");
		selectPackageName = intent.getStringExtra("selectPackageName");
	}

	private void setMonitorInfo() {
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		isCpu = preferences.getBoolean(Settings.KEY_HOME_CPU, true);
		isMemory = preferences.getBoolean(Settings.KEY_HOME_MEMORY, true);
		isTraffic = preferences.getBoolean(Settings.KEY_HOME_TRAFFIC, true);
		isBattery = preferences.getBoolean(Settings.KEY_HOME_BATTERY, true);
		cb_cpu.setChecked(isCpu);
		cb_memory.setChecked(isMemory);
		cb_traffic.setChecked(isTraffic);
		cb_battery.setChecked(isBattery);
		interval = preferences.getInt(Settings.KEY_HOME_INTERVAL, 5);
	}

	private void setCollectingFrequencyVlaue() {
		adapter = ArrayAdapter.createFromResource(this, R.array.collecting_frequency_value, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_frequency.setAdapter(adapter);
		setSpinnerItemSelectedByValue(sp_frequency, String.valueOf(interval));
		sp_frequency.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				interval = Integer.parseInt(CapabilityActivity.this.getResources().getStringArray(R.array.collecting_frequency_value)[arg2]);
				preferences.edit().putInt(Settings.KEY_HOME_INTERVAL, interval).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	private class HomeTestClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isFastClick(2000)) {
				return;
			}
			if (!cb_cpu.isChecked() && !cb_memory.isChecked() && !cb_traffic.isChecked() && !cb_battery.isChecked()) {
				Toast.makeText(CapabilityActivity.this, R.string.select_monitor_setting, Toast.LENGTH_LONG).show();
			} else {
				monitorService = new Intent();
				monitorService.setClass(CapabilityActivity.this, STService.class);
				if (getString(R.string.start_test).equals(btHomeTest.getText().toString())) {
					Intent intent = getPackageManager().getLaunchIntentForPackage(selectPackageName);
					String startActivity = "";
					// clear logcat
					try {
						Runtime.getRuntime().exec("logcat -c");
					} catch (IOException e) {
						Log.d(LOG_TAG, e.getMessage());
					}
					try {
						startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
						startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(CapabilityActivity.this, getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
						return;
					}
					waitForAppStart(selectPackageName);
					monitorService.putExtra("processName", selectProcessName);
					monitorService.putExtra("pid", pid);
					monitorService.putExtra("uid", uid);
					monitorService.putExtra("packageName", selectPackageName);
					monitorService.putExtra("startActivity", startActivity);
					startService(monitorService);
					isServiceStop = false;
					btHomeTest.setText(getString(R.string.stop_test));
				} else {
					btHomeTest.setText(getString(R.string.start_test));
					stopService(monitorService);
				}
			}
		}

	}

	private class GoBackClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (getString(R.string.start_test).equals(btHomeTest.getText().toString())) {
				finish();
			} else {
				new AlertDialog.Builder(CapabilityActivity.this).setTitle("确认").setMessage("当前正在测试中，返回将停止测试，请确认？").setCancelable(false)
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								if (monitorService != null) {
									Log.d(LOG_TAG, "stop service");
									stopService(monitorService);
								}
								btHomeTest.setText(getString(R.string.start_test));
								finish();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						}).show();
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
			layGoBack.performClick();
		}
		return super.onKeyDown(keyCode, event);
	}

	private class CbCpuClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			boolean isCpuChecked = cb_cpu.isChecked();
			cb_cpu.setChecked(!isCpuChecked);
			preferences.edit().putBoolean(Settings.KEY_HOME_CPU, !isCpuChecked).commit();
		}

	}

	private class CbMemoryClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			boolean isMemoryChecked = cb_memory.isChecked();
			cb_memory.setChecked(!isMemoryChecked);
			preferences.edit().putBoolean(Settings.KEY_HOME_MEMORY, !isMemoryChecked).commit();
		}

	}

	private class CbTrafficClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			boolean isTrafficChecked = cb_traffic.isChecked();
			cb_traffic.setChecked(!isTrafficChecked);
			preferences.edit().putBoolean(Settings.KEY_HOME_TRAFFIC, !isTrafficChecked).commit();
		}

	}

	private class CbBatteryClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			boolean isBatteryChecked = cb_battery.isChecked();
			cb_battery.setChecked(!isBatteryChecked);
			preferences.edit().putBoolean(Settings.KEY_HOME_BATTERY, !isBatteryChecked).commit();
		}

	}

	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceStop = intent.getExtras().getBoolean("isServiceStop");
			if (isServiceStop) {
				btHomeTest.setText(getString(R.string.start_test));
			}
		}
	}

	@Override
	protected void onStart() {
		Log.d(LOG_TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");
		if (isServiceStop) {
			btHomeTest.setText(getString(R.string.start_test));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private void waitForAppStart(String packageName) {
		Log.d(LOG_TAG, "wait for app start");
		boolean isProcessStarted = false;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + TIMEOUT) {
			processList = processInfo.getRunningProcess(getBaseContext());
			for (Programe programe : processList) {
				if ((programe.getPackageName() != null) && (programe.getPackageName().equals(packageName))) {
					pid = programe.getPid();
					Log.d(LOG_TAG, "pid:" + pid);
					uid = programe.getUid();
					Log.d(LOG_TAG, "uid:" + uid);
					if (pid != 0) {
						isProcessStarted = true;
						break;
					}
				}
			}
			if (isProcessStarted) {
				break;
			}
		}
	}

	/**
	 * 根据值, 设置spinner默认选中:
	 * 
	 * @param spinner
	 * @param value
	 */
	public static void setSpinnerItemSelectedByValue(Spinner spinner, String value) {
		SpinnerAdapter spAdapter = spinner.getAdapter(); // 得到SpinnerAdapter对象
		int k = spAdapter.getCount();
		for (int i = 0; i < k; i++) {
			if (value.equals(spAdapter.getItem(i).toString())) {
				spinner.setSelection(i, true);// 默认选中项
				break;
			}
		}
	}

}

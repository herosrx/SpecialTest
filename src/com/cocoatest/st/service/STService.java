package com.cocoatest.st.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoatest.st.activity.BaseActivity;
import com.cocoatest.st.activity.HomeActivity;
import com.cocoatest.st.activity.MainTabActivity;
import com.cocoatest.st.utils.Constants;
import com.cocoatest.st.utils.CpuInfo;
import com.cocoatest.st.utils.CurrentInfo;
import com.cocoatest.st.utils.DateTimeUtils;
import com.cocoatest.st.utils.MemoryInfo;
import com.cocoatest.st.utils.MyApplication;
import com.cocoatest.st.utils.Settings;
import com.cocoatest.st.R;

public class STService extends Service {

	private final static String LOG_TAG = "SpecialTest-" + STService.class.getSimpleName();

	private static final String BLANK_STRING = "";

	private WindowManager windowManager = null;
	private WindowManager.LayoutParams wmParams = null;
	private View viFloatingWindow;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	private TextView txtMemInfo;
	private TextView txtCpuInfo;
	private TextView txtTrafficInfo;
	private TextView txtBatInfo;
	private Button btnStop;
	private Button btnWifi;
	private int delaytime;
	private DecimalFormat fomart;
	private MemoryInfo memoryInfo;
	private WifiManager wifiManager;
	private Handler handler = new Handler();
	private CpuInfo cpuInfo;

	private boolean isCpu;
	private boolean isMemory;
	private boolean isTraffic;
	private boolean isBattery;
	private boolean isFloating;
	private boolean isRoot;
	private String processName, packageName, startActivity;
	private int pid, uid;
	private boolean isServiceStop = false;

	public static BufferedWriter bw;
	public static FileOutputStream out;
	public static OutputStreamWriter osw;
	public static String resultFilePath;
	public static boolean isStop = false;

	private String totalBatt;
	private String temperature;
	private String voltage;
	private CurrentInfo currentInfo;
	private BatteryInfoBroadcastReceiver batteryBroadcast = null;

	// get start time
	private static final int MAX_START_TIME_COUNT = 5;
	private static final String START_TIME = "#startTime";
	private int getStartTimeCount = 0;
	private boolean isGetStartTime = true;
	private String startTime = "";
	public static final String SERVICE_ACTION = "com.cocoatest.action.STService";

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "service onCreate");
		super.onCreate();
		isServiceStop = false;
		isStop = false;
		memoryInfo = new MemoryInfo();
		fomart = new DecimalFormat();
		fomart.setMaximumFractionDigits(2);
		fomart.setMinimumFractionDigits(0);
		currentInfo = new CurrentInfo();
		batteryBroadcast = new BatteryInfoBroadcastReceiver();
		registerReceiver(batteryBroadcast, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
	}

	/**
	 * 电池信息监控监听器
	 * 
	 * @author andrewleo
	 * 
	 */
	public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				totalBatt = String.valueOf(level * 100 / scale);
				voltage = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
				temperature = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
			}

		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "service onStart");
		PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(this, HomeActivity.class), 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.icon).setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setContentTitle("SpecialTest");
		startForeground(startId, builder.build());

		pid = intent.getExtras().getInt("pid");
		uid = intent.getExtras().getInt("uid");
		processName = intent.getExtras().getString("processName");
		packageName = intent.getExtras().getString("packageName");
		startActivity = intent.getExtras().getString("startActivity");

		cpuInfo = new CpuInfo(getBaseContext(), pid, Integer.toString(uid));
		readSettingInfo();
		if (isFloating) {
			viFloatingWindow = LayoutInflater.from(this).inflate(R.layout.floating, null);
			txtMemInfo = (TextView) viFloatingWindow.findViewById(R.id.meminfo);
			txtCpuInfo = (TextView) viFloatingWindow.findViewById(R.id.cpuinfo);
			txtTrafficInfo = (TextView) viFloatingWindow.findViewById(R.id.trafficinfo);
			txtBatInfo = (TextView) viFloatingWindow.findViewById(R.id.batteryinfo);
			btnWifi = (Button) viFloatingWindow.findViewById(R.id.wifi);

			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()) {
				btnWifi.setText(R.string.close_wifi);
			} else {
				btnWifi.setText(R.string.open_wifi);
			}
			txtMemInfo.setText(getString(R.string.calculating));
			txtMemInfo.setTextColor(android.graphics.Color.RED);
			txtCpuInfo.setTextColor(android.graphics.Color.RED);
			txtTrafficInfo.setTextColor(android.graphics.Color.RED);
			txtBatInfo.setTextColor(android.graphics.Color.RED);
			btnStop = (Button) viFloatingWindow.findViewById(R.id.stop);
			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Builder builder = new AlertDialog.Builder(STService.this);
					builder.setTitle("确认");
					builder.setMessage("测试完成");
					builder.setPositiveButton("查看报告", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setClass(STService.this, MainTabActivity.class);
							intent.putExtra("tag", "report_tab");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							
						}
					});
					builder.setNegativeButton("留在这里", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
					final AlertDialog dialog = builder.create();
					dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					Handler handler = new Handler(getMainLooper());
					handler.post(new Runnable(){
					    @Override
						public void run(){
					    	dialog.show();
					    }
					});
					Intent intent = new Intent();
					intent.putExtra("isServiceStop", true);
					intent.setAction(SERVICE_ACTION);
					sendBroadcast(intent);
					stopSelf();
				}
			});
			createFloatingWindow();
		}
		createResultCsv();
		handler.postDelayed(task, 1000);
		return START_NOT_STICKY;
	}

	/**
	 * read configuration file.
	 * 
	 * @throws IOException
	 */
	private void readSettingInfo() {
		SharedPreferences preferences = Settings.getDefaultSharedPreferences(getApplicationContext());

		int interval = preferences.getInt(Settings.KEY_HOME_INTERVAL, 5);
		delaytime = interval * 1000;
		isCpu = preferences.getBoolean(Settings.KEY_HOME_CPU, true);
		isMemory = preferences.getBoolean(Settings.KEY_HOME_MEMORY, true);
		isTraffic = preferences.getBoolean(Settings.KEY_HOME_TRAFFIC, true);
		isBattery = preferences.getBoolean(Settings.KEY_HOME_BATTERY, true);
		isFloating = preferences.getBoolean(Settings.KEY_ISFLOAT, true);
		isRoot = preferences.getBoolean(Settings.KEY_ROOT, false);
		Log.d(LOG_TAG, "isCpu = " + isCpu);
		Log.d(LOG_TAG, "isMemory = " + isMemory);
		Log.d(LOG_TAG, "isTraffic = " + isTraffic);
		Log.d(LOG_TAG, "isBattery = " + isBattery);
		Log.d(LOG_TAG, "isFloating = " + isFloating);
		Log.d(LOG_TAG, "isRoot = " + isRoot);
	}

	/**
	 * write the test result to csv format report.
	 */
	private void createResultCsv() {
		String heapData = "";
		String mDateTime = DateTimeUtils.getDateTime();
		resultFilePath = BaseActivity.path + "ST_TestResult_" + mDateTime + ".csv";
		try {
			File resultFile = new File(resultFilePath);
			if (!resultFile.exists()) {
				File dirFile = new File(BaseActivity.path);
				dirFile.mkdir();
				resultFile.createNewFile();
			}
			out = new FileOutputStream(resultFile);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			long totalMemorySize = memoryInfo.getTotalMemory();
			String totalMemory = fomart.format((double) totalMemorySize / 1024);
			String multiCpuTitle = BLANK_STRING;
			// titles of multiple cpu cores
			ArrayList<String> cpuList = cpuInfo.getCpuList();
			for (int i = 0; i < cpuList.size(); i++) {
				multiCpuTitle += Constants.COMMA + cpuList.get(i) + getString(R.string.total_usage);
			}
			bw.write(getString(R.string.process_package) + ": ," + packageName + Constants.LINE_END + getString(R.string.process_name) + ": ,"
					+ processName + Constants.LINE_END + getString(R.string.process_pid) + ": ," + String.valueOf(pid) + Constants.LINE_END
					+ getString(R.string.mem_size) + ": ," + totalMemory + Constants.COMMA + Constants.LINE_END + getString(R.string.cpu_type) + ": ,"
					+ cpuInfo.getCpuName() + Constants.LINE_END + getString(R.string.android_system_version) + ": ," + memoryInfo.getSDKVersion()
					+ Constants.LINE_END + getString(R.string.mobile_type) + ": ," + memoryInfo.getPhoneType() + Constants.LINE_END + "UID" + ": ,"
					+ String.valueOf(uid) + Constants.LINE_END);

			if (isGrantedReadLogsPermission()) {
				bw.write(START_TIME);
			}
			if (isRoot) {
				heapData = getString(R.string.native_heap) + Constants.COMMA + getString(R.string.dalvik_heap) + Constants.COMMA;
			}
			bw.write(getString(R.string.timestamp) + Constants.COMMA + getString(R.string.top_activity) + Constants.COMMA + heapData
					+ getString(R.string.used_mem_PSS) + Constants.COMMA + getString(R.string.used_mem_ratio) + Constants.COMMA
					+ getString(R.string.mobile_free_mem) + Constants.COMMA + getString(R.string.app_used_cpu_ratio) + Constants.COMMA
					+ getString(R.string.total_used_cpu_ratio) + multiCpuTitle + Constants.COMMA + getString(R.string.send_traffic) + "(KB)"
					+ Constants.COMMA + getString(R.string.rcv_traffic) + "(KB)" + Constants.COMMA + getString(R.string.battery) + Constants.COMMA
					+ getString(R.string.current) + Constants.COMMA + getString(R.string.temperature) + Constants.COMMA + getString(R.string.voltage)
					+ Constants.LINE_END);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	/**
	 * create a floating window to show real-time data.
	 */
	private void createFloatingWindow() {
		SharedPreferences shared = getSharedPreferences("float_flag", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("float", 1);
		editor.commit();
		// 要实现在App所在进程中运行的悬浮窗口，当然是得要获取CompatModeWrapper，而不是LocalWindowManger。
		windowManager = (WindowManager) getApplicationContext().getSystemService("window");
		wmParams = ((MyApplication) getApplication()).getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = LayoutParams.WRAP_CONTENT;
		wmParams.height = LayoutParams.WRAP_CONTENT;
		wmParams.format = 1;
		windowManager.addView(viFloatingWindow, wmParams);
		viFloatingWindow.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY() - 25;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();
					// showImg();
					mTouchStartX = mTouchStartY = 0;
					break;
				}
				return true;
			}
		});

		btnWifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					btnWifi = (Button) viFloatingWindow.findViewById(R.id.wifi);
					String buttonText = (String) btnWifi.getText();
					String wifiText = getResources().getString(R.string.open_wifi);
					if (buttonText.equals(wifiText)) {
						wifiManager.setWifiEnabled(true);
						btnWifi.setText(R.string.close_wifi);
					} else {
						wifiManager.setWifiEnabled(false);
						btnWifi.setText(R.string.open_wifi);
					}
				} catch (Exception e) {
					Toast.makeText(viFloatingWindow.getContext(), getString(R.string.wifi_fail_toast), Toast.LENGTH_LONG).show();
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	private Runnable task = new Runnable() {

		@Override
		public void run() {
			if (!isServiceStop) {
				dataRefresh();
				handler.postDelayed(this, delaytime);
				if (isFloating && viFloatingWindow != null) {
					windowManager.updateViewLayout(viFloatingWindow, wmParams);
				}
				// get app start time from logcat on every task running
				getStartTimeFromLogcat();
			} else {
				Intent intent = new Intent();
				intent.putExtra("isServiceStop", true);
				intent.setAction(SERVICE_ACTION);
				sendBroadcast(intent);
				stopSelf();
			}
		}
	};

	/**
	 * Try to get start time from logcat.
	 */
	private void getStartTimeFromLogcat() {
		if (!isGetStartTime || getStartTimeCount >= MAX_START_TIME_COUNT) {
			return;
		}
		try {
			// filter logcat by Tag:ActivityManager and Level:Info
			String logcatCommand = "logcat -v time -d ActivityManager:I *:S";
			Process process = Runtime.getRuntime().exec(logcatCommand);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder strBuilder = new StringBuilder();
			String line = BLANK_STRING;

			while ((line = bufferedReader.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append(Constants.LINE_END);
				String regex = ".*Displayed.*" + startActivity + ".*\\+(.*)ms.*";
				if (line.matches(regex)) {
					Log.w("my logs", line);
					if (line.contains("total")) {
						line = line.substring(0, line.indexOf("total"));
					}
					startTime = line.substring(line.lastIndexOf("+") + 1, line.lastIndexOf("ms") + 2);
					Toast.makeText(STService.this, getString(R.string.start_time) + startTime, Toast.LENGTH_LONG).show();
					isGetStartTime = false;
					break;
				}
			}
			getStartTimeCount++;
		} catch (IOException e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	/**
	 * Above JellyBean, we cannot grant READ_LOGS permission...
	 * 
	 * @return
	 */
	private boolean isGrantedReadLogsPermission() {
		int permissionState = getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, getPackageName());
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * refresh the performance data showing in floating window.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 */
	private void dataRefresh() {
		int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
		long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
		String freeMemoryKb = fomart.format((double) freeMemory / 1024);
		String processMemory = fomart.format((double) pidMemory / 1024);
		String currentBatt = String.valueOf(currentInfo.getCurrentValue());
		// 异常数据过滤
		try {
			if (Math.abs(Double.parseDouble(currentBatt)) >= 500) {
				currentBatt = Constants.NA;
			}
		} catch (Exception e) {
			currentBatt = Constants.NA;
		}
		ArrayList<String> processInfo = cpuInfo.getCpuRatioInfo(totalBatt, currentBatt, temperature, voltage, isRoot);
		if (isFloating) {
			String processCpuRatio = "0.00";
			String totalCpuRatio = "0.00";
			String sndTrafficSize = "0";
			String rcvTrafficSize = "0";
			String sndTraffic = "";
			String rcvTraffic = "";
//			double tempSndTraffic = 0;
//			double tempRcvTraffic = 0;
//			double sndTrafficMb = 0;
//			double rcvTrafficMb = 0;
//			boolean isMb_snd = false;
//			boolean isMb_rcv = false;
			if (!processInfo.isEmpty()) {
				processCpuRatio = processInfo.get(0);
				totalCpuRatio = processInfo.get(1);
				sndTrafficSize = processInfo.get(2);
				rcvTrafficSize = processInfo.get(3);
				// 如果cpu使用率存在且都不小于0，则输出
				if (processCpuRatio != null && totalCpuRatio != null) {
					if (isMemory) {
						txtMemInfo.setText(getString(R.string.process_free_mem) + processMemory + "/" + freeMemoryKb + "MB");
					} else {
						txtMemInfo.setVisibility(View.GONE);
					}
					if (isCpu) {
						txtCpuInfo.setText(getString(R.string.process_overall_cpu) + processCpuRatio + "%/" + totalCpuRatio + "%");
					} else {
						txtCpuInfo.setVisibility(View.GONE);
					}
					if ("-1".equals(sndTrafficSize)) {
						sndTraffic = Constants.NA;
					} else {
						sndTraffic = sndTrafficSize + "KB";
					}
					if ("-1".equals(rcvTrafficSize)) {
						rcvTraffic = Constants.NA;
					} else {
						rcvTraffic = rcvTrafficSize + "KB";
					}
					if (isTraffic) {
						txtTrafficInfo.setText(getString(R.string.traffic) + getString(R.string.send_traffic) + sndTraffic + ","
								+ getString(R.string.rcv_traffic) + rcvTraffic);
					} else {
						txtTrafficInfo.setVisibility(View.GONE);
					}
					if (isBattery) {
						txtBatInfo.setText(getString(R.string.current) + currentBatt);
					} else {
						txtBatInfo.setVisibility(View.GONE);
					}
				}
				// 当内存为0切cpu使用率为0时则是被测应用退出
				if ("0".equals(processMemory)) {
					closeOpenedStream();
					isServiceStop = true;
					return;
				}
			}

		}
	}

	/**
	 * update the position of floating window.
	 */
	private void updateViewPosition() {
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		if (viFloatingWindow != null) {
			windowManager.updateViewLayout(viFloatingWindow, wmParams);
		}
	}

	/**
	 * close all opened stream.
	 */
	public void closeOpenedStream() {
		try {
			if (bw != null) {
				bw.write(getString(R.string.comment1) + Constants.LINE_END + getString(R.string.comment2) + Constants.LINE_END
						+ getString(R.string.comment3) + Constants.LINE_END + getString(R.string.comment4) + Constants.LINE_END);
				bw.close();
			}
			if (osw != null)
				osw.close();
			if (out != null)
				out.close();
		} catch (Exception e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "service onDestroy");
		if (windowManager != null) {
			windowManager.removeView(viFloatingWindow);
			viFloatingWindow = null;
		}
		handler.removeCallbacks(task);
		closeOpenedStream();
		// replace the start time in file
		if (!BLANK_STRING.equals(startTime)) {
			replaceFileString(resultFilePath, START_TIME, getString(R.string.start_time) + startTime + Constants.LINE_END);
		} else {
			replaceFileString(resultFilePath, START_TIME, BLANK_STRING);
		}
		isStop = true;
		unregisterReceiver(batteryBroadcast);
//		Toast.makeText(this, R.string.test_finish_toast, Toast.LENGTH_LONG).show();
		super.onDestroy();
		stopForeground(true);
	}

	/**
	 * Replaces all matches for replaceType within this replaceString in file on
	 * the filePath
	 * 
	 * @param filePath
	 * @param replaceType
	 * @param replaceString
	 */
	private void replaceFileString(String filePath, String replaceType, String replaceString) {
		try {
			File file = new File(filePath);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = BLANK_STRING;
			String oldtext = BLANK_STRING;
			while ((line = reader.readLine()) != null) {
				oldtext += line + Constants.LINE_END;
			}
			reader.close();
			// replace a word in a file
			String newtext = oldtext.replaceAll(replaceType, replaceString);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), getString(R.string.csv_encoding)));
			writer.write(newtext);
			writer.close();
		} catch (IOException e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
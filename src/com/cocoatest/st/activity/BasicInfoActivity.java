package com.cocoatest.st.activity;

import java.text.DecimalFormat;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.CpuInfo;
import com.cocoatest.st.utils.MemoryInfo;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BasicInfoActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + BasicInfoActivity.class.getSimpleName();

	private ImageView btnSave;
	private LinearLayout layGoBack;
	private TextView title;

	private TextView tvBrand;
	private TextView tvModel;
	private TextView tvAndroidVersion;
	private TextView tvAndroidName;
	private TextView tvCpuModel;
	private TextView tvCpuNum;
	private TextView tvMem;
	private TextView tvScreenHeight;
	private TextView tvScreenWidth;
	private TextView tvImei;
	private TextView tvImsi;
	private TextView tvMac;
	private TextView tvSerial;
	private TextView tvOperator;

	private CpuInfo cpuInfo;
	private MemoryInfo memInfo;
	private DisplayMetrics dm;
	private TelephonyManager tm;

	private DecimalFormat format;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.basicinfo_device);
		cpuInfo = new CpuInfo();
		memInfo = new MemoryInfo();
		dm = new DisplayMetrics();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(2);
		initViews();
		layGoBack.setOnClickListener(new GoBackClickListener());
		tvBrand.setText(Build.BRAND);
		tvModel.setText(Build.MODEL);
		tvAndroidVersion.setText(Build.VERSION.RELEASE);
		tvAndroidName.setText(Build.VERSION.SDK);
		tvCpuModel.setText(cpuInfo.getCpuName());
		tvCpuNum.setText(cpuInfo.getCpuNum() + "");
		double totalMem = (double) (memInfo.getTotalMemory()) / (1024 * 1024);
		Log.d(LOG_TAG, "memory===" + totalMem);
		tvMem.setText(format.format(totalMem) + "GB");
		tvScreenHeight.setText(dm.heightPixels + "");
		tvScreenWidth.setText(dm.widthPixels + "");
		tvImei.setText(tm.getDeviceId());
		tvImsi.setText(tm.getSubscriberId());
		tvMac.setText(getMac());
		tvSerial.setText(Build.SERIAL);
		tvOperator.setText(getOperator(tm));
	}

	private void initViews() {
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		title.setText(R.string.device_basic_info);
		btnSave.setVisibility(View.INVISIBLE);

		tvBrand = (TextView) findViewById(R.id.device_basic_brand);
		tvModel = (TextView) findViewById(R.id.device_basic_model);
		tvAndroidVersion = (TextView) findViewById(R.id.device_basic_android_version);
		tvAndroidName = (TextView) findViewById(R.id.device_basic_android_name);
		tvCpuModel = (TextView) findViewById(R.id.device_basic_cpu_model);
		tvCpuNum = (TextView) findViewById(R.id.device_basic_cpu_num);
		tvMem = (TextView) findViewById(R.id.device_basic_mem);
		tvScreenHeight = (TextView) findViewById(R.id.device_basic_screen_height);
		tvScreenWidth = (TextView) findViewById(R.id.device_basic_screen_width);
		tvImei = (TextView) findViewById(R.id.device_basic_imei);
		tvImsi = (TextView) findViewById(R.id.device_basic_imsi);
		tvMac = (TextView) findViewById(R.id.device_basic_mac);
		tvSerial = (TextView) findViewById(R.id.device_basic_serial);
		tvOperator = (TextView) findViewById(R.id.device_basic_operator);
	}

	private class GoBackClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}
	}

	private String getMac() {
		String result = "";
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		result = wifiInfo.getMacAddress();
		return result;
	}

	private String getOperator(TelephonyManager tm) {
		String result = "";
		String operator = tm.getSimOperator();
		if (operator != null) {

			if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
				result = "中国移动";
			} else if (operator.equals("46001")) {
				result = "中国联通";
			} else if (operator.equals("46003")) {
				result = "中国电信";
			}
		}
		return result;
	}
}

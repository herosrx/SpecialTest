package com.cocoatest.st.activity;

import com.cocoatest.st.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity implements OnCheckedChangeListener {

	private TabHost mHost;
	private Intent mHomeIntent;
	private Intent mReportIntent;
	private Intent mSettingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.maintabs);

		// ~~~~~~~~~~~~ 初始化
		this.mHomeIntent = new Intent(this, HomeActivity.class);
		this.mReportIntent = new Intent(this, ReportActivity.class);
		this.mSettingIntent = new Intent(this, SettingsActivity.class);

		initRadios();

		setupIntent();
		Intent intent = getIntent();
		String tag = intent.getStringExtra("tag");
		if(!"".equals(tag)){
			this.mHost.setCurrentTabByTag(tag);
		}
	}

	/**
	 * 初始化底部按钮
	 */
	private void initRadios() {
		((RadioButton) findViewById(R.id.radio_home)).setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_report)).setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_setting)).setOnCheckedChangeListener(this);
	}

	/**
	 * 切换模块
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.radio_home:
				this.mHost.setCurrentTabByTag("home_tab");
				break;
			case R.id.radio_report:
				this.mHost.setCurrentTabByTag("report_tab");
				break;
			case R.id.radio_setting:
				this.mHost.setCurrentTabByTag("setting_tab");
				break;
			}
		}
	}

	private void setupIntent() {
		this.mHost = getTabHost();
		TabHost localTabHost = this.mHost;

		localTabHost.addTab(buildTabSpec("home_tab", R.string.home, R.drawable.icon, this.mHomeIntent));
		localTabHost.addTab(buildTabSpec("report_tab", R.string.report, R.drawable.icon, this.mReportIntent));
		localTabHost.addTab(buildTabSpec("setting_tab", R.string.setting, R.drawable.icon, this.mSettingIntent));

	}

	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon, final Intent content) {
		return this.mHost.newTabSpec(tag).setIndicator(getString(resLabel), getResources().getDrawable(resIcon)).setContent(content);
	}

}

package com.cocoatest.st.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.Settings;

public class HomeActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + HomeActivity.class.getSimpleName();

	private LinearLayout layGoBack;
	private TextView title;
	private ImageView btnSave;
	private ImageView homeAppIcon;
	private TextView homeAppName;
	private RelativeLayout selectApp;

	public static final int HOME_REQUSET = 1;
	private Bitmap bt_app_icon;
	private String selectProcessName;
	private String selectPackageName;

	private GridView gridView;
	private ArrayList<HashMap<String, Object>> funcList;
	
	private SharedPreferences preferences;
	private PackageManager pm;
	private ApplicationInfo appInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
		preferences = Settings.getDefaultSharedPreferences(HomeActivity.this);
		initViews();
		selectPackageName  = preferences.getString(Settings.KEY_SELECTED_PACKAGE, "");
		//如果用户之前选择过应用程序，就通过存的包名取得对应的图标和名称显示出来
		if(!"".equals(selectPackageName)){
			pm = getPackageManager();
			try {
				appInfo =pm.getApplicationInfo(selectPackageName, PackageManager.GET_META_DATA);
				homeAppIcon.setVisibility(View.VISIBLE);
				homeAppIcon.setImageDrawable(pm.getApplicationIcon(appInfo));
				selectProcessName = (String) pm.getApplicationLabel(appInfo);
				homeAppName.setText(selectProcessName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		selectApp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (isFastClick(2000)) {
					return;
				}
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, SelectAppActivity.class);
				startActivityForResult(intent, HOME_REQUSET);
			}
		});
		setGridView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HOME_REQUSET && resultCode == RESULT_OK) {
			bt_app_icon = data.getParcelableExtra("select_app_icon");
			selectProcessName = data.getStringExtra("select_app_name");
			selectPackageName = data.getStringExtra("select_app_packagename");
			homeAppIcon.setImageBitmap(bt_app_icon);
			homeAppIcon.setVisibility(View.VISIBLE);
			homeAppName.setText(selectProcessName);
			preferences.edit().putString(Settings.KEY_SELECTED_PACKAGE, selectPackageName).commit();
			Log.d(LOG_TAG, "select_app_name=====" + selectProcessName);
			Log.d(LOG_TAG, "select_app_packagename=====" + selectPackageName);
		}
	}

	private void initViews() {
		// 初始化titile的属性
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		layGoBack.setVisibility(View.INVISIBLE);
		title.setText(R.string.home);
		btnSave.setVisibility(View.INVISIBLE);
		// 初始化选择app框的属性
		selectApp = (RelativeLayout) findViewById(R.id.ll_home_select_app);
		homeAppIcon = (ImageView) findViewById(R.id.home_app_icon);
		homeAppName = (TextView) findViewById(R.id.home_app_name);

		gridView = (GridView) findViewById(R.id.gridView);
	}

	
	
	
	private void setGridView() {
		funcList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map_capability = new HashMap<String, Object>();
		map_capability.put("item_image", R.drawable.capability);
		map_capability.put("item_name", getString(R.string.capability_itemlabel));
		funcList.add(map_capability);
		HashMap<String, Object> map_monkey = new HashMap<String, Object>();
		map_monkey.put("item_image", R.drawable.monkey);
		map_monkey.put("item_name", getString(R.string.monkey_itemlabel));
		funcList.add(map_monkey);
		SimpleAdapter saFunctions = new SimpleAdapter(HomeActivity.this, funcList, R.layout.function_item,
				new String[] { "item_image", "item_name" }, new int[] { R.id.item_image, R.id.item_name });
		gridView.setAdapter(saFunctions);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					if (!"".equals(selectPackageName)) {
						Intent capabilityIntent = new Intent();
						capabilityIntent.setClass(HomeActivity.this, CapabilityActivity.class);
						capabilityIntent.putExtra("selectProcessName", selectProcessName);
						capabilityIntent.putExtra("selectPackageName", selectPackageName);
						startActivity(capabilityIntent);
					} else {
						Toast.makeText(HomeActivity.this, getString(R.string.tv_select_app), Toast.LENGTH_LONG).show();
					}
					break;
				case 1:
					if (!"".equals(selectPackageName)) {
						Intent monkeyIntent = new Intent();
						monkeyIntent.setClass(HomeActivity.this, MonkeyActivity.class);
						monkeyIntent.putExtra("selectProcessName", selectProcessName);
						monkeyIntent.putExtra("selectPackageName", selectPackageName);
						startActivity(monkeyIntent);
					} else {
						Toast.makeText(HomeActivity.this, getString(R.string.tv_select_app), Toast.LENGTH_LONG).show();
					}
					break;
				}
			}
		});
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

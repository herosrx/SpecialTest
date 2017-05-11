package com.cocoatest.st.activity;

import java.util.List;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.ProcessInfo;
import com.cocoatest.st.utils.Programe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SelectAppActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + SelectAppActivity.class.getSimpleName();

	private LinearLayout layGoBack;
	private TextView title;
	private ImageView btnSave;

	private ProcessInfo processInfo;
	private ListView lstViProgramme;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.select_app);
		initViews();
		processInfo = new ProcessInfo();
		lstViProgramme.setAdapter(new ListAdapter());
		lstViProgramme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Programe pr = (Programe) adapterView.getItemAtPosition(i);
				Log.d(LOG_TAG, "programe name is:" + pr.getProcessName());
				Bitmap bitmap = ((BitmapDrawable) pr.getIcon()).getBitmap();
				Intent data = new Intent();
				data.putExtra("select_app_icon", bitmap);
				data.putExtra("select_app_name", pr.getProcessName());
				data.putExtra("select_app_packagename", pr.getPackageName());
				setResult(RESULT_OK, data);
				finish();
			}
		});
		layGoBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private void initViews() {
		// 初始化titile的属性
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		title.setText(R.string.title_select_app);
		btnSave.setVisibility(View.INVISIBLE);
		lstViProgramme = (ListView) findViewById(R.id.processList);
	}

	private class ListAdapter extends BaseAdapter {
		List<Programe> programes;

		public ListAdapter() {
			programes = processInfo.getRunningProcess(getBaseContext());
		}

		@Override
		public int getCount() {
			return programes.size();
		}

		@Override
		public Object getItem(int position) {
			return programes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Programe pr = programes.get(position);
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.applist_item, parent, false);
			Viewholder holder = (Viewholder) convertView.getTag();
			if (holder == null) {
				holder = new Viewholder();
				convertView.setTag(holder);
				holder.imgViAppIcon = (ImageView) convertView.findViewById(R.id.image);
				holder.txtAppName = (TextView) convertView.findViewById(R.id.text);
			}
			holder.imgViAppIcon.setImageDrawable(pr.getIcon());
			holder.txtAppName.setText(pr.getProcessName());
			return convertView;
		}
	}

	private class Viewholder {
		TextView txtAppName;
		ImageView imgViAppIcon;
	}

}

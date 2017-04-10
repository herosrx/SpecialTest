package com.cocoatest.st.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.MailSender;
import com.cocoatest.st.utils.Report;
import com.cocoatest.st.utils.Settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReportActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + ReportActivity.class.getSimpleName();
	private static final String BLANK_STRING = "";

	private LinearLayout layGoBack;
	private TextView title;
	private ImageView btnSave;

	private String recipients;
	private String[] receivers;
	private String selectedMails;
	private String[] selectMail;

	private TextView tvReportNull;
	private ListView listReport;
	private List<Report> reports;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.report);
		initViews();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(LOG_TAG, "resume");
		getMailSetting();
		reports = GetFiles(new File(path));
		if (reports.size() == 0) {
			tvReportNull.setVisibility(View.VISIBLE);
		} else {
			tvReportNull.setVisibility(View.GONE);
			listReport.setAdapter(new ReportAdapter());
		}

	}

	private void initViews() {
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		layGoBack.setVisibility(View.INVISIBLE);
		title.setText(R.string.report);
		btnSave.setVisibility(View.INVISIBLE);

		tvReportNull = (TextView) findViewById(R.id.tv_report_null);
		listReport = (ListView) findViewById(R.id.reportList);

	}

	private void getMailSetting() {
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		recipients = preferences.getString(Settings.KEY_RECIPIENTS, BLANK_STRING);
		Log.d(LOG_TAG, "recipients == " + recipients);
	}

	private class ReportAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return reports.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return reports.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final Report report = reports.get(position);
			Viewholder holder = null;
			if (convertView == null) {
				holder = new Viewholder();
				convertView = getLayoutInflater().inflate(R.layout.reportlist_item, parent, false);
				holder.reportName = (TextView) convertView.findViewById(R.id.tv_reportname);
				holder.delReport = (Button) convertView.findViewById(R.id.bt_deletereport);
				holder.sendReport = (Button) convertView.findViewById(R.id.bt_sendreport);
				convertView.setTag(holder);
			} else {
				holder = (Viewholder) convertView.getTag();
			}
			holder.reportName.setText(report.getName());
			holder.reportName.setSelected(true);
			holder.delReport.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(ReportActivity.this).setTitle("确认").setMessage("确认删除吗？").setCancelable(false)
							.setPositiveButton("确认", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									delReport(report.getPath());
									reports = GetFiles(new File(path));
									if (reports.size() == 0) {
										tvReportNull.setVisibility(View.VISIBLE);
									} else {
										tvReportNull.setVisibility(View.GONE);
										listReport.setAdapter(new ReportAdapter());
									}
								}

							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.cancel();
								}
							}).show();
				}
			});
			holder.sendReport.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (!"".equals(recipients.trim())) {
						receivers = (recipients.trim()).split("\\s+");
						final boolean[] isSelectedMail = new boolean[receivers.length];
						for (int i = 0; i < receivers.length; i++) {
							isSelectedMail[i] = false;
						}
						new AlertDialog.Builder(ReportActivity.this).setTitle("选择邮箱").setCancelable(false)
								.setMultiChoiceItems(receivers, null, new DialogInterface.OnMultiChoiceClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
										// TODO Auto-generated method
										isSelectedMail[arg1] = arg2;
									}
								}).setPositiveButton("发送", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method
										// stub
										selectedMails = "";
										for (int i = 0; i < receivers.length; i++) {
											if (isSelectedMail[i]) {
												selectedMails = selectedMails + receivers[i] + " ";
											}
										}
										Log.d(LOG_TAG, "selectedMails == " + selectedMails.trim());
										selectMail = (selectedMails.trim()).split("\\s+");
										if (!"".equals(selectedMails.trim())) {
											SendMailTask task = new SendMailTask();
											task.execute(report.getName(), report.getPath());
											controlDialog(arg0, true);
										} else {
											controlDialog(arg0, false);
											Toast.makeText(ReportActivity.this, "请选择要发送的邮箱", Toast.LENGTH_LONG).show();
										}
									}
								}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method
										// stub
										controlDialog(arg0, true);
										arg0.cancel();
									}
								}).show();
					} else {
						Intent intent = new Intent();
						intent.setClass(ReportActivity.this, MailSettingActivity.class);
						startActivity(intent);
//						Toast.makeText(ReportActivity.this, R.string.no_mail_toast, Toast.LENGTH_LONG).show();
					}
				}
			});
			return convertView;
		}
	}

	private class Viewholder {
		TextView reportName;
		Button delReport;
		Button sendReport;
	}

	// 发送邮件需要用异步任务，否则会报NetworkOnMainThreadException
	private class SendMailTask extends AsyncTask<String, Integer, Boolean> {

		boolean isSendSuccessfully = false;

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				isSendSuccessfully = MailSender.sendTextMail(getString(R.string.send_mail_subject) + params[0],
						getString(R.string.send_mail_content), params[1], selectMail);
			} catch (Exception e) {
				Log.e("SendMail", e.getMessage(), e);
				isSendSuccessfully = false;
			}
			return isSendSuccessfully;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result) {
				Toast.makeText(ReportActivity.this, getString(R.string.send_success_toast) + selectedMails, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(ReportActivity.this, getString(R.string.send_fail_toast), Toast.LENGTH_LONG).show();
			}
		}

	}

	/**
	 * 获取报告列表
	 * 
	 * @param filePath
	 */
	private List<Report> GetFiles(File filePath) {
		List<Report> reports = new ArrayList<Report>();
		File[] files = filePath.listFiles();

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					if (files[i].getName().startsWith("ST_TestResult_") || files[i].getName().startsWith("ST_MonkeyLog_")) {
						Report report = new Report();
						report.setName(files[i].getName());
						report.setPath(files[i].getPath());
						reports.add(report);
					}
				}
			}
		}
		return reports;
	}

	/**
	 * 根据指定路径删除SD卡中的文件
	 * 
	 * @param path
	 */
	private void delReport(String path) {
		File file = new File(path);
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			if (file.exists()) {
				if (file.isFile())
					file.delete();
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

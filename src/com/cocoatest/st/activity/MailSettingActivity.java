package com.cocoatest.st.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cocoatest.st.R;
import com.cocoatest.st.utils.Settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MailSettingActivity extends BaseActivity {

	private static final String LOG_TAG = "SpecialTest-" + MailSettingActivity.class.getSimpleName();
	private static final String BLANK_STRING = "";

	private ImageView btnSave;
	private LinearLayout layGoBack;
	private TextView title;
	private Button bt_addMail;

	private ListView listMail;
	private String strMail;
	private List<String> mails = new ArrayList<String>();
	private String strMails = "";

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mail_settings);
		initViews();
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		strMails = preferences.getString(Settings.KEY_RECIPIENTS, BLANK_STRING);
		Log.d(LOG_TAG, "strMails =" + strMails);
		mails = getListMail(strMails);
		layGoBack.setOnClickListener(new GoBackClickListener());
		bt_addMail.setOnClickListener(new AddMailClickListener());
		if (mails != null) {
			listMail.setAdapter(new ListAdapter());
		};
	}

	private void initViews() {
		layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		title = (TextView) findViewById(R.id.nb_title);
		btnSave = (ImageView) findViewById(R.id.btn_set);
		title.setText(R.string.mail_settings);
		btnSave.setVisibility(View.INVISIBLE);

		bt_addMail = (Button) findViewById(R.id.bt_add_mail);
		listMail = (ListView) findViewById(R.id.mailList);
	}

	private class AddMailClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			final EditText etMail = new EditText(MailSettingActivity.this);
			new AlertDialog.Builder(MailSettingActivity.this).setTitle("请输入").setIcon(android.R.drawable.ic_dialog_info).setView(etMail)
					.setCancelable(false).setPositiveButton("确认", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							strMail = etMail.getText().toString().trim();
							if (!BLANK_STRING.equals(strMail)) {
								if (checkMailFormat(strMail)) {
									mails.add(strMail);
									listMail.setAdapter(new ListAdapter());
									strMails = getStringMails(mails);
									Log.d(LOG_TAG, "strMails = " + strMails);
									preferences.edit().putString(Settings.KEY_RECIPIENTS, strMails).commit();
								} else {
									Toast.makeText(MailSettingActivity.this, R.string.format_incorrect_format, Toast.LENGTH_LONG).show();
								}
								controlDialog(arg0, true);
							} else {
								controlDialog(arg0, false);
								Toast.makeText(MailSettingActivity.this, "请填写邮箱", Toast.LENGTH_LONG).show();
							}
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							controlDialog(arg0, true);
							arg0.cancel();
						}
					}).show();
		}
	}

	private class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mails.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mails.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int pos = position;
			Viewholder holder = null;
			if (convertView == null) {
				holder = new Viewholder();
				convertView = getLayoutInflater().inflate(R.layout.maillist_item, parent, false);
				holder.mailName = (TextView) convertView.findViewById(R.id.tv_mail);
				holder.delMail = (Button) convertView.findViewById(R.id.bt_deletemail);
				holder.editMail = (Button) convertView.findViewById(R.id.bt_editmail);
				convertView.setTag(holder);
			} else {
				holder = (Viewholder) convertView.getTag();
			}
			holder.mailName.setText(mails.get(position));
			holder.mailName.setSelected(true);
			holder.delMail.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(MailSettingActivity.this).setTitle("确认").setMessage("确认删除吗？").setCancelable(false)
							.setPositiveButton("确认", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									mails.remove(pos);
									listMail.setAdapter(new ListAdapter());
									strMails = getStringMails(mails);
									Log.d(LOG_TAG, "strMails = " + strMails);
									preferences.edit().putString(Settings.KEY_RECIPIENTS, strMails).commit();
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									arg0.cancel();
								}
							}).show();
				}
			});
			holder.editMail.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					final EditText editMail = new EditText(MailSettingActivity.this);
					editMail.setText(mails.get(pos));
					new AlertDialog.Builder(MailSettingActivity.this).setTitle("请输入").setIcon(android.R.drawable.ic_dialog_info).setView(editMail)
							.setCancelable(false).setPositiveButton("确认", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									String str_mail = editMail.getText().toString();
									if (!str_mail.equals(mails.get(pos))) {
										if (!BLANK_STRING.equals(str_mail)) {
											if (checkMailFormat(str_mail)) {
												mails.remove(pos);
												mails.add(pos, str_mail);
												listMail.setAdapter(new ListAdapter());
												strMails = getStringMails(mails);
												Log.d(LOG_TAG, "strMails = " + strMails);
												preferences.edit().putString(Settings.KEY_RECIPIENTS, strMails).commit();
											} else {
												Toast.makeText(MailSettingActivity.this, R.string.format_incorrect_format, Toast.LENGTH_LONG).show();
											}
											controlDialog(arg0, true);
										} else {
											controlDialog(arg0, false);
											Toast.makeText(MailSettingActivity.this, "请填写邮箱", Toast.LENGTH_LONG).show();
										}
									}
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									controlDialog(arg0, true);
									arg0.cancel();
								}
							}).show();
				}
			});
			return convertView;
		}

	}

	private class GoBackClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}
	}

	private List<String> getListMail(String string) {
		List<String> lists = new ArrayList<String>();
		if (string != "") {
			String[] strings = (string.trim()).split("\\s+");
			for (int i = 0; i < strings.length; i++) {
				lists.add(strings[i]);
			}
		}
		return lists;
	}

	private String getStringMails(List<String> strings) {
		String string = "";
		for (int i = 0; i < strings.size(); i++) {
			string = string + strings.get(i) + " ";
		}
		return string;
	}

	/**
	 * check mail format
	 * 
	 * @return true: valid email address
	 */
	private boolean checkMailFormat(String mail) {
		String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*" + "[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(mail);
		return m.matches();
	}

	private class Viewholder {
		TextView mailName;
		Button delMail;
		Button editMail;
	}
}

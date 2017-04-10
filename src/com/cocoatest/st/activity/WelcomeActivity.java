package com.cocoatest.st.activity;

import com.cocoatest.st.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(WelcomeActivity.this, MainTabActivity.class);
				startActivity(intent);
				WelcomeActivity.this.finish();
			}
		}, 3000);
	}

}

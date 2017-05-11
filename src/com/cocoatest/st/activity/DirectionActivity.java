package com.cocoatest.st.activity;

import com.cocoatest.st.R;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DirectionActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.direction);
		TextView title = (TextView) findViewById(R.id.nb_title);
		title.setText(R.string.direction);

		ImageView btnSave = (ImageView) findViewById(R.id.btn_set);
		btnSave.setVisibility(View.INVISIBLE);
		
		LinearLayout layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);

		layGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DirectionActivity.this.finish();
			}
		});

	}

}

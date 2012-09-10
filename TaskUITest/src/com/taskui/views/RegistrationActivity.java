package com.taskui.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.taskui.models.Constants;

public final class RegistrationActivity extends Activity implements Constants {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.registration_activity);

		final EditText txtUsername = (EditText) findViewById(R.id.username);
		final EditText txtRealname = (EditText) findViewById(R.id.realname);
		final Button btnRegister = (Button) findViewById(R.id.button_register);

		btnRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String uname = txtUsername.getText().toString();
				String rname = txtRealname.getText().toString();

				if (uname != null && uname.length() > 0 && rname != null && rname.length() > 0) {
					TaskUIApplication.prefs.edit().putString(KEY_USERNAME, uname).putString(KEY_REALNAME, rname).commit();
					Intent intent = getIntent();
					if (Intent.ACTION_VIEW.equals(intent.getAction())) {
						intent.setClass(RegistrationActivity.this, TaskExecutionActivity.class);
					} else {
						intent.setClass(RegistrationActivity.this, TaskListActivity.class);
					}
					finish();
					startActivity(intent);
				} else {
					Toast.makeText(RegistrationActivity.this, "'username' and 'real name' can't be empty!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}

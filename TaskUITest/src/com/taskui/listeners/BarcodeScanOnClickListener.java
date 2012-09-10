package com.taskui.listeners;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.zxing.integration.android.IntentIntegrator;

public class BarcodeScanOnClickListener implements OnClickListener {

	private Activity activity;
	private View view;
	
	public BarcodeScanOnClickListener(Activity _activity, View _view) {
		this.activity = _activity;
		this.view = _view;
	}

	@Override
	public void onClick(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this.activity);
		integrator.initiateScan();
	}

	public View getListenerView() {
		return this.view;
	}
}
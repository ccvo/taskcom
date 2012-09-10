package com.taskui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BarcodeView extends LinearLayout {
	private EditText editText;
	private Button btnScan;
	
	public BarcodeView(Context context) {
		super(context);
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		this.setOrientation(VERTICAL);

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		editText = (EditText) layoutInflater.inflate(R.layout.an_edit_text, this, false);
		this.addView(editText);
		editText.setHint("barcode");
		editText.setKeyListener(null);

		this.btnScan = (Button) layoutInflater.inflate(R.layout.a_button, this, false);
		this.btnScan.setText(R.string.button_label_tap_to_scan);
		this.addView(this.btnScan);
	}

	public String getText() {
		return editText.getText().toString();
	}

	public Button getScanButton() {
		return this.btnScan;
	}

	public View getEditText() {
		return this.editText;
	}
}

package com.taskui.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.taskui.listeners.BarcodeScanOnClickListener;
import com.taskui.models.Constants;
import com.taskui.models.Response;
import com.taskui.models.SharedTask;
import com.taskui.models.ui.Img;
import com.taskui.models.ui.Input;
import com.taskui.models.ui.Listener;
import com.taskui.models.ui.Listview;
import com.taskui.models.ui.Mapview;
import com.taskui.models.ui.Option;
import com.taskui.models.ui.Select;
import com.taskui.models.ui.Slider;
import com.taskui.models.ui.Textview;
import com.taskui.models.ui.UiElement;

public final class TaskExecutionActivity extends Activity implements Constants {

	private static final int REQUEST_CODE_MAP = 0;

	private static final int DIALOG_ID_SHARE_TASK = 0;

	private String task_id;
	private String user_id;
	private String task_title;

	private Button btnBack = null;
	private Button btnNext = null;
	private Button btnShare = null;
	private Button btnSkip = null;
	private LayoutInflater layoutInflater;
	private LinearLayout mainView;
	private ProgressDialog progressDialog;
	private Hashtable<String, Listener> uiListeners;
	private Hashtable<String, Object> variables;
	private boolean isSharingTask = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		setContentView(R.layout.task_execution);

		mainView = (LinearLayout) findViewById(R.id.mainContainer);
		btnBack = (Button) findViewById(R.id.backButton);
		btnNext = (Button) findViewById(R.id.nextButton);
		btnShare = (Button) findViewById(R.id.shareButton);
		btnSkip = (Button) findViewById(R.id.skipButton);

		btnSkip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeSkip();
			}
		});

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeBack();
			}
		});

		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String inputValue = null;
				for (int i = 0; i < TaskExecutionActivity.this.mainView.getChildCount(); i++) {
					View view = TaskExecutionActivity.this.mainView.getChildAt(i);
					if (view instanceof RadioGroup) {
						RadioGroup radioGroup = (RadioGroup) view;
						RadioButton RadioButton = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
						if (inputValue == null) {
							inputValue = radioGroup.getTag() + NAME_VALUE_SEPERATOR + (String) RadioButton.getTag();
						} else {
							inputValue += NAME_VALUE_PAIR_SEPERATOR + radioGroup.getTag() + NAME_VALUE_SEPERATOR + (String) RadioButton.getTag();
						}
					} else if (view instanceof EditText) {
						EditText editText = (EditText) view;
						if (inputValue == null) {
							inputValue = editText.getTag() + NAME_VALUE_SEPERATOR + editText.getText().toString();
						} else {
							inputValue += NAME_VALUE_PAIR_SEPERATOR + editText.getTag() + NAME_VALUE_SEPERATOR + editText.getText().toString();
						}
					} else if (view instanceof BarcodeView) {
						BarcodeView barcodeView = (BarcodeView) view;
						if (inputValue == null) {
							inputValue = barcodeView.getTag() + NAME_VALUE_SEPERATOR + barcodeView.getText();
						} else {
							inputValue += NAME_VALUE_PAIR_SEPERATOR + barcodeView.getTag() + NAME_VALUE_SEPERATOR + barcodeView.getText();
						}
					}
				}
				executeNext(inputValue);
			}
		});

		btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ID_SHARE_TASK);
			}
		});

		SharedTask sharedtask = (SharedTask) getIntent().getSerializableExtra(TAG_SHARED_TASK);

		if (sharedtask != null) {
			this.isSharingTask = true;
			this.task_title = sharedtask.getTaskid();
			this.task_id = sharedtask.getTaskid();
			this.user_id = sharedtask.getSharer();
		} else {
			this.task_title = getIntent().getStringExtra(KEY_TASK_TITLE);
			this.task_id = getIntent().getStringExtra(KEY_TASK_ID);
			this.user_id = TaskUIApplication.prefs.getString(KEY_USERNAME, null);
		}
		executeTask();
	}

	private TimerTask myTask;
	private Timer timer;

	private void startTimer() {
		myTask = new MyTimerTask();
		timer = new Timer();
		timer.schedule(myTask, 5000, 5000);
	}

	private void stopTimer() {
		if (myTask != null) {
			myTask.cancel();
			myTask = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	protected void onResume() {
		super.onResume();
		if (this.isSharingTask) {
			startTimer();
		}
	}

	protected void onPause(){
		stopTimer();
		super.onPause();
	}
	
	private Hashtable<String, Listener> getUiListeners() {
		if (this.uiListeners == null) {
			this.uiListeners = new Hashtable<String, Listener>();
		}
		return this.uiListeners;
	}

	private Hashtable<String, Object> getVariables() {
		if (this.variables == null) {
			this.variables = new Hashtable<String, Object>();
		}
		return this.variables;
	}

	private void executeNext(String inputValue) {
		String args;
		try {
			args = (inputValue == null ? "" : "&args=" + URLEncoder.encode(inputValue, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			args = (inputValue == null ? "" : "&args=" + URLEncoder.encode(inputValue));
		}
		String url = TASK_SERVER_URL + "executeCommand?userid=" + this.user_id + URL_COMMAND_NEXT + "&taskid=" + task_id + args;
		//System.out.println(url);
		new HttpWorker().execute(url);
	}

	private void executeBack() {
		String url = TASK_SERVER_URL + "executeCommand?userid=" + this.user_id + URL_COMMAND_BACK + "&taskid=" + task_id;
		new HttpWorker().execute(url);
	}

	private void executeSkip() {
		String url = TASK_SERVER_URL + "executeCommand?userid=" + this.user_id + URL_COMMAND_SKIP + "&taskid=" + task_id;
		new HttpWorker().execute(url);
	}

	private void executeTask() {
		String url = TASK_SERVER_URL + "executeTask?userid=" + this.user_id + "&taskid=" + task_id;
		new HttpWorker().execute(url);
	}

	private final class HttpWorker extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(TaskExecutionActivity.this, null, "Loading, please wait...", true, false);
			} else {
				progressDialog.show();
			}
		}

		@Override
		protected String doInBackground(String... src) {
			try {
				URL url = new URL(src[0]);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(15 * 1000); //15 seconds
				urlConnection.connect();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream is = urlConnection.getInputStream();
					StringBuffer sb = new StringBuffer();
					int chr;
					while ((chr = is.read()) != -1) {
						sb.append((char) chr);
					}
					return sb.toString();
				} else {
					return "Task server responses: " + urlConnection.getResponseMessage();
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			setProgressBarIndeterminateVisibility(false);
			progressDialog.dismiss();
			parseResponse(result);
		}
	}

	private void parseResponse(String responseStr) {
		int currentVewId = 0;
		if (responseStr != null && !responseStr.equals("") && responseStr.startsWith("<" + TAG_TASK_RESPONSE + ">")) {
			//------------------------------------------------------------------------------------------------------------------
			System.out.println(responseStr);
			//System.out.println("----------------------");
			Response response = (Response) TaskUIApplication.xstream.fromXML(responseStr);
			//System.out.println(TaskUIApplication.xstream.toXML(response));
			//-------------------------------------------------------------------------------------------------------------------

			this.uitoken = response.getUItoken();

			if (response.getUi().size() == 0) {
				this.finish();
				return;
			}

			if (response.getTitle() != null && response.getTitle().length() > 0 && !response.getTitle().equalsIgnoreCase("null")) {
				this.setTitle(response.getTitle());
			} else {
				this.setTitle(this.task_title);
			}

			if (response.getUi().get(0) instanceof Mapview) {
				Mapview mapview = (Mapview) response.getUi().get(0);
				String address = mapview.getAddress();
				Intent intent = new Intent(this, TheMapActivity.class);
				if (address != null && address.trim().length() > 0) {
					intent.putExtra(KEY_ADDRESS, address);
					intent.putExtra(KEY_IS_NEXT_BUTTON, response.getNavBar() != null && response.getNavBar().isNext());
					intent.putExtra(KEY_IS_BACK_BUTTON, response.getNavBar() != null && response.getNavBar().isBack());
					intent.putExtra(KEY_IS_SHARE_BUTTON, response.getNavBar() != null && response.getNavBar().isShare());
					intent.putExtra(KEY_IS_SKIP_BUTTON, response.getNavBar() != null && response.getNavBar().isSkip());
					startActivityForResult(intent, REQUEST_CODE_MAP);
				}
				return;
			}

			this.btnBack.setEnabled(response.getNavBar() != null && response.getNavBar().isBack());
			this.btnShare.setEnabled(response.getNavBar() != null && response.getNavBar().isShare());
			this.btnSkip.setEnabled(response.getNavBar() != null && response.getNavBar().isSkip());
			this.btnNext.setEnabled(true);
			this.btnNext.setText((response.getNavBar() != null && response.getNavBar().isNext()) ? R.string.next_button_label : R.string.done_button_label);

			mainView.removeAllViews();

			for (int i = 0; i < response.getUi().size(); i++) {
				UiElement uiElement = response.getUi().get(i);

				if (uiElement instanceof Textview) { // a text view
					Textview tv = (Textview) uiElement;
					TextView textView = (TextView) layoutInflater.inflate(R.layout.a_text_view, null);
					textView.setText(tv.getText());
					mainView.addView(textView);

				} else if (uiElement instanceof Img) { // an image view
					Img img = (Img) uiElement;
					ImageView imageView = (ImageView) layoutInflater.inflate(R.layout.an_image_view, null);
					mainView.addView(imageView);
					new HttpImageViewLoader(imageView).execute(img.getUrl());

				} else if (uiElement instanceof Select) {
					Select sv = (Select) uiElement;
					RadioGroup radioGroup = (RadioGroup) layoutInflater.inflate(R.layout.a_radio_group, null);
					radioGroup.setId(++currentVewId);
					radioGroup.setTag(sv.getName());
					mainView.addView(radioGroup);
					for (int j = 0; j < sv.getOptions().size(); j++) {
						Option ov = (Option) sv.getOptions().get(j);
						RadioButton radioButton = (RadioButton) layoutInflater.inflate(R.layout.a_radio_button, null);
						radioButton.setId(++currentVewId);
						radioButton.setText(ov.getText());
						radioButton.setTag(ov.getValue());
						//radioButton.setChecked(false);
						radioGroup.addView(radioButton);
					}
					if (sv.getOptions().size() > 0) {
						((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
					}
				} else if (uiElement instanceof Input) {
					Input input = (Input) uiElement;
					if (input.getType().equalsIgnoreCase(UI_INPUT_TYPE_ADDRESS)) {
						View view = layoutInflater.inflate(R.layout.an_address, null);
						mainView.addView(view);
					} else if (input.getType().equalsIgnoreCase(UI_INPUT_TYPE_DATETIME)) {
						View view = layoutInflater.inflate(R.layout.a_date_time_picker, null);
						mainView.addView(view);
					} else if (input.getType().equalsIgnoreCase(UI_INPUT_TYPE_PHONENUMBER)) {
						View view = layoutInflater.inflate(R.layout.a_phone_number_picker, null);
						mainView.addView(view);
					} else if (input.getType().equalsIgnoreCase(UI_INPUT_TYPE_STRING)) {
						EditText view = (EditText) layoutInflater.inflate(R.layout.an_edit_text, null);
						view.setTag(input.getName());
						if (input.getValue() != null) {
							view.setText(input.getValue());
						}
						mainView.addView(view);
					} else if (input.getType().equalsIgnoreCase(UI_INPUT_TYPE_BARCODE)) {
						BarcodeView view = new BarcodeView(this);
						view.setTag(input.getName());
						view.getScanButton().setOnClickListener(
								barcodeScanOnClickListener = new BarcodeScanOnClickListener(TaskExecutionActivity.this, view.getEditText()));
						mainView.addView(view);
					}
				} else if (uiElement instanceof Slider) {
					final Slider slider = (Slider) uiElement;
					View view = (View) layoutInflater.inflate(R.layout.a_seekbar, null);
					SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekBar);
					TextView txtMin = (TextView) view.findViewById(R.id.textViewMin);
					TextView txtMax = (TextView) view.findViewById(R.id.textViewMax);
					final TextView txtValue = (TextView) view.findViewById(R.id.textViewValue);

					if (slider.getStep() == 0) {//to avoid divide by zero in the following
						slider.setStep(1);
					}

					seekbar.setMax((slider.getMax() - slider.getMin()) / slider.getStep());
					txtMin.setText("" + slider.getMin());
					txtMax.setText("" + slider.getMax());
					txtValue.setText("" + slider.getValue());
					seekbar.setProgress((slider.getValue() - slider.getMin()) / slider.getStep());

					seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							txtValue.setText("" + (progress * slider.getStep() + slider.getMin()));
							getVariables().put(slider.getName(), (progress * slider.getStep() + slider.getMin()));
							callHttpService(slider);
						}
					});

					mainView.addView(view);
				} else if (uiElement instanceof Listener) {
					Listener l = (Listener) uiElement;
					this.getUiListeners().put(l.getId(), l);
				} else if (uiElement instanceof Listview) {
					final Listview lv = (Listview) uiElement;
					ListView listView = (ListView) layoutInflater.inflate(R.layout.a_listview, null);
					listView.setAdapter(new ArrayAdapter<Option>(TaskExecutionActivity.this, android.R.layout.simple_list_item_1, lv.getOptions()));
					listView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
							getVariables().put(lv.getName(), lv.getOptions().get((int) id).getValue());
							callHttpService(lv);
						}
					});
					listView.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> listview, View view, int position, long id) {
							getVariables().put(lv.getName(), lv.getOptions().get((int) id).getValue());
							callHttpService(lv);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					});
					mainView.addView(listView);
				}
			}
		} else if (responseStr != null && !responseStr.equals("")) {
			mainView.removeAllViews();
			TextView textView = (TextView) layoutInflater.inflate(R.layout.a_text_view, null);
			textView.setText(responseStr);
			mainView.addView(textView);
		} else {
			mainView.removeAllViews();
			TextView textView = (TextView) layoutInflater.inflate(R.layout.a_text_view, null);
			textView.setText("Task server responses: null or empty");
			mainView.addView(textView);
		}
	}

	private void callHttpService(UiElement ui) {
		if (ui.getListener() != null) {
			Listener l = getUiListeners().get(ui.getListener());
			if (l != null && l.getUrl() != null) {
				String urlStr = l.getUrl();
				if (l.getArgs() != null && l.getArgs().size() > 0) {
					urlStr += "?a=.";
					for (int i = 0; i < l.getArgs().size(); i++) {
						if (l.getArgs().get(i).getValue().startsWith("$")) {
							String name = l.getArgs().get(i).getValue().substring(1);
							Object value = getVariables().get(name);
							urlStr += "&" + l.getArgs().get(i).getName() + "=" + value;
						} else {
							urlStr += "&" + l.getArgs().get(i).getName() + "=" + l.getArgs().get(i).getValue();
						}
					}
				}
				// TODO
				System.out.println("callHttpService: " + urlStr);
				new HttpCall().execute(urlStr);
			}
		}
	}

	private final class HttpImageViewLoader extends AsyncTask<String, Void, Bitmap> {
		private ImageView imageView;

		public HttpImageViewLoader(ImageView _imageView) {
			this.imageView = _imageView;
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			try {
				return BitmapFactory.decodeStream((InputStream) new URL(urls[0]).getContent());
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap resultBitmap) {
			imageView.setImageBitmap(resultBitmap);
		}
	}

	private final class HttpCall extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			try {
				InputStream is = (InputStream) new URL(urls[0]).getContent();
				StringBuffer sb = new StringBuffer();
				int chr;
				while ((chr = is.read()) != -1) {
					sb.append((char) chr);
				}
				return sb.toString();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
			return null;
		}
	}

	private BarcodeScanOnClickListener barcodeScanOnClickListener;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (barcodeScanOnClickListener != null && scanResult != null && barcodeScanOnClickListener.getListenerView() != null) {
			if (scanResult.getContents() == null || scanResult.getContents().trim().length() == 0) {
				barcodeScanOnClickListener.onClick(null);
				return;
			}
			if (barcodeScanOnClickListener.getListenerView() instanceof EditText) {
				((EditText) barcodeScanOnClickListener.getListenerView()).setText(scanResult.getContents());
			} else {
				barcodeScanOnClickListener.getListenerView().setTag(scanResult.getContents());
			}
		}

		switch (requestCode) {
		case REQUEST_CODE_MAP:
			if (resultCode == RESULT_CANCELED) {
				executeBack();
			} else {
				String button = intent.getStringExtra(KEY_BUTTON_CLICKED);
				if (button.equals(KEY_IS_NEXT_BUTTON)) {
					executeNext(null);
				} else if (button.equals(KEY_IS_SKIP_BUTTON)) {
					executeSkip();
				}
			}
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_ID_SHARE_TASK:
			final EditText input = new EditText(this);
			dialog = new AlertDialog.Builder(this).setTitle("Please enter sharee' id:").setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (input.getText().toString().length() > 0) {

						String url = TASK_SERVER_URL + "shareTask?sharer=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null) + "&taskid=" + task_id + "&sharee="
								+ input.getText().toString();
						new AsyncTask<String, Void, String>() {
							ProgressDialog progressDialog;

							@Override
							protected void onPreExecute() {
								progressDialog = ProgressDialog.show(TaskExecutionActivity.this, null, "Please wait...", true, true, new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										cancel(true);
									}
								});
							}

							@Override
							protected String doInBackground(String... urls) {
								try {
									InputStream is = (InputStream) new URL(urls[0]).getContent();
									StringBuffer sb = new StringBuffer();
									int chr;
									while ((chr = is.read()) != -1) {
										sb.append((char) chr);
									}
									return sb.toString();
								} catch (MalformedURLException e) {
								} catch (IOException e) {
								}
								return null;
							}

							@Override
							protected void onPostExecute(String result) {
								progressDialog.dismiss();
								isSharingTask = true;
								startTimer();
								Toast.makeText(TaskExecutionActivity.this, "Shared!", Toast.LENGTH_LONG).show();
							}
						}.execute(url);
					}
				}
			}).setNegativeButton("Cancel", null).create();
			break;
		}
		return dialog;
	}

	private String uitoken;

	private class MyTimerTask extends TimerTask {

		public final void run() {
			new AsyncTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... params) {
					try {
						String url = TASK_SERVER_URL + "heartbeat?userid=" + user_id;
						InputStream is = (InputStream) new URL(url).getContent();
						StringBuffer sb = new StringBuffer();
						int chr;
						while ((chr = is.read()) != -1) {
							sb.append((char) chr);
						}
						return sb.toString();
					} catch (MalformedURLException e) {
					} catch (IOException e) {
					}
					return null;
				}

				@Override
				protected void onPostExecute(String responseStr) {
					if (responseStr != null && !responseStr.equals(uitoken)) {
						uitoken = responseStr;
						TaskExecutionActivity.this.executeTask();
					}
				}
			}.execute();
		}
	};

	@Override
	public void onDestroy() {
		stopTimer();
		super.onDestroy();
	}

}

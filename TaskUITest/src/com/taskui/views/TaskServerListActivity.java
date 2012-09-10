package com.taskui.views;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.taskui.models.Constants;
import com.taskui.models.TaskServer;

public final class TaskServerListActivity extends ListActivity implements Constants {
	private static final int DIALOG_ID_ADD_TASK_SERVER = 0;

	// -- container for task servers <friendly name, description>
	private List<Map<String, String>> taskSersers = new ArrayList<Map<String, String>>();

	// The adapter that holds the actual data of the list view
	private SimpleAdapter adapter;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		/*
		setContentView(R.layout.task_server_list);
		adapter = new SimpleAdapter(this, taskSersers, android.R.layout.simple_list_item_2, new String[] { KEY_TASK_SERVER_TITLE, KEY_TASK_SERVER_DESCRIPTION },
				new int[] { android.R.id.text1, android.R.id.text2 });

		setListAdapter(adapter);

		Button btnRefresh = (Button) findViewById(R.id.refreshButton);
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				taskSersers.clear();
				new HttpWorker().execute(TASK_SERVER_LIST_URL);
			}
		});

		Button btnAdd = (Button) findViewById(R.id.addButton);
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ID_ADD_TASK_SERVER);
			}
		});

		Button btnViewTasks = (Button) findViewById(R.id.viewtasksButton);
		btnViewTasks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] servers = new CharSequence[taskSersers.size() + 1];
				final boolean[] checkedItems = new boolean[taskSersers.size() + 1];
				servers[0] = "All";
				checkedItems[0] = true;
				for (int i = 0; i < taskSersers.size(); i++) {
					servers[i + 1] = taskSersers.get(i).get(KEY_TASK_SERVER_TITLE);
				}

				OnMultiChoiceClickListener onMultiChoiceClickListener = new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						checkedItems[which] = isChecked;
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(TaskServerListActivity.this);
				builder.setTitle("Select task server(s):");
				builder.setMultiChoiceItems(servers, checkedItems, onMultiChoiceClickListener);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ArrayList<String> serverUrls = new ArrayList<String>();
						ArrayList<String> serverIds = new ArrayList<String>();
						if (checkedItems[0]) {//all servers
							for (int i = 0; i < taskSersers.size(); i++) {
								serverUrls.add(taskSersers.get(i).get(KEY_TASK_SERVER_URL));
								serverIds.add(taskSersers.get(i).get(KEY_TASK_SERVER_ID));
							}
						} else {//some (may be zero) of task servers are selected
							for (int i = 0; i < taskSersers.size(); i++) {
								if (checkedItems[i + 1]) {
									serverUrls.add(taskSersers.get(i).get(KEY_TASK_SERVER_URL));
									serverIds.add(taskSersers.get(i).get(KEY_TASK_SERVER_ID));
								}
							}
						}
						if (serverUrls.size() > 0) {
							Intent taskIntent = new Intent(TaskServerListActivity.this, TaskListActivity.class);
							taskIntent.putStringArrayListExtra(KEY_TASK_SERVER_URL, serverUrls);
							taskIntent.putStringArrayListExtra(KEY_TASK_SERVER_ID, serverIds);
							startActivity(taskIntent);
						} else {
							Toast.makeText(TaskServerListActivity.this, "You've not selected any task servers!", Toast.LENGTH_LONG).show();
						}
					}
				});
				builder.setNegativeButton("Cancel", null);
				builder.show();
			}
		});

		new HttpWorker().execute(TASK_SERVER_LIST_URL);
		if (!CheckSharedTaskService.isServiceStarted) {
			Intent intent = new Intent(this, CheckSharedTaskService.class);
			intent.putExtra(KEY_TASK_SERVER_URL, "http://149.144.209.135:8084/taskos/");
			startService(intent);
		}
		*/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ArrayList<String> serverUrls = new ArrayList<String>();
		ArrayList<String> serverIds = new ArrayList<String>();
		serverUrls.add(taskSersers.get((int) id).get(KEY_TASK_SERVER_URL));
		serverIds.add(taskSersers.get((int) id).get(KEY_TASK_SERVER_ID));
		Intent taskIntent = new Intent(this, TaskListActivity.class);
		taskIntent.putStringArrayListExtra(KEY_TASK_SERVER_URL, serverUrls);
		taskIntent.putStringArrayListExtra(KEY_TASK_SERVER_ID, serverIds);
		startActivity(taskIntent);
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_ID_ADD_TASK_SERVER:
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			input.setText("http://");
			dialog = new AlertDialog.Builder(this).setTitle("Please enter task server URL:").setView(input)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (input.getText().toString().length() > 0) {
								new HttpWorker().execute(input.getText().toString());
								input.setText("http://");
							}
						}
					}).setNegativeButton("Cancel", null).create();
			break;
		}
		return dialog;
	}

	private final class HttpWorker extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(TaskServerListActivity.this, null, "Loading server list, please wait...", true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						HttpWorker.this.cancel(true);
						setProgressBarIndeterminateVisibility(false);
					}
				});
			} else {
				progressDialog.show();
			}
		}

		@Override
		protected String doInBackground(String... src) {
			try {
				String queryString = null;
				try {
					queryString = "userid=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null) + "&realname="
							+ URLEncoder.encode(TaskUIApplication.prefs.getString(KEY_REALNAME, null), "utf-8");
				} catch (UnsupportedEncodingException e1) {
				}
				URL url = new URL("http://149.144.209.135:8084/taskos/registerUser?" + queryString);
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
				}
				
				
				url = new URL(src[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
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
					return "Server responses: " + urlConnection.getResponseMessage();
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

	@SuppressWarnings("unchecked")
	public void parseResponse(String responseStr) {
		if (responseStr != null && !responseStr.equals("") && responseStr.startsWith("<" + TAG_TASK_SERVERS + ">")) {
			List<TaskServer> _taskSersers = (List<TaskServer>) TaskUIApplication.xstream.fromXML(responseStr);
			for (int i = 0; i < _taskSersers.size(); i++) {
				Map<String, String> taskServer = new HashMap<String, String>();
				/*
				{
					private static final long serialVersionUID = 5799407525152491702L;

					public final boolean equals(Object obj){
						if(obj instanceof Map<?, ?>){
							Map<String, String> taskServer = (Map<String, String>) obj;
							boolean result = (this.get(KEY_TASK_SERVER_TITLE).equals(taskServer.get(KEY_TASK_SERVER_TITLE)));
							result &= (this.get(KEY_TASK_SERVER_DESCRIPTION).equals(taskServer.get(KEY_TASK_SERVER_DESCRIPTION)));;
							result &= (this.get(KEY_TASK_SERVER_URL).equals(taskServer.get(KEY_TASK_SERVER_URL)));
							return result;
						}
						return false;
					}
				};
				*/
				taskServer.put(KEY_TASK_SERVER_ID, _taskSersers.get(i).getId());
				taskServer.put(KEY_TASK_SERVER_TITLE, _taskSersers.get(i).getSpace());
				taskServer.put(KEY_TASK_SERVER_DESCRIPTION, _taskSersers.get(i).getDescription());
				taskServer.put(KEY_TASK_SERVER_URL, _taskSersers.get(i).getUrl());
				if (!taskSersers.contains(taskServer)) {
					taskSersers.add(taskServer);
				}
			}
			adapter.notifyDataSetChanged();
		} else {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error").setMessage(responseStr).setPositiveButton("Dismiss", null).show();
		}
	}
}
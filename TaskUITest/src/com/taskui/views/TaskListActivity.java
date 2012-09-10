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
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.taskui.models.Constants;
import com.taskui.models.Recommendation;
import com.taskui.models.Task;
import com.taskui.models.TaskDatabase;
import com.taskui.models.TaskServer;
import com.taskui.models.TaskSuggestionProvider;

public final class TaskListActivity extends ListActivity implements Constants {

	// -- container for tasks
	private static final List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();

	// The adapter that holds the actual data of the list view
	private SimpleAdapter adapter;
	private ProgressDialog progressDialog;
	private static String task_space_id = "all";
	private BluetoothAdapter mBluetoothAdapter;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		Intent intent = getIntent();

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			filter(query);
		} else {
			setContentView(R.layout.task_list);
			adapter = new SimpleAdapter(this, tasks, android.R.layout.simple_list_item_2, new String[] { KEY_TASK_TITLE, KEY_TASK_DESCRIPTION }, new int[] {
					android.R.id.text1, android.R.id.text2 });

			setListAdapter(adapter);

			//start to get the recommended task list
			new HttpWorkerGetTaskList().execute(TASK_SERVER_URL, TaskListActivity.task_space_id); // mean all task servers
		}

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
	}

	protected void onResume() {
		if (TaskUIApplication.prefs.getBoolean(KEY_AUTOMATIC_SWITCH_TASK_SPACE, false)) {
			startTimer();
		}
		super.onResume();
	}

	protected void onPause() {
		stopTimer();
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			filter(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			Cursor cursor = managedQuery(uri, null, null, null, null);
			if (cursor == null) {
			} else {
				cursor.moveToFirst();
				int id_index = cursor.getColumnIndexOrThrow(TaskDatabase.TASK_ID);
				int title_index = cursor.getColumnIndexOrThrow(TaskDatabase.TASK_TITLE);

				Intent _intent = new Intent(this, TaskExecutionActivity.class);
				_intent.putExtra(KEY_TASK_SERVER_URL, TASK_SERVER_URL);
				_intent.putExtra(KEY_TASK_ID, cursor.getString(id_index));
				_intent.putExtra(KEY_TASK_TITLE, cursor.getString(title_index));
				startActivity(_intent);
			}
		}
	}

	private void filter(String s) {
		List<Map<String, String>> temp = new ArrayList<Map<String, String>>();
		for (int i = tasks.size() - 1; i >= 0; i--) {
			Map<String, String> taskMap = TaskListActivity.tasks.get(i);
			Task t = new Task(taskMap.get(Constants.KEY_TASK_ID), taskMap.get(Constants.KEY_TASK_TITLE), taskMap.get(Constants.KEY_TASK_DESCRIPTION));
			if (t.getMatchScore(s) > 0) {
				temp.add(taskMap);
			}
		}

		if (temp.size() == 0) {
			setTitle("No tasks found for '" + s + "'!!!");
		} else {
			setTitle("Task(s) found for '" + s + "'");
		}

		adapter = new SimpleAdapter(this, temp, android.R.layout.simple_list_item_2, new String[] { KEY_TASK_TITLE, KEY_TASK_DESCRIPTION }, new int[] {
				android.R.id.text1, android.R.id.text2 });

		setListAdapter(adapter);
	}

	private final class HttpWorkerGetTaskList extends AsyncTask<String, Object, String> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(TaskListActivity.this, null, "Loading task list, please wait...", true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						HttpWorkerGetTaskList.this.cancel(true);
						setProgressBarIndeterminateVisibility(false);
					}
				});
			} else {
				progressDialog.show();
			}
		}

		@Override
		protected String doInBackground(String... params) {
			String serverUrl = params[0];
			String serverId = params[1];
			URL url;
			HttpURLConnection urlConnection;
			InputStream is;
			StringBuffer sb;
			int chr;
			String queryString = null;
			try {
				queryString = "&userid=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null) + "&realname="
						+ URLEncoder.encode(TaskUIApplication.prefs.getString(KEY_REALNAME, null), "utf-8");
			} catch (UnsupportedEncodingException e1) {
			}
			try {
				url = new URL(serverUrl + "getRecommendedTasks?serverid=" + serverId + queryString);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(15 * 1000); //15 seconds
				urlConnection.connect();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					is = urlConnection.getInputStream();
					sb = new StringBuffer();
					while ((chr = is.read()) != -1) {
						sb.append((char) chr);
					}
					publishProgress(serverUrl, sb.toString());
				} else {
					Exception e = new Exception("Server responses: " + urlConnection.getResponseMessage());
					publishProgress(e);
				}
			} catch (Exception e) {
				publishProgress(e);
			}
			return null;
		}

		protected void onProgressUpdate(Object... objs) {
			if (objs[0] instanceof Exception) {
				new AlertDialog.Builder(TaskListActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
						.setMessage(((Exception) objs[0]).getMessage()).setPositiveButton("Dismiss", null).show();
			} else {//good result
				String responseStr = (String) objs[1];
				if (responseStr != null && !responseStr.equals("") && responseStr.startsWith("<" + TAG_RECOMMENDATIONS + ">")) {
					parseResponse((String) objs[0], responseStr);
					adapter.notifyDataSetChanged();
				} else {
					new AlertDialog.Builder(TaskListActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error").setMessage(responseStr)
							.setPositiveButton("Dismiss", null).show();
				}
			}
		}

		@Override
		protected void onPostExecute(String result) {
			setProgressBarIndeterminateVisibility(false);
			progressDialog.dismiss();
		}
	}

	@SuppressWarnings("unchecked")
	private static void parseResponse(String taskServerUrl, String responseStr) {
		List<Recommendation> recommendations = (List<Recommendation>) TaskUIApplication.xstream.fromXML(responseStr);
		for (int i = 0; i < recommendations.size(); i++) {
			Map<String, String> task = new HashMap<String, String>();
			task.put(KEY_TASK_TITLE, recommendations.get(i).getTitle());
			task.put(KEY_TASK_DESCRIPTION, recommendations.get(i).getDescription());
			task.put(KEY_TASK_ID, recommendations.get(i).getId());
			task.put(KEY_TASK_SERVER_URL, taskServerUrl);
			if (!tasks.contains(task)) {
				tasks.add(task);
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent taskIntent = new Intent(this, TaskExecutionActivity.class);
		taskIntent.putExtra(KEY_TASK_SERVER_URL, tasks.get((int) id).get(KEY_TASK_SERVER_URL));
		taskIntent.putExtra(KEY_TASK_ID, tasks.get((int) id).get(KEY_TASK_ID));
		taskIntent.putExtra(KEY_TASK_TITLE, tasks.get((int) id).get(KEY_TASK_TITLE));
		startActivity(taskIntent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	/** Optionally, specify what should be shown when launching Gesture Search.
	 * If this is not specified, SHOW_HISTORY will be used as a default value. */
	private static String SHOW_MODE = "show";
	/** Possible values for invoking mode */
	// Show the visited items
	//private static final int SHOW_HISTORY = 0;
	// Show nothing (a blank screen)
	private static final int SHOW_NONE = 1;
	// Show all of date items
	//private static final int SHOW_ALL = 2;

	/** The theme of Gesture Search can be light or dark.
	 * By default, Gesture Search will use a dark theme. */
	private static final String THEME = "theme";
	//private static final int THEME_LIGHT = 0;
	private static final int THEME_DARK = 1;

	private static final int GESTURE_SEARCH_REQUEST_CODE = 0;
	/** Keys for results returned by Gesture Search */
	private static final String SELECTED_ITEM_NAME = "selected_item_name";

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnu_keyboard_search:
			onSearchRequested();
			return true;
		case R.id.mnu_gesture_search:
			try {
				Intent intent = new Intent();
				intent.setAction("com.google.android.apps.gesturesearch.SEARCH");
				intent.setData(TaskSuggestionProvider.CONTENT_URI);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.putExtra(SHOW_MODE, SHOW_NONE);
				intent.putExtra(THEME, THEME_DARK);
				startActivityForResult(intent, GESTURE_SEARCH_REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(TaskListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.mnu_settings:
			final CharSequence[] items = { "Switch task space automatically based on location" };
			final boolean[] checkeditems = { TaskUIApplication.prefs.getBoolean(KEY_AUTOMATIC_SWITCH_TASK_SPACE, false) };

			AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Settings");
			builder.setMultiChoiceItems(items, checkeditems, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					TaskUIApplication.prefs.edit().putBoolean(KEY_AUTOMATIC_SWITCH_TASK_SPACE, isChecked).commit();
					startTimer();
				}
			}).setCancelable(true).setPositiveButton("Close", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();

			return true;
		case R.id.mnu_switch_task_space:
			new HttpWorker().execute(TASK_SERVER_LIST_URL);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent intent) {
		switch (reqCode) {
		case GESTURE_SEARCH_REQUEST_CODE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				String selectedTaskTitle = intent.getStringExtra(SELECTED_ITEM_NAME);

				Map<String, String> task = findTask(selectedTaskTitle);

				Intent _intent = new Intent(this, TaskExecutionActivity.class);
				_intent.putExtra(KEY_TASK_SERVER_URL, TASK_SERVER_URL);
				_intent.putExtra(KEY_TASK_ID, task.get(KEY_TASK_ID));
				_intent.putExtra(KEY_TASK_TITLE, task.get(KEY_TASK_TITLE));
				startActivity(_intent);
			}
		}
	}

	private Map<String, String> findTask(String taskTitle) {
		for (int i = tasks.size() - 1; i >= 0; i--) {
			if (tasks.get(i).get(KEY_TASK_TITLE).equals(taskTitle)) {
				return tasks.get(i);
			}
		}
		return null;
	}

	public static List<Task> getTasks() {
		if (TaskListActivity.tasks.isEmpty()) {
			URL url;
			HttpURLConnection urlConnection;
			InputStream is;
			StringBuffer sb;
			int chr;
			String queryString = null;
			try {
				queryString = "&userid=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null) + "&realname="
						+ URLEncoder.encode(TaskUIApplication.prefs.getString(KEY_REALNAME, null), "utf-8");
			} catch (UnsupportedEncodingException e1) {
			}
			try {
				url = new URL(TASK_SERVER_URL + "getRecommendedTasks?serverid=" + task_space_id + queryString);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(15 * 1000); //15 seconds
				urlConnection.connect();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					is = urlConnection.getInputStream();
					sb = new StringBuffer();
					while ((chr = is.read()) != -1) {
						sb.append((char) chr);
					}
					parseResponse(TASK_SERVER_URL, sb.toString());
				}
			} catch (Exception e) {
			}
		}
		List<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < TaskListActivity.tasks.size(); i++) {
			Map<String, String> taskMap = TaskListActivity.tasks.get(i);
			Task t = new Task(taskMap.get(Constants.KEY_TASK_ID), taskMap.get(Constants.KEY_TASK_TITLE), taskMap.get(Constants.KEY_TASK_DESCRIPTION));
			tasks.add(t);
		}
		return tasks;
	}

	private final class HttpWorker extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(TaskListActivity.this, null, "Loading task list, please wait...", true, true, new OnCancelListener() {
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
	private void parseResponse(String responseStr) {
		if (responseStr != null && !responseStr.equals("") && responseStr.startsWith("<" + TAG_TASK_SERVERS + ">")) {
			List<TaskServer> _taskSersers = (List<TaskServer>) TaskUIApplication.xstream.fromXML(responseStr);
			showSwitchSpaceDialog(_taskSersers);

		} else {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error").setMessage(responseStr).setPositiveButton("Dismiss", null).show();
		}
	}

	private void showSwitchSpaceDialog(final List<TaskServer> taskSersers) {
		final CharSequence[] servers = new CharSequence[taskSersers.size() + 1];
		servers[0] = "All";
		for (int i = 0; i < taskSersers.size(); i++) {
			servers[i + 1] = taskSersers.get(i).getSpace();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
		builder.setTitle("Select a task space:");
		builder.setSingleChoiceItems(servers, -1, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TaskUIApplication.prefs.edit().putBoolean(KEY_AUTOMATIC_SWITCH_TASK_SPACE, false).commit();
				myTask.cancel();
				dialog.dismiss();
				String selected_space_id = (which == 0) ? "all" : taskSersers.get(which - 1).getId();
				if (!selected_space_id.equals(task_space_id)) {//only refresh if the selection is different from the current one
					task_space_id = selected_space_id;
					tasks.clear();
					new HttpWorkerGetTaskList().execute(TASK_SERVER_URL, task_space_id);
				}
			}
		});
		builder.show();
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

	@Override
	public void onDestroy() {
		stopTimer();
		// Unregister broadcast listeners
		try {
			this.unregisterReceiver(mReceiver);
		} catch (Throwable t) {
		}
		super.onDestroy();
	}

	private boolean isScanAndDiscoveryDone = true;
	private String lastBlueToothDevice;

	private class MyTimerTask extends TimerTask {
		public final void run() {
			if (isScanAndDiscoveryDone) {
				isScanAndDiscoveryDone = false;

				if (mBluetoothAdapter != null) {
					// If we're already discovering, stop it
					if (mBluetoothAdapter.isDiscovering()) {
						mBluetoothAdapter.cancelDiscovery();
					}

					// Request discover from BluetoothAdapter
					mBluetoothAdapter.startDiscovery();
				}
			}
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes isScanAndDiscoveryDone to true when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//System.out.println("device.getAddress() --- " + device.getAddress() + " " + device.getName());
				if (!device.getAddress().equals(lastBlueToothDevice)) {
					lastBlueToothDevice = device.getAddress();
					new HttpWorkerGetSpaceId().execute(TASK_SERVER_URL, device.getAddress());
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//System.out.println("BluetoothAdapter.ACTION_DISCOVERY_FINISHED --- ");
				isScanAndDiscoveryDone = true;
			}
		}
	};

	private final class HttpWorkerGetSpaceId extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String serverUrl = params[0];
			String bluetoothAddress = params[1];
			URL url;
			HttpURLConnection urlConnection;
			InputStream is;
			StringBuffer sb;
			int chr;
			try {
				url = new URL(serverUrl + "getSpaceId?bluetoothAddress=" + bluetoothAddress);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(15 * 1000); //15 seconds
				urlConnection.connect();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					is = urlConnection.getInputStream();
					sb = new StringBuffer();
					while ((chr = is.read()) != -1) {
						sb.append((char) chr);
					}
					return sb.toString();
				} else {
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && result.trim().length() > 0) {
				if (!result.equals(task_space_id)) {//only refresh if the selection is different from the current one
					task_space_id = result;
					tasks.clear();
					new HttpWorkerGetTaskList().execute(TASK_SERVER_URL, task_space_id);
				}
			}
		}
	}
}
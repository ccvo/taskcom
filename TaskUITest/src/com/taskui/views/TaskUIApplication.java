package com.taskui.views;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.taskui.models.Constants;
import com.taskui.models.NavigationBar;
import com.taskui.models.Recommendation;
import com.taskui.models.Response;
import com.taskui.models.SharedTask;
import com.taskui.models.TaskDatabase;
import com.taskui.models.TaskServer;
import com.taskui.models.ui.Argument;
import com.taskui.models.ui.Img;
import com.taskui.models.ui.Input;
import com.taskui.models.ui.Listener;
import com.taskui.models.ui.Listview;
import com.taskui.models.ui.Mapview;
import com.taskui.models.ui.Option;
import com.taskui.models.ui.Select;
import com.taskui.models.ui.Slider;
import com.taskui.models.ui.Textview;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public final class TaskUIApplication extends Activity implements Constants {
	public static SharedPreferences prefs = null;
	public static final XStream xstream;
	static {
		xstream = new XStream(new DomDriver());

		xstream.alias(TAG_RECOMMENDATION, Recommendation.class);
		xstream.alias(TAG_RECOMMENDATIONS, List.class);

		xstream.alias(TAG_SHARED_TASKS, List.class);
		xstream.alias(TAG_SHARED_TASK, SharedTask.class);

		xstream.alias(TAG_TASK_SERVER, TaskServer.class);
		xstream.alias(TAG_TASK_SERVERS, List.class);

		xstream.alias(TAG_TASK_RESPONSE, Response.class);
		xstream.alias(TAG_TASK_UI, List.class);
		xstream.alias(TAG_TASK_NAVIGATION_BAR, NavigationBar.class);
		xstream.alias(TAG_UI_TEXTVIEW, Textview.class);
		xstream.alias(TAG_UI_IMAGE, Img.class);
		xstream.alias(TAG_UI_SELECT, Select.class);
		xstream.alias(TAG_UI_OPTION, Option.class);
		xstream.alias(TAG_UI_INPUT, Input.class);
		xstream.alias(TAG_ARGUMENT, Argument.class);
		xstream.alias(TAG_UI_SLIDER, Slider.class);
		xstream.alias(TAG_UI_LISTENER, Listener.class);
		xstream.alias(TAG_UI_LISTVIEW, Listview.class);
		xstream.alias(TAG_UI_MAPVIEW, Mapview.class);

		xstream.addImplicitCollection(Select.class, TAG_UI_OPTIONS, Option.class);
		xstream.addImplicitCollection(Listview.class, TAG_UI_OPTIONS, Option.class);

		xstream.useAttributeFor(Textview.class, ATTRIBUTE_TEXT);
		xstream.useAttributeFor(Img.class, ATTRIBUTE_URL);
		xstream.useAttributeFor(Select.class, ATTRIBUTE_NAME);
		xstream.useAttributeFor(Option.class, ATTRIBUTE_VALUE);
		xstream.useAttributeFor(Option.class, ATTRIBUTE_TEXT);
		xstream.useAttributeFor(Input.class, ATTRIBUTE_TYPE);
		xstream.useAttributeFor(Input.class, ATTRIBUTE_NAME);
		xstream.useAttributeFor(Input.class, ATTRIBUTE_VALUE);

		xstream.useAttributeFor(Slider.class, ATTRIBUTE_NAME);
		xstream.useAttributeFor(Slider.class, ATTRIBUTE_LISTENER);
		xstream.useAttributeFor(Slider.class, ATTRIBUTE_MIN);
		xstream.useAttributeFor(Slider.class, ATTRIBUTE_MAX);
		xstream.useAttributeFor(Slider.class, ATTRIBUTE_VALUE);
		xstream.useAttributeFor(Slider.class, ATTRIBUTE_STEP);

		xstream.useAttributeFor(Listview.class, ATTRIBUTE_NAME);
		xstream.useAttributeFor(Listview.class, ATTRIBUTE_LISTENER);
		xstream.useAttributeFor(Listview.class, ATTRIBUTE_VALUE);

		xstream.useAttributeFor(Listener.class, ATTRIBUTE_ID);
		xstream.useAttributeFor(Listener.class, ATTRIBUTE_URL);

		xstream.useAttributeFor(Argument.class, ATTRIBUTE_NAME);
		xstream.useAttributeFor(Argument.class, ATTRIBUTE_VALUE);

		xstream.useAttributeFor(Mapview.class, ATTRIBUTE_ADDRESS);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {//this indicates that the global search starts this activity
			Uri uri = intent.getData();
			Cursor cursor = managedQuery(uri, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				int id_index = cursor.getColumnIndexOrThrow(TaskDatabase.TASK_ID);
				int title_index = cursor.getColumnIndexOrThrow(TaskDatabase.TASK_TITLE);
				intent.putExtra(KEY_TASK_SERVER_URL, TASK_SERVER_URL);
				intent.putExtra(KEY_TASK_ID, cursor.getString(id_index));
				intent.putExtra(KEY_TASK_TITLE, cursor.getString(title_index));
			}
		}

		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		}
		if (prefs.getString(KEY_USERNAME, null) != null) {// already registered

			new HttpWorker().execute();//to register with the server

			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				intent.setClass(TaskUIApplication.this, TaskExecutionActivity.class);
			} else {
				intent.setClass(TaskUIApplication.this, TaskListActivity.class);
			}
		} else {
			intent.setClass(TaskUIApplication.this, RegistrationActivity.class);
		}
		finish();
		startActivity(intent);
	}

	private final class HttpWorker extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... src) {
			try {
				String queryString = null;
				try {
					queryString = "userid=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null) + "&realname="
							+ URLEncoder.encode(TaskUIApplication.prefs.getString(KEY_REALNAME, null), "utf-8");
				} catch (UnsupportedEncodingException e1) {
				}
				URL url = new URL(TASK_SERVER_URL + "registerUser?" + queryString);
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
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				if (!CheckSharedTaskService.isServiceStarted) {
					Intent intent = new Intent(TaskUIApplication.this, CheckSharedTaskService.class);
					intent.putExtra(KEY_TASK_SERVER_URL, TASK_SERVER_URL);
					startService(intent);
				}
			}
		}
	}
}

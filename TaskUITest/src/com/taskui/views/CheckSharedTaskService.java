package com.taskui.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.taskui.models.Constants;
import com.taskui.models.SharedTask;

public final class CheckSharedTaskService extends Service implements Constants {
	private static final int CHECKING_INTERVAL = 5000; // milliseconds, 5 seconds
	private static final int CHECKING_INITIAL_DELAY = 5000; // 1 second delay when the application just started 
	public static boolean isServiceStarted = false;
	private Timer timer = new Timer();
	private String taskServerUrl;

	@Override
	public void onCreate() {
		super.onCreate();
		isServiceStarted = true;
		timer.schedule(myTask, CHECKING_INITIAL_DELAY, CHECKING_INTERVAL);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		taskServerUrl = intent.getStringExtra(KEY_TASK_SERVER_URL);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		isServiceStarted = false;
		timer.cancel();
		super.onDestroy();
	}

	private TimerTask myTask = new TimerTask() {
		public final void run() {
			try {
				String url = taskServerUrl + "checkSharedTasks?userid=" + TaskUIApplication.prefs.getString(KEY_USERNAME, null);
				InputStream is = (InputStream) new URL(url).getContent();
				StringBuffer sb = new StringBuffer();
				int chr;
				while ((chr = is.read()) != -1) {
					sb.append((char) chr);
				}
				String responseStr = sb.toString();
				//System.out.println(responseStr);

				if (responseStr != null && !responseStr.equals("") && responseStr.startsWith("<" + TAG_SHARED_TASKS + ">")) {
					@SuppressWarnings("unchecked")
					List<SharedTask> tasks = (List<SharedTask>) TaskUIApplication.xstream.fromXML(responseStr);
					if (tasks != null && tasks.size() > 0) {
						for (int i = 0; i < tasks.size(); i++) {
							Intent intent = new Intent(getApplicationContext(), SharedTaskReceiver.class);
							intent.putExtra(TAG_SHARED_TASK, tasks.get(i));
							sendBroadcast(intent);
						}
					}
				}
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}
	};
}

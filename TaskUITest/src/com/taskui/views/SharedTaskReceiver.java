package com.taskui.views;

import com.taskui.models.Constants;
import com.taskui.models.SharedTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SharedTaskReceiver extends BroadcastReceiver implements Constants {

	private Context context;
	private NotificationManager notificationManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		SharedTask task = (SharedTask) intent.getSerializableExtra(TAG_SHARED_TASK);
		createNotification(task);
	}

	private void createNotification(SharedTask task) {
		Notification notification = new Notification(R.drawable.ic_menu_notifications, "'" + task.getSharer() + "' shares you the task '" + task.getTaskid() + "'", System.currentTimeMillis());

		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;

		Intent intent = new Intent(context, TaskExecutionActivity.class);
		intent.putExtra(TAG_SHARED_TASK, task);

		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, "You have a shared task", "Task=" + task.getTaskid() + " Sharer=" + task.getSharer(), pi);

		notificationManager.notify(task.getTaskid() + task.getSharer(), 0, notification);
	}

}

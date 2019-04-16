package org.policerewired.recorder.tasks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/**
 * An abstract asynchronous task that provides a notification to the user while it is running.
 */
public abstract class AbstractNotifyingAsyncTask<Params,Progress,Result> extends AsyncTask<Params,Progress,Result> {

  protected Context context;
  protected NotificationChannel channel;
  protected NotificationCompat.Builder builder;
  protected Notification notification;
  protected int notificationId;

  protected AbstractNotifyingAsyncTask(Context context, NotificationChannel channel) {
    this.context = context;
    this.channel = channel;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    builder = buildNotification();
    notification = builder.build();
    notificationId = AllNotifications.getNextNotificationId();
    mgr.notify(notificationId, notification);
  }

  protected void updateNotificationText(String content) {
    NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    builder.setContentText(content);
    notification = builder.build();
    mgr.notify(notificationId, notification);
  }

  @Override
  protected void onPostExecute(Result result) {
    super.onPostExecute(result);
    NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mgr.cancel(notificationId);
    notification = null;

    if (wasSuccess(result)) {
      String completionToast = getCompletionToast();
      if (completionToast != null) {
        Toast.makeText(context, completionToast, Toast.LENGTH_LONG).show();
      }
    } else {
      String failureToast = getFailureToast();
      if (failureToast != null) {
        Toast.makeText(context, failureToast, Toast.LENGTH_LONG).show();
      }
    }
  }

  protected NotificationCompat.Builder buildNotification() {
    NotificationCompat.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
      builder = new NotificationCompat.Builder(context, channel.getId());
    } else {
      builder = new NotificationCompat.Builder(context); // yes it's deprecated
    }

    builder.setContentTitle(getNotificationTitle());
    builder.setContentText(getNotificationContent());

    builder.setProgress(100, 1, true);

    if (getNotificationTicker() != null) { builder.setTicker(getNotificationTicker()); }

    builder.setSmallIcon(getNotificationIcon());
    builder.setPriority(getNotificationPriority());
    builder.setAutoCancel(false);
    builder.setOngoing(true); // shouldn't be swipable

    return builder;
  }

  protected abstract boolean wasSuccess(Result result);

  protected abstract String getNotificationTitle();
  protected abstract String getNotificationContent();
  protected abstract String getNotificationTicker();
  protected abstract int getNotificationIcon();
  protected abstract int getNotificationPriority();

  protected abstract String getCompletionToast();
  protected abstract String getFailureToast();
}

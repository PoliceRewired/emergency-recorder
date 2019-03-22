package org.policerewired.recorder.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.flt.servicelib.AbstractBootReceiver;

import org.policerewired.recorder.service.RecorderService;

public class BootReceiever extends AbstractBootReceiver<RecorderService> {

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    // schedule a repeating alarm to restart the service periodically
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent serviceIntent = new Intent(context, RecorderService.class);
    PendingIntent alarmIntent = PendingIntent.getService(context, 1000, serviceIntent, 0);
    alarmManager.setInexactRepeating(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
      AlarmManager.INTERVAL_HALF_HOUR,
      alarmIntent);
  }

  @Override
  protected boolean shouldStartAsForegroundService() {
    return true;
  }

  @Override
  protected Class<RecorderService> getServiceClass() {
    return RecorderService.class;
  }
}

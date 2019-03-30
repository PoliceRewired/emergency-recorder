package org.policerewired.recorder.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flt.servicelib.AbstractBootReceiver;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.service.RecorderService;

/**
 * Responsible for receiving notification when the system starts.
 */
public class BootReceiever extends AbstractBootReceiver<RecorderService> {
  private static final String TAG = BootReceiever.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    Log.i(TAG, "Received boot intent, scheduling alarm.");
    EmergencyRecorderApp.scheduleAlarm(context);
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

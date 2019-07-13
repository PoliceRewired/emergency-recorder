package org.policerewired.recorder.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flt.servicelib.AbstractBootReceiver;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.service.RecorderService;

/**
 * Responsible for receiving a notification broadcast when the system starts.
 */
public class BootReceiver extends AbstractBootReceiver<RecorderService> {
  private static final String TAG = BootReceiver.class.getSimpleName();

  @Override
  @SuppressLint("UnsafeProtectedBroadcastReceiver")
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    Log.i(TAG, "Received boot intent, scheduling repeating restart job.");
    EmergencyRecorderApp.scheduleRepeatingCheckup(context);
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

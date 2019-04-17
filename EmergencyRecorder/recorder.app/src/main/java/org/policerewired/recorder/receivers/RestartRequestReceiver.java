package org.policerewired.recorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.policerewired.recorder.EmergencyRecorderApp;

/**
 * Receives and handles explicit intent requests to restart the service.
 */
public class RestartRequestReceiver extends BroadcastReceiver {
  private static final String TAG = RestartRequestReceiver.class.getSimpleName();

  public static final String ACTION_RESTART_SERVICE = "RestartRequestReceiver.RESTART_SERVICE";

  public static final int REQUEST_CODE_ONE_OFF_RESTART = 7000;
  public static final int REQUEST_CODE_REPEATING_CHECKUP = 7001;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "Intent received: " + intent.getAction());
    if (ACTION_RESTART_SERVICE.equals(intent.getAction())) {
      Log.i(TAG, "Restart request Intent received.");
      EmergencyRecorderApp.startRecorderService(context);
    }
  }

}

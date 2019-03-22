package org.policerewired.recorder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import org.policerewired.recorder.service.RecorderService;

public class EmergencyRecorderApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Intent intent = new Intent(this, RecorderService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(intent);
    } else {
      startService(intent);
    }
  }

  public static String[] permissions = new String[] {
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.PROCESS_OUTGOING_CALLS
  };
}

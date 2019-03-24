package org.policerewired.recorder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import org.policerewired.recorder.constants.BaseData;
import org.policerewired.recorder.db.RecordingDb;
import org.policerewired.recorder.service.RecorderService;

import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class EmergencyRecorderApp extends Application {
  private static final String TAG = EmergencyRecorderApp.class.getSimpleName();
  private static String db_name = "recording_db";

  public static RecordingDb db;

  @Override
  public void onCreate() {
    super.onCreate();

    Log.d(TAG, "Initialising app database.");
    db = createDb();

    Intent intent = new Intent(this, RecorderService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Log.d(TAG, "Calling startForegroundService from Application class.");
      startForegroundService(intent);
    } else {
      Log.d(TAG, "Calling startService from Application class.");
      startService(intent);
    }

    Log.d(TAG, "Scheduling alarm from application class.");
    scheduleAlarm(this);
  }

  private RecordingDb createDb() {
    return Room.databaseBuilder(getApplicationContext(), RecordingDb.class, db_name)
      .addCallback(new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
          super.onCreate(db);
          Executors.newSingleThreadScheduledExecutor().execute(prepopulate);
        }
      })
      .build();
  }

  private Runnable prepopulate = () -> {
    Log.d(TAG, "Inserting default data into blank database.");
    db.getRuleDao().insert(BaseData.getRules(EmergencyRecorderApp.this));
  };

  public static void scheduleAlarm(Context context) {
    try {
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      Intent serviceIntent = new Intent(context, RecorderService.class);
      PendingIntent alarmIntent = PendingIntent.getService(context, 1000, serviceIntent, 0);
      alarmManager.setInexactRepeating(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
        AlarmManager.INTERVAL_HALF_HOUR,
        alarmIntent);
      Log.i(TAG, "Scheduled repeating checkup for background service.");
    } catch (Exception e) {
      Log.e(TAG, "Unable to schedule repeating checkup.", e);
    }
  }

  public static String[] permissions = new String[] {
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.PROCESS_OUTGOING_CALLS,
    Manifest.permission.ACCESS_FINE_LOCATION
  };
}

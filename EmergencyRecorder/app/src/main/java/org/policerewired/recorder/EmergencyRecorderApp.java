package org.policerewired.recorder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import org.policerewired.recorder.constants.BaseData;
import org.policerewired.recorder.db.RecordingDb;
import org.policerewired.recorder.service.RecorderService;

import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class EmergencyRecorderApp extends Application {
  private static String db_name = "recording_db";

  public static RecordingDb db;

  @Override
  public void onCreate() {
    super.onCreate();

    db = createDb();

    Intent intent = new Intent(this, RecorderService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(intent);
    } else {
      startService(intent);
    }
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
    db.getRuleDao().insert(BaseData.getRules(EmergencyRecorderApp.this));
  };

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

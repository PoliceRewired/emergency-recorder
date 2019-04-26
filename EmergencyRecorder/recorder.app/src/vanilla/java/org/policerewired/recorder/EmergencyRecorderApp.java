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

import org.jcodec.common.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.constants.BaseData;
import org.policerewired.recorder.db.RecordingDb;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.receivers.RestartRequestReceiver;
import org.policerewired.recorder.service.RecorderService;

import java.util.Date;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static org.policerewired.recorder.receivers.RestartRequestReceiver.REQUEST_CODE_ONE_OFF_RESTART;
import static org.policerewired.recorder.receivers.RestartRequestReceiver.REQUEST_CODE_REPEATING_CHECKUP;

/**
 * Application class for the Emergency Recorder. This class initialises a single instance of
 * the database.
 */
public class EmergencyRecorderApp extends Application {
  private static final String TAG = EmergencyRecorderApp.class.getSimpleName();
  private static String db_name = "recording_db";

  public static boolean alarm_set; // alarm used to restart the service in case of OS termination
  public static RecordingDb db;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "Emergency Recorder: onCreate");
    Log.d(TAG, "Initialising app database.");
    db = createDb();

    Log.d(TAG, "Starting service from Application.");
    startRecorderService(this);

    Log.d(TAG, "Scheduling alarm from application class.");
    scheduleRepeatingCheckup(this);

    recordAuditableEvent(new Date(), getString(R.string.event_audit_application_created), null, false);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    Log.i(TAG, "Emergency Recorder: onTerminate");
    scheduleServiceStart(this);
    recordAuditableEvent(new Date(), getString(R.string.event_audit_application_terminated), null, false);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    Log.d(TAG, "Emergency Recorder: onLowMemory");
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    Log.d(TAG, "Emergency Recorder: onTrimMemory");
  }

  /**
   * Creates or retrieves the app database. If creating the database for the first time, adds a
   * callback to execute the prepopulate Runnable.
   */
  private RecordingDb createDb() {
    return Room.databaseBuilder(getApplicationContext(), RecordingDb.class, db_name)
      .allowMainThreadQueries()
      .addCallback(new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
          super.onCreate(db);
          Executors.newSingleThreadScheduledExecutor().execute(prepopulate);
        }
      })
      .build();
  }

  /**
   * Task to pre-populate the database with standard rules. See: BaseData.getRules(Context);
   */
  private Runnable prepopulate = () -> {
    Log.d(TAG, "Inserting default Rules into blank database.");
    recordAuditableEvent(new Date(), getString(R.string.event_audit_prepopulate_database), null, false);
    db.getRuleDao().insert(BaseData.getRules(EmergencyRecorderApp.this));
  };

  /**
   * Uses the context provided to start the RecorderService (with a preference for calling
   * startForegroundService over startService).
   */
  public static void startRecorderService(Context context) {
    Intent intent = new Intent(context, RecorderService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Log.v(TAG, "Calling startForegroundService from Application class.");
      context.startForegroundService(intent);
    } else {
      Log.v(TAG, "Calling startService from Application class.");
      context.startService(intent);
    }
  }

  /**
   * Creates an intent to restart the service.
   * @param context context required to create the intent
   * @param requestCode request code for the alarm - important to distinguish pending intents
   * @return a PendingIntent containing an explicit Intent for the RestartRequestReceiver.
   */
  public static PendingIntent createRestartIntent(Context context, int requestCode) {
    Intent restartIntent = new Intent(context, RestartRequestReceiver.class);
    restartIntent.setAction(RestartRequestReceiver.ACTION_RESTART_SERVICE);
    restartIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND); // permits the receiver to run in foreground
    restartIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // also for fully stopped packages
    return PendingIntent.getBroadcast(context, requestCode, restartIntent, FLAG_UPDATE_CURRENT);
  }

  /**
   * Schedules a service restart after 60 seconds.
   */
  public static void scheduleServiceStart(Context context) {
    try {
      Log.d(TAG, "Scheduling restart alarm.");
      PendingIntent alarmIntent = createRestartIntent(context, REQUEST_CODE_ONE_OFF_RESTART);

      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.setAndAllowWhileIdle(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + (60 * 1000),
        alarmIntent);

      Log.i(TAG, "Alarm scheduled for 1 minute.");
    } catch (Exception e) {
      Log.e(TAG, "Unable to schedule restart alarm.", e);
    }
  }

  /**
   * Schedules a repeating checkup for the RecorderService.
   */
  public static void scheduleRepeatingCheckup(Context context) {
    if (!alarm_set) {
      try {
        Log.d(TAG, "Scheduling repeat alarm.");
        PendingIntent alarmIntent = createRestartIntent(context, REQUEST_CODE_REPEATING_CHECKUP);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(
          AlarmManager.ELAPSED_REALTIME_WAKEUP,
          SystemClock.elapsedRealtime() + (60*1000),
          AlarmManager.INTERVAL_FIFTEEN_MINUTES,
          alarmIntent);

        Log.i(TAG, "Scheduled repeating checkup for service.");
        alarm_set = true;

      } catch (Exception e) {
        Log.e(TAG, "Unable to schedule repeating checkup.", e);
      }
    } else {
      Log.d(TAG, "Not setting alarm - already marked set.");
    }
  }

  /**
   * Generalised method to log an auditable event that took place.
   * @param at date/time of the event
   * @param event the type of event
   * @param detail essential details to be logged
   * @param debug if true, flagged as a debug record and not visible in the UI (but saved to log files)
   */
  public static void recordAuditableEvent(@NotNull Date at, @NotNull String event, String detail, boolean debug) {
    AuditRecord item = new AuditRecord();
    item.started = at;
    item.type = debug ? AuditRecordType.Debug : AuditRecordType.Audit;
    item.data = event + (StringUtils.isEmpty(detail) ? "" : ": " + detail);
    saveAuditRecord(item);
  }

  /**
   * Stores a record for the auditing 'records' log.
   * @param item the record to store
   */
  public static void saveAuditRecord(@NotNull final AuditRecord item) {
    Executors.newSingleThreadScheduledExecutor().execute(() -> {
      try {
        Log.d(TAG, String.format("Audit: %s, %s", item.type.name(), item.data));
        db.getRecordingDao().insert(item);
      } catch (Exception e) {
        Log.e(TAG, "Unable to record new record of type: " + item.type.name(), e);
      }
    });
  }

  /**
   * <p>
   * This app is in testing, and we are exploring a number of different options for managing
   * background detection of OUTGOING_CALL intents. See:
   * <ul>
   *   <li>https://developer.android.com/about/versions/pie/android-9.0-changes-all#privacy-changes-all</li>
   *   <li>https://developer.android.com/training/monitoring-device-state/doze-standby.html</li>
   * </ul>
   *</p>
   *
   * <p>
   *   The READ_CALL_LOG permission is required for Android 9 and above in order to have visibility
   *   of calls when using PROCESS_OUTGOING_CALLS.
   * </p>
   *
   * <p>
   *   The READ_CALL_LOG permission also requires a form submission to Google Play - as apps that
   *   use it are limited by app store policy for security reasons.
   * </p>
   *
   * <p>
   *   Requesting permission to ignore battery permissions also requires whitelisting on a case
   *   by case basis. As we are an 'automation' app, we may qualify for Play Store whitelisting.
   * </p>
   */
  public static String[] permissions = new String[] {
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET,
    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    Manifest.permission.PROCESS_OUTGOING_CALLS,
    // Manifest.permission.READ_PHONE_STATE,
    // Manifest.permission.READ_CALL_LOG, // required for Android 9 and above
  };
}
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

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.gson.Gson;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.Flags;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.distribute.Distribute;

import org.jcodec.common.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.constants.BaseData;
import org.policerewired.recorder.db.RecordingDb;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.receivers.RestartRequestReceiver;
import org.policerewired.recorder.service.RecorderService;
import org.policerewired.recorder.service.RestartJobService;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static org.policerewired.recorder.receivers.RestartRequestReceiver.REQUEST_CODE_ONE_OFF_RESTART;

/**
 * Application class for the Emergency Recorder. This class initialises a single instance of
 * the database.
 */
public class EmergencyRecorderApp extends Application {
  private static final String TAG = EmergencyRecorderApp.class.getSimpleName();
  private static String db_name = "recording_db";

  public static boolean job_set; // alarm used to restart the service in case of OS termination
  public static RecordingDb db;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "Emergency Recorder: onCreate");

    if (getResources().getBoolean(R.bool.appcenter_features_enabled)) {
      Log.d(TAG, "Connecting to AppCenter.");
      AppCenter.start(this, getString(R.string.api_key_appcenter), Analytics.class, Crashes.class, Distribute.class);
    }

    Log.d(TAG, "Initialising app database.");
    db = createDb();

    Log.d(TAG, "Starting service from Application.");
    startRecorderService(this);

    Log.d(TAG, "Scheduling restart job from application class.");
    scheduleRepeatingCheckup(this);

    recordAuditableEvent(new Date(), getString(R.string.event_audit_application_created), null, false);
  }

  @Override
  public void onTerminate() {
    Log.i(TAG, "Emergency Recorder: onTerminate");
    recordAuditableEvent(new Date(), getString(R.string.event_audit_application_terminated), null, false);
    super.onTerminate();
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
   * Task to pre-populate the database with standard rules. See: BaseData.getRules_live(Context);
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
    intent.setAction(RestartRequestReceiver.ACTION_RESTART_SERVICE);
    intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND); // permits the receiver to run in foreground
    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // also for fully stopped packages

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Log.v(TAG, "Calling startForegroundService from Application class.");
      recordAuditableEvent(new Date(), "App start intent initiated.", "Foreground service.", true);
      context.startForegroundService(intent);
    } else {
      Log.v(TAG, "Calling startService from Application class.");
      recordAuditableEvent(new Date(), "App start intent initiated.", "Regular service.", true);
      context.startService(intent);
    }
  }

  /**
   * Creates an intent to restart the service.
   * @param context context required to create the intent
   * @param requestCode request code for the alarm - important to distinguish pending intents
   * @return a PendingIntent containing an explicit Intent for the RestartRequestReceiver.
   */
  @Deprecated
  public static PendingIntent createRestartIntent(Context context, int requestCode) {
    Intent restartIntent = new Intent(context, RestartRequestReceiver.class);
    restartIntent.setAction(RestartRequestReceiver.ACTION_RESTART_SERVICE);
    restartIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND); // permits the receiver to run in foreground
    restartIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // also for fully stopped packages

    PendingIntent pending;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      pending = PendingIntent.getForegroundService(context, requestCode, restartIntent, FLAG_UPDATE_CURRENT);
    } else {
      pending = PendingIntent.getService(context, requestCode, restartIntent, FLAG_UPDATE_CURRENT);
    }

    return pending;
  }

  /**
   * Schedules a service restart after 60 seconds.
   */
  @Deprecated
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
    if (!job_set) {
      try {
        Log.d(TAG, "Scheduling repeat job.");
        RestartJobService.initialise(context);

        Log.i(TAG, "Scheduled repeating restart job for service.");
        job_set = true;

      } catch (Exception e) {
        Log.e(TAG, "Unable to schedule repeating restart job.", e);
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
        // android logs
        Log.d(TAG, String.format("Audit: %s, %s", item.type.name(), item.data));

        // database
        db.getRecordingDao().insert(item);

        // analytics
        Map<String, String> properties = new HashMap<>();
        properties.put("type", item.type.name());
        properties.put("data", item.data);
        properties.put("started", item.started.toString());
        Analytics.trackEvent(item.getClass().getSimpleName(), properties, Flags.PERSISTENCE_NORMAL);

      } catch (Exception e) {
        Log.e(TAG, "Unable to record new record of type: " + item.type.name(), e);
      }
    });
  }

  public static void recordAnalyticsIssue(String message, Object... details) {
    Map<String, String> properties = new HashMap<>();
    properties.put("occurred", new Date().toString());
    properties.put("message", message);

    int i = 0;
    for (Object detail : details) {
      ++i;
      properties.put("detail-" + i + "-class", detail.getClass().getSimpleName());
      if (detail instanceof String) {
        properties.put("detail-" + i + "-string", (String)detail);
      } else {
        properties.put("detail-" + i + "-json", new Gson().toJson(detail));
      }
    }

    Analytics.trackEvent("Issue", properties, Flags.PERSISTENCE_CRITICAL);
  }

  public static void recordAnalyticsException(Exception e) {
    Map<String, String> properties = new HashMap<>();
    properties.put("thrown", new Date().toString());

    properties.put("exception", e.getClass().getSimpleName());
    properties.put("exception-message", e.getMessage());
    properties.put("exception-stacktrace", parseStackTrace(e.getStackTrace()));

    if (e.getCause() != null) {
      properties.put("cause", e.getCause().getClass().getSimpleName());
      properties.put("cause-message", e.getCause().getMessage());
      properties.put("cause-stacktrace", parseStackTrace(e.getCause().getStackTrace()));
    }

    Analytics.trackEvent("Exception", properties, Flags.PERSISTENCE_CRITICAL);
  }

  private static String parseStackTrace(StackTraceElement[] elements) {
    List<String> items = new LinkedList<>();
    for (StackTraceElement element : elements) {
      items.add(element.toString());
    }
    return StringUtils.join2(items.toArray(), '\n');
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
   *   Requesting permission to ignore battery saving (and so allow the app to run in the background)
   *   also requires whitelisting on a case by case basis. Our use case does not fit neatly into any
   *   of the previously established 'legitimate' uses acknowledged by Google.
   * </p>
   */
  public static String[] get_permissions() {
    if (_universal_permissions == null) {
      String[] universal_permissions = new String[]{
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Manifest.permission.PROCESS_OUTGOING_CALLS
      };

      // NB. SYSTEM_ALERT_WINDOW is not included in the list above.
      // This must be granted through an alternate dialog.
      // See: OverlaySlide.requestOverlayPermission

      List<String> permissions = new LinkedList<>(Arrays.asList(universal_permissions));

      // from Android P onwards, we need the following additional permissions:
      // FOREGROUND_SERVICE (to hold our service in the foreground with a notification)
      // READ_PHONE_STATE, READ_CALL_LOG (to support PROCESS_OUTGOING_CALLS)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        //permissions.add(Manifest.permission.READ_PHONE_STATE);
        //permissions.add(Manifest.permission.READ_CALL_LOG);
      }

      _universal_permissions = permissions.toArray(new String[0]);
    }
    return _universal_permissions;
  }

  private static String[] _universal_permissions;
}

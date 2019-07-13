package org.policerewired.recorder.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import org.jcodec.common.StringUtils;
import org.jcodec.common.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.constants.Behaviour;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.db.entity.Rule;
import org.policerewired.recorder.receivers.EmbeddedOutgoingCallReceiver;
import org.policerewired.recorder.receivers.ScreenReceiver;
import org.policerewired.recorder.tasks.HybridCollection;
import org.policerewired.recorder.tasks.StitchHybridImagesTask;
import org.policerewired.recorder.tasks.ZippingTask;
import org.policerewired.recorder.ui.HomeActivity;
import org.policerewired.recorder.ui.overlay.BubbleCamConfig;
import org.policerewired.recorder.ui.overlay.BubbleCamOverlay;
import org.policerewired.recorder.ui.overlay.IBubbleCamOverlay;
import org.policerewired.recorder.ui.overlay.ILauncherOverlay;
import org.policerewired.recorder.ui.overlay.LauncherConfig;
import org.policerewired.recorder.ui.overlay.LauncherOverlay;
import org.policerewired.recorder.util.CapturePhotoUtils;
import org.policerewired.recorder.util.CaptureVideoUtils;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.PhotoAnnotationUtils;
import org.policerewired.recorder.util.SharingUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.policerewired.recorder.EmergencyRecorderApp.recordAuditableEvent;
import static org.policerewired.recorder.EmergencyRecorderApp.saveAuditRecord;

/**
 * Core service for this app - responsible for managing the overlay, and app database.
 */
public class RecorderService extends AbstractBackgroundBindingService<IRecorderService> implements IRecorderService {
  private static final String TAG = RecorderService.class.getSimpleName();

  public static final String ACTION_CALL_RECEIVED = "org.policerewired.recorder.service.RecorderService.CALL_RECEIVED";
  public static final String EXTRA_NUMBER = "number";

  private EmbeddedOutgoingCallReceiver call_receiver;
  private ScreenReceiver screen_receiver;

  private boolean embedded_call_receiver_registered;
  private boolean embedded_screen_receiver_registered;

  private BubbleCamOverlay bubblecam;
  private LauncherOverlay launcher;

  private NamingUtils naming;
  private PhotoAnnotationUtils annotation;
  private SharingUtils sharing;
  private StorageUtils storage;

  @Override
  protected void restoreFrom(SharedPreferences prefs) {
    // used for storing state (where long running tasks need to be resumed)
  }

  @Override
  protected void storeTo(SharedPreferences.Editor editor) {
    // used for storing state (where long running tasks need to be resumed)
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO(v2) move to injection for all helpers
    naming = new NamingUtils(this);
    annotation = new PhotoAnnotationUtils(this);
    sharing = new SharingUtils(this);
    storage = new StorageUtils(this);

    call_receiver = new EmbeddedOutgoingCallReceiver(call_listener);
    IntentFilter call_filter = new IntentFilter();
    call_filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    call_filter.addCategory(Intent.CATEGORY_DEFAULT);
    if (getResources().getBoolean(R.bool.use_embedded_listener)) {
      registerReceiver(call_receiver, call_filter);
      embedded_call_receiver_registered = true;
    }

    screen_receiver = new ScreenReceiver(screen_listener);
    IntentFilter screen_filter = new IntentFilter();
    screen_filter.addAction(Intent.ACTION_SCREEN_ON);
    screen_filter.addAction(Intent.ACTION_SCREEN_OFF);
    registerReceiver(screen_receiver, screen_filter);
    embedded_screen_receiver_registered = true;

    bubblecam = new BubbleCamOverlay(this, bubble_cam_listener, BubbleCamConfig.defaults(this));
    launcher = new LauncherOverlay(this, launcher_listener, LauncherConfig.defaults(this));

    recordAuditableEvent(new Date(), getString(R.string.event_audit_service_started), null, true);

    onPermissionsUpdated();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // NB. onDestroy is not called for services that are disposed of by the system.
    // This defensive code may not ever be reached - but if the Service voluntarily stops (perhaps
    // because its Notification and UI are removed, or under other unexpected circumstances) then
    // an Alarm will be set to reinitialise the service shortly afterwards.

    // unregister call receiver (if embedded version is enabled)
    if (embedded_call_receiver_registered) {
      try {
        unregisterReceiver(call_receiver);
        embedded_call_receiver_registered = false;
      } catch (Exception e) {
        Log.w(TAG, "Failed to unregister call_receiver", e);
      }
    }

    // unregister screen receiver
    if (embedded_screen_receiver_registered) {
      try {
        unregisterReceiver(screen_receiver);
        embedded_screen_receiver_registered = false;
      } catch (Exception e) {
        Log.w(TAG, "Failed to unregister screen_receiver", e);
      }
    }

    Log.w(TAG, "Service was stopped naturally. Scheduling a restart...");
    EmergencyRecorderApp.scheduleServiceStart(this);
    recordAuditableEvent(new Date(), getString(R.string.event_audit_service_stopped), getString(R.string.event_audit_alarm_set_1_min), true);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    String detail;
    if (intent == null) {
      detail = "(null intent)";
    } else {
      if (intent.getAction() == null) {
        detail = "(null action)";
      } else {
        detail = "action = " + intent.getAction();
      }
    }

    Log.i(TAG, getString(R.string.event_audit_intent_received_ACTION, detail));
    recordAuditableEvent(new Date(), getString(R.string.event_audit_intent_received), detail, true);

    if (intent != null && ACTION_CALL_RECEIVED.equals(intent.getAction())) {
      Log.d(TAG, "Initiating call handling...");
      String number = intent.getStringExtra(EXTRA_NUMBER);
      onOutgoingCall(number);
    }

    return super.onStartCommand(intent, flags, startId); // START_STICKY
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Log.w(TAG, "Initial task is being removed. Scheduling a restart...");
    EmergencyRecorderApp.scheduleServiceStart(this);
    recordAuditableEvent(new Date(), getString(R.string.event_audit_task_stopped), getString(R.string.event_audit_alarm_set_1_min), true);
    super.onTaskRemoved(rootIntent);
  }

  @Override
  public void onPermissionsUpdated() {
    if (hasOverlayPermission() && !bubblecam.isShowing() && !launcher.isInitialised()) {
      launcher.init();
    }
  }

  private void onOutgoingCall(String number) {
    // NB. this has to be quick - if the receiver takes too long, Android will kill it.
    // If not, this could be the issue. We should schedule the bubblecam.show() with a Handler.

    List<Rule> rules = getRulesFor_static(number);
    for (Rule rule : rules) {
      if (rule.matches(number)) {
        informUser(getString(R.string.toast_info_call_detected, number));
        recordCall(new Date(), number); // match found, record it
        switch (rule.behaviour) {
          case OpenBubbleCam:
            showBubbleCam();
            break;

          case OpenBubbleCamStartBurstMode:
            showAndHybridBubbleCam();
            break;

          case OpenBubbleCamStartVideoMode:
            showAndRecordBubbleCam();
            break;
        }

        if (rule.behaviour != Behaviour.Nothing) {
          break; // exit loop -- we have taken an action now
        }
      } // matches the number
    } // each rule
  }

  private IBubbleCamOverlay.Listener bubble_cam_listener = new IBubbleCamOverlay.Listener() {

    @Override
    public void closed() {
      launcher.indicate();
    }

    @Override
    public void photoCaptured(Date taken, Uri photo) {
      recordPhoto(taken, photo);
    }

    @Override
    public void videoCaptured(Date started, Uri video) {
      recordVideo(started, video);
    }

    @Override
    public void hybridPhotoCaptured(Date taken, Uri photo) { recordHybridPhoto(taken, photo); }

    @Override
    public void hybridsCaptured(HybridCollection collection) {
      // TODO(v2): move the video rescale parameters into configuration options that the user can edit
      int default_hybrid_video_max_width = getResources().getInteger(R.integer.default_hybrid_video_max_width);
      StitchHybridImagesTask.Params param = new StitchHybridImagesTask.Params(collection, default_hybrid_video_max_width, null);
      new StitchHybridImagesTask(RecorderService.this, foreground_channel).execute(param);
    }
  };

  @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
  private ILauncherOverlay.Listener launcher_listener = new ILauncherOverlay.Listener() {
    @Override
    public void launchSelected() {
      showBubbleCam();
    }
  };

  private void showBubbleCam() {
    if (!anyOutstandingPermissions() && hasOverlayPermission()) {
      bubblecam.show();
      launcher.hide();
    } else {
      informUser(R.string.toast_warning_need_permissions_to_launch_bubblecam);
    }
  }

  private void showAndHybridBubbleCam() {
    if (!anyOutstandingPermissions() && hasOverlayPermission()) {
      bubblecam.showAndHybrid();
      launcher.hide();
    } else {
      informUser(R.string.toast_warning_need_permissions_to_launch_bubblecam);
    }
  }

  private void showAndRecordBubbleCam() {
    if (!anyOutstandingPermissions() && hasOverlayPermission()) {
      bubblecam.showAndRecord();
      launcher.hide();
    } else {
      informUser(R.string.toast_warning_need_permissions_to_launch_bubblecam);
    }
  }

  @SuppressWarnings("Convert2Lambda")
  private ScreenReceiver.Listener screen_listener = new ScreenReceiver.Listener() {
    @Override
    public void onScreenStateChange(boolean isScreenAwake, boolean isPhoneLocked) {
      if (launcher.isInitialised() && isScreenAwake && isPhoneLocked) {
        launcher.indicate();
      }
    }
  };

  private EmbeddedOutgoingCallReceiver.Listener call_listener = this::onOutgoingCall;

  @Override
  public void showOverlay() {
    showBubbleCam();
  }

  @Override
  public void hideOverlay() {
    bubblecam.hide();
  }

  @Override
  protected BackgroundServiceConfig configure(BackgroundServiceConfig defaults) {
    defaults.setNotification(
      getString(R.string.recording_service_notification_title),
      getString(R.string.recording_service_notification_content),
      null, // no ticker
      R.mipmap.ic_launcher_foreground,
      HomeActivity.class,
      getString(R.string.foreground_channel_name),
      getString(R.string.foreground_channel_description),
      NotificationManagerCompat.IMPORTANCE_MAX,
      NotificationCompat.PRIORITY_MAX);
    return defaults;
  }

  @Override
  protected String[] getRequiredPermissions() {
    return EmergencyRecorderApp.get_permissions();
  }

  @Override
  public void setConfig() {
    // TODO(v2): called from the config activity to update configuration parameters
  }

  @Override
  public void getConfig() {
    // TODO(v2): called from the config activity to retrieve configuration parameters
  }

  @Override
  public Uri storeUserPhoto(@NotNull byte[] data, @NotNull Date taken, Location location, String geocode, String w3w) {
    Bitmap bmp = annotation.drawOnBitmap(
      BitmapFactory.decodeByteArray(data, 0, data.length),
      taken,
      location,
      geocode,
      w3w);
    String title = naming.generate_photo_title(taken);
    String description = naming.generate_photo_description(taken);
    Uri uri = CapturePhotoUtils.insertImage(this, bmp, title, description, taken, location);
    return uri;
  }

  @Override
  public Uri storeHybridPhoto(@NotNull byte[] data, @NotNull Date started, @NotNull Date taken, Location location, String geocode, String w3w) {
    Bitmap bmp = annotation.drawOnBitmap(
      BitmapFactory.decodeByteArray(data, 0, data.length),
      taken,
      location,
      geocode,
      w3w);
    String title = naming.generate_hybrid_photo_title(taken);
    String description = naming.generate_hybrid_photo_description(taken, started);
    Uri uri = CapturePhotoUtils.insertImage(this, bmp, title, description, taken, location);
    return uri;
  }

  @Override
  public Uri storeVideo(@NotNull File source, @NotNull Date started, @NotNull Date completed, Location location, String geocode, String w3w) {
    long duration_ms = completed.getTime() - started.getTime();
    String title = naming.generate_video_title(started);
    String description = naming.generate_video_description(started, duration_ms);
    Uri uri = CaptureVideoUtils.insertVideo(getContentResolver(), source, title, description, started, AuditRecordType.VideoRecording.mime_type, duration_ms);
    return uri;
  }

  @Override
  public void recordCall(@NotNull Date initiated,@NotNull String number) {
    AuditRecord item = new AuditRecord();
    item.started = initiated;
    item.type = AuditRecordType.OutgoingCall;
    item.data = number;
    saveAuditRecord(item);
  }

  @Override
  public void recordPhoto(@NotNull Date taken, @NotNull Uri uri) {
    AuditRecord item = new AuditRecord();
    item.started = taken;
    item.type = AuditRecordType.Photo;
    item.data = uri.toString();
    saveAuditRecord(item);
  }

  @Override
  public void recordHybridPhoto(@NotNull Date taken, @NotNull Uri uri) {
    AuditRecord item = new AuditRecord();
    item.started = taken;
    item.type = AuditRecordType.BurstModePhoto;
    item.data = uri.toString();
    saveAuditRecord(item);
  }

  @Override
  public void recordHybridVideo(@NotNull Date started, @NotNull Uri uri) {
    AuditRecord item = new AuditRecord();
    item.started = started;
    item.type = AuditRecordType.BurstModeVideo;
    item.data = uri.toString();
    saveAuditRecord(item);
  }

  @Override
  public void recordAudio(@NotNull Date started, @NotNull Uri uri) {
    AuditRecord item = new AuditRecord();
    item.started = started;
    item.type = AuditRecordType.AudioRecording;
    item.data = uri.toString();
    saveAuditRecord(item);
  }

  @Override
  public void recordVideo(@NotNull Date started, @NotNull Uri uri) {
    try {
      AuditRecord item = new AuditRecord();
      item.started = started;
      item.type = AuditRecordType.VideoRecording;
      item.data = uri.toString();
      saveAuditRecord(item);
    } catch (Exception e) {
      Log.e(TAG, "Could not store video.", e);
      informUser(R.string.toast_warning_exception_during_video_store);
    }
  }

  @Override
  public void deleteEntireAuditLog() {
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        EmergencyRecorderApp.db.getRecordingDao().deleteAll();
      } catch (Exception e) {
        Log.e(TAG, "Unable to delete audit log.", e);
        informUser(R.string.toast_warning_unable_to_delete_entire_audit_log);
      }
    });
  }

  @Override
  public void delete(@NotNull final Rule rule) {
    if (rule.locked) {
      informUser(R.string.toast_warning_rule_locked_cannot_delete);
      return;
    }

    Executors.newSingleThreadScheduledExecutor().execute(() -> {
      try {
        EmergencyRecorderApp.db.getRuleDao().delete(rule);
      } catch (Exception e) {
        Log.e(TAG, "Unable to delete rule: " + rule.name, e);
        informUser(R.string.toast_warning_unable_to_insert_rule);
      }
    });
  }

  @Override
  public void insert(@NotNull final Rule rule) {
    Executors.newSingleThreadScheduledExecutor().execute(() -> {
      try {
        EmergencyRecorderApp.db.getRuleDao().insert(rule);
      } catch (Exception e) {
        Log.e(TAG, "Unable to insert rule: " + rule.name, e);
        informUser(R.string.toast_warning_unable_to_insert_rule);
      }
    });
  }

  @Override
  public void update(@NotNull final Rule rule) {
    Executors.newSingleThreadScheduledExecutor().execute(() -> {
      try {
        EmergencyRecorderApp.db.getRuleDao().update(rule);
      } catch (Exception e) {
        Log.e(TAG, "Unable to update rule: " + rule.name, e);
        informUser(R.string.toast_warning_unable_to_insert_rule);
      }
    });
  }

  @Override
  public LiveData<List<Rule>> getRules_live() {
    return EmergencyRecorderApp.db.getRuleDao().getAll_live();
  }

  @Override
  public List<Rule> getRules_static() {
    return EmergencyRecorderApp.db.getRuleDao().getAll_static();
  }

  @Override
  public LiveData<List<AuditRecord>> getAuditLog_live() {
    return EmergencyRecorderApp.db.getRecordingDao().getNearlyAll_live(AuditRecordType.Debug);
  }

  @Override
  public LiveData<List<AuditRecord>> getMediaLog_live_mostRecent(AuditRecordType[] chosen_media, int max) {
    return EmergencyRecorderApp.db.getRecordingDao().getSpecifically_live_limited(chosen_media, max);
  }

  @Override
  public List<AuditRecord> getAuditLog_static() {
    return EmergencyRecorderApp.db.getRecordingDao().getAll_static();
  }

  @Override
  public List<AuditRecord> getAuditLog_static(Date from, Date until) {
    return EmergencyRecorderApp.db.getRecordingDao().getAllBetween_static(from, until);
  }

  @Override
  public long countAuditLog() {
    return EmergencyRecorderApp.db.getRecordingDao().count();
  }

  @Override
  public long countAuditLog(Date from, Date until) {
    return EmergencyRecorderApp.db.getRecordingDao().countBetween(from, until);
  }

  @Override
  public AuditRecord getEarliestLog() {
    return EmergencyRecorderApp.db.getRecordingDao().getEarliest();
  }

  @Override
  public AuditRecord getLatestLog() {
    return EmergencyRecorderApp.db.getRecordingDao().getLatest();
  }

  @Override
  public List<Rule> getRulesFor_static(String number) {
    return EmergencyRecorderApp.db.getRuleDao().getMatchingRules_static(number);
  }

  @Override
  public void zipAndShareAuditLog(Date from, Date to) {
    try {

      @SuppressLint("StaticFieldLeak")
      ZippingTask task = new ZippingTask(this, foreground_channel) {
        @Override
        protected void onPostExecute(Result result) {
          super.onPostExecute(result);

          if (result.areAnySuccessful()) {
            String subject = getString(R.string.share_subject_audit_log);
            String description = getString(R.string.share_description_audit_log);

            //Uri contentUri = getUriForFile(RecorderService.this, sharing.getFileProviderAuthority(), result.target_zip);
            Uri contentUri = Uri.fromFile(result.target_zip);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TITLE, subject);
            intent.putExtra(Intent.EXTRA_TEXT, description);

            // attachment
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent chooserIntent = Intent.createChooser(intent, getString(R.string.chooser_title_share_audit_log));
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooserIntent);
          }

        }
      };

      //File zipFile = sharing.generate_export_zip_file(new Date());
      File zipFile = storage.tempAuditLogZipFile();
      File logFile = createAuditLogFile();

      ZippingTask.Params param = new ZippingTask.Params();
      param.source_records = getAuditLog_static(from, to);
      param.log_file = logFile;
      param.target_zip = zipFile;

      task.execute(param);

    } catch (Exception e) {
      Log.e(TAG, "Unable to share zip file.");
    }
  }

  @Override
  public File createAuditLogFile() throws IOException {
    File file = storage.tempAuditFile(".csv");

    List<AuditRecord> records = getAuditLog_static();

    List<String> entries = new LinkedList<>();
    String csv_entry = "\"%s\"";

    for (AuditRecord record : records) {
      String time = String.format(csv_entry, String.valueOf(record.started.getTime()));
      String date = String.format(csv_entry, naming.getShortDate(record.started));
      String type = String.format(csv_entry, getString(record.type.description_id));
      String data = String.format(csv_entry, record.data);
      String row = StringUtils.join2(new String[] { time,date,type,data } , ',');
      entries.add(row);
    }

    String[] entries_array = entries.toArray(new String[entries.size()]);
    String log_final = StringUtils.join2(entries_array, '\n');
    IOUtils.writeStringToFile(file, log_final);

    return file;
  }

}

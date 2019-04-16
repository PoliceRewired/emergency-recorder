package org.policerewired.recorder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.BuildConfig;
import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.Behaviour;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.db.entity.Rule;
import org.policerewired.recorder.receivers.OutgoingCallReceiver;
import org.policerewired.recorder.receivers.ScreenReceiver;
import org.policerewired.recorder.tasks.HybridCollection;
import org.policerewired.recorder.tasks.StitchHybridImagesTask;
import org.policerewired.recorder.ui.ConfigActivity;
import org.policerewired.recorder.ui.overlay.BubbleCamConfig;
import org.policerewired.recorder.ui.overlay.BubbleCamOverlay;
import org.policerewired.recorder.ui.overlay.IBubbleCamOverlay;
import org.policerewired.recorder.ui.overlay.ILauncherOverlay;
import org.policerewired.recorder.ui.overlay.LauncherConfig;
import org.policerewired.recorder.ui.overlay.LauncherOverlay;
import org.policerewired.recorder.util.CapturePhotoUtils;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.PhotoAnnotationUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;

import static org.policerewired.recorder.EmergencyRecorderApp.recordAuditableEvent;
import static org.policerewired.recorder.EmergencyRecorderApp.saveAuditRecord;

/**
 * Core service for this app - responsible for managing the overlay, and app database.
 */
public class RecorderService extends AbstractBackgroundBindingService<IRecorderService> implements IRecorderService {
  private static final String TAG = RecorderService.class.getSimpleName();

  private OutgoingCallReceiver call_receiver;
  private ScreenReceiver screen_receiver;

  private BubbleCamOverlay bubblecam;
  private LauncherOverlay launcher;

  private NamingUtils naming;
  private PhotoAnnotationUtils annotation;

  public RecorderService() { }

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

    naming = new NamingUtils(this);
    annotation = new PhotoAnnotationUtils(this);

    call_receiver = new OutgoingCallReceiver(call_listener);
    IntentFilter call_filter = new IntentFilter();
    call_filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    call_filter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(call_receiver,  call_filter);

    screen_receiver = new ScreenReceiver(screen_listener);
    IntentFilter screen_filter = new IntentFilter();
    screen_filter.addAction(Intent.ACTION_SCREEN_ON);
    screen_filter.addAction(Intent.ACTION_SCREEN_OFF);
    registerReceiver(screen_receiver, screen_filter);

    bubblecam = new BubbleCamOverlay(this, bubble_cam_listener, BubbleCamConfig.defaults(this));
    launcher = new LauncherOverlay(this, launcher_listener, LauncherConfig.defaults(this));

    onPermissionsUpdated();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(call_receiver);

    // NB. onDestroy is not called for services that are disposed of by the system.
    // This defensive code may not ever be reached - but if the Service voluntarily stops (perhaps
    // because its Notification and UI are removed, or under other unexpected circumstances) then
    // an Alarm will be set to reinitialise the service shortly afterwards.

    Log.w(TAG, "Service was stopped naturally. Scheduling a restart...");
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent serviceIntent = new Intent(this, RecorderService.class);
    PendingIntent alarmIntent = PendingIntent.getService(this, 1000, serviceIntent, 0);
    alarmManager.setAndAllowWhileIdle(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      SystemClock.elapsedRealtime() + (60*1000),
      alarmIntent);
    Log.i(TAG, "Alarm scheduled for 1 minute.");
    recordAuditableEvent(new Date(), getString(R.string.event_audit_service_stopped), getString(R.string.event_audit_alarm_set_1_min));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "Intent received" + (intent.getAction() != null ? ": action=" + intent.getAction() : " (null action)"));
    if (BuildConfig.DEBUG) {
      recordAuditableEvent(new Date(), getString(R.string.event_audit_intent_received), intent.getAction());
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onPermissionsUpdated() {
    if (hasOverlayPermission() && !bubblecam.isShowing() && !launcher.isInitialised()) {
      launcher.init();
    }
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

  @SuppressWarnings("Convert2Lambda")
  private OutgoingCallReceiver.Listener call_listener = new OutgoingCallReceiver.Listener() {
    @Override
    public void onCall(String number) {
      // NB. this has to be quick - if the receiver takes too long, Android will kill it.
      // TODO(testing) - confirm that the bubblecam appears across a wide range of devices.
      // If not, this could be the issue. We should schedule the bubblecam.show() with a Handler.

      List<Rule> rules = getRulesFor(number);
      for (Rule rule : rules) {
        if (rule.matches(number)) {
          Toast.makeText(RecorderService.this, getString(R.string.toast_info_call_detected, number), Toast.LENGTH_SHORT).show();
          recordCall(new Date(), number);

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
  };

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
      R.drawable.ic_launcher_foreground,
      ConfigActivity.class,
      getString(R.string.foreground_channel_name),
      getString(R.string.foreground_channel_description),
      NotificationManagerCompat.IMPORTANCE_MAX,
      NotificationCompat.PRIORITY_MAX);
    return defaults;
  }

  @Override
  protected String[] getRequiredPermissions() {
    return EmergencyRecorderApp.permissions;
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
    AuditRecord item = new AuditRecord();
    item.started = started;
    item.type = AuditRecordType.VideoRecording;
    item.data = uri.toString();
    saveAuditRecord(item);
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
  public LiveData<List<Rule>> getRules() {
    return EmergencyRecorderApp.db.getRuleDao().getAll();
  }

  @Override
  public LiveData<List<AuditRecord>> getAuditLog_live() {
    return EmergencyRecorderApp.db.getRecordingDao().getAll_live();
  }

  @Override
  public List<AuditRecord> getAuditLog_static() {
    return EmergencyRecorderApp.db.getRecordingDao().getAll_static();
  }

  public List<Rule> getRulesFor(String number) {
    return EmergencyRecorderApp.db.getRuleDao().getMatchingRules(number);
  }
}

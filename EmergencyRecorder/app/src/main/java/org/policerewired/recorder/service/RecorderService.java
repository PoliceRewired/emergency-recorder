package org.policerewired.recorder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.RecordType;
import org.policerewired.recorder.db.entity.Recording;
import org.policerewired.recorder.receivers.OutgoingCallReceiver;
import org.policerewired.recorder.tasks.StitchHybridImagesTask;
import org.policerewired.recorder.ui.ConfigActivity;
import org.policerewired.recorder.ui.overlay.BubbleCamConfig;
import org.policerewired.recorder.ui.overlay.BubbleCamOverlay;
import org.policerewired.recorder.tasks.HybridCollection;
import org.policerewired.recorder.ui.overlay.IBubbleCamOverlay;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RecorderService extends AbstractBackgroundBindingService<IRecorderService> implements IRecorderService {
  private static final String TAG = RecorderService.class.getSimpleName();

  private OutgoingCallReceiver call_receiver;
  private BubbleCamOverlay overlay;

  public RecorderService() { }

  @Override
  protected void restoreFrom(SharedPreferences prefs) {

  }

  @Override
  protected void storeTo(SharedPreferences.Editor editor) {

  }

  @Override
  public void onCreate() {
    super.onCreate();

    call_receiver = new OutgoingCallReceiver(call_listener);
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(call_receiver,  filter);

    overlay = new BubbleCamOverlay(this, bubble_cam_listener, BubbleCamConfig.defaults());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(call_receiver);

    Log.w(TAG, "Service was stopped. Scheduling a restart...");
    // schedule an alarm to restart the service in 1 minute
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent serviceIntent = new Intent(this, RecorderService.class);
    PendingIntent alarmIntent = PendingIntent.getService(this, 1000, serviceIntent, 0);
    alarmManager.setAndAllowWhileIdle(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      SystemClock.elapsedRealtime() + (60*1000),
      alarmIntent);
  }

  private IBubbleCamOverlay.Listener bubble_cam_listener = new IBubbleCamOverlay.Listener() {

    @Override
    public void photoCaptured(Date taken, Uri photo) {
      recordPhoto(taken, photo);
    }

    @Override
    public void videoCaptured(Date started, Uri video) {
      recordVideo(started, video);
    }

    @Override
    public void hybridsCaptured(HybridCollection collection) {
      // TODO: shift video rescale params into configuration options
      int default_hybrid_video_max_width = getResources().getInteger(R.integer.default_hybrid_video_max_width);
      StitchHybridImagesTask.Params param = new StitchHybridImagesTask.Params(collection, default_hybrid_video_max_width, null);
      new StitchHybridImagesTask(RecorderService.this, foreground_channel).execute(param);
    }
  };

  private OutgoingCallReceiver.Listener call_listener = new OutgoingCallReceiver.Listener() {
    @Override
    public void onCall(String number) {
      Toast.makeText(RecorderService.this, "Call detected: " + number, Toast.LENGTH_SHORT).show();
      // TODO: determine best way to open BubbleCam based on called number and rules
      overlay.show();
    }
  };

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
      NotificationManagerCompat.IMPORTANCE_LOW,
      NotificationCompat.PRIORITY_LOW);
    return defaults;
  }

  @Override
  protected String[] getRequiredPermissions() {
    return EmergencyRecorderApp.permissions;
  }

  @Override
  public void setConfig() {
    // TODO: called from the config activity
  }

  @Override
  public void getConfig() {
    // TODO: called from the config activity
  }

  @Override
  public void showOverlay() {
    overlay.show();
  }

  @Override
  public void hideOverlay() {
    overlay.hide();
  }

  @Override
  public void recordCall(Date initiated, String number) {
    Recording item = new Recording();
    item.started = initiated;
    item.type = RecordType.OutgoingCall;
    item.data = number;
    saveRecord(item);
  }

  @Override
  public void recordPhoto(Date taken, Uri uri) {
    Recording item = new Recording();
    item.started = taken;
    item.type = RecordType.Photo;
    item.data = uri.toString();
    saveRecord(item);
  }

  @Override
  public void recordHybridPhoto(Date taken, Uri uri) {
    Recording item = new Recording();
    item.started = taken;
    item.type = RecordType.BurstModePhoto;
    item.data = uri.toString();
    saveRecord(item);
  }

  @Override
  public void recordHybridVideo(Date started, Uri uri) {
    Recording item = new Recording();
    item.started = started;
    item.type = RecordType.BurstModeVideo;
    item.data = uri.toString();
    saveRecord(item);
  }

  @Override
  public void recordAudio(Date started, Uri uri) {
    Recording item = new Recording();
    item.started = started;
    item.type = RecordType.AudioRecording;
    item.data = uri.toString();
    saveRecord(item);
  }

  @Override
  public void recordVideo(Date started, Uri uri) {
    Recording item = new Recording();
    item.started = started;
    item.type = RecordType.VideoRecording;
    item.data = uri.toString();
    saveRecord(item);
  }

  private void saveRecord(Recording item) {
    Executors.newSingleThreadScheduledExecutor().execute(() -> {
      try {
        EmergencyRecorderApp.db.getRecordingDao().insert(item);
      } catch (Exception e) {
        Log.e(TAG, "Unable to record new record of type: " + item.type.name(), e);
      }
    });
  }
}

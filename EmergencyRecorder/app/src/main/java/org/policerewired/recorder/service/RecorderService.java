package org.policerewired.recorder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.receivers.OutgoingCallReceiver;
import org.policerewired.recorder.tasks.StitchHybridImagesTask;
import org.policerewired.recorder.ui.ConfigActivity;
import org.policerewired.recorder.ui.overlay.BubbleCamConfig;
import org.policerewired.recorder.ui.overlay.BubbleCamOverlay;
import org.policerewired.recorder.DTO.HybridCollection;
import org.policerewired.recorder.ui.overlay.IBubbleCamOverlay;

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
    public void photoCaptured() { }

    @Override
    public void videoCaptured() { }

    @Override
    public void hybridsCaptured(HybridCollection collection) {
      // TODO: shift image size into configuration options
      StitchHybridImagesTask.Params param = new StitchHybridImagesTask.Params(collection, 320, null);
      new StitchHybridImagesTask(RecorderService.this).execute(param);
    }
  };

  private OutgoingCallReceiver.Listener call_listener = new OutgoingCallReceiver.Listener() {
    @Override
    public void onCall(String number) {
      Toast.makeText(RecorderService.this, "I saw that: " + number, Toast.LENGTH_SHORT).show();
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
}

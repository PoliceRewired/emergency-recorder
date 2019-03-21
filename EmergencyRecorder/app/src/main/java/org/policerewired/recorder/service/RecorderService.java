package org.policerewired.recorder.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.receivers.OutgoingCallReceiver;
import org.policerewired.recorder.ui.ConfigActivity;
import org.policerewired.recorder.ui.overlay.BubbleCamConfig;
import org.policerewired.recorder.ui.overlay.BubbleCamOverlay;
import org.policerewired.recorder.ui.overlay.HybridCollection;
import org.policerewired.recorder.ui.overlay.IBubbleCamOverlay;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RecorderService extends AbstractBackgroundBindingService<IRecorderService> implements IRecorderService {

  private OutgoingCallReceiver receiver;
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

    receiver = new OutgoingCallReceiver(call_listener);
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(receiver,  filter);

    overlay = new BubbleCamOverlay(this, bubble_cam_listener, BubbleCamConfig.defaults());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(receiver);
    // TODO: schedule a restart with a Job, here
    // TODO: also a BOOT listener
  }

  private IBubbleCamOverlay.Listener bubble_cam_listener = new IBubbleCamOverlay.Listener() {
    @Override
    public void photoCaptured() { }

    @Override
    public void videoCaptured() { }

    @Override
    public void hybridsCaptured(HybridCollection collection) {
      // TODO: process hybrids in an async task - using Jcodec to convert them into a video
      // See: http://jcodec.org/

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

package org.policerewired.recorder.receivers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
 * Receiver able to detect screen on and screen off.
 * Used to promote the launcher tab above the lock screen by re-adding it to the WindowManager.
 */
public class ScreenReceiver extends BroadcastReceiver {
  private static final String TAG = ScreenReceiver.class.getSimpleName();

  private Listener listener;

  public ScreenReceiver(Listener listener) {
    this.listener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())
      || Intent.ACTION_USER_PRESENT.equals(intent.getAction()) // maybe not needed?
      || Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {

      PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      boolean isScreenAwake = powerManager.isInteractive();

      KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
      boolean isPhoneLocked = myKM.isKeyguardLocked();

      Log.i(TAG, "Detected screen state: " + (isScreenAwake ? "ON" : "OFF"));
      Log.i(TAG, "Detected locked state: " + (isPhoneLocked ? "LOCKED" : "UNLOCKED"));

      listener.onScreenStateChange(isScreenAwake, isPhoneLocked);
    }
  }

  public interface Listener {
    void onScreenStateChange(boolean isScreenAwake, boolean isPhoneLocked);
  }
}

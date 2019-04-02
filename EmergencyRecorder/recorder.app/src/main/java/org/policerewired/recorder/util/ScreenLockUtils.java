package org.policerewired.recorder.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

public class ScreenLockUtils {

  public static void scratch(Context context) {
    PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    boolean isScreenAwake = powerManager.isInteractive();

    KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    boolean isPhoneLocked = myKM.isKeyguardLocked();


    final IntentFilter theFilter = new IntentFilter();
    theFilter.addAction(Intent.ACTION_SCREEN_ON);
    theFilter.addAction(Intent.ACTION_SCREEN_OFF);
    theFilter.addAction(Intent.ACTION_USER_PRESENT);


  }

}
